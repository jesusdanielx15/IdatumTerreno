package cl.idatum.idatumterreno.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Build;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import cl.idatum.idatumterreno.R;
import cl.idatum.idatumterreno.api.ApiUtils;
import cl.idatum.idatumterreno.elena.ElenaExcepciones;
import cl.idatum.idatumterreno.utils.Config;
import cl.idatum.idatumterreno.adapters.SpinnerAdapter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author Jesus Cañizales <jcanizales@siigsa.cl>
 * @version 2
 * @package cl.idatum.idatumterreno Proyecto IdatumTerreno
 * @link http://www.siigsa.cl
 * @copyright SIIGSA, Propiedad Intelectual y Derechos Patrimoniales de Software y Base de Datos i-datum. Registro Propiedad Intelectual Nº 211.351 y 211.352 respectivamente, con fecha 22 de noviembre del 2011
 * @since 25/10/2017
 */
public class ConfigBusqueda extends AppCompatActivity {

    private SharedPreferences prefs;
    private TextView idPortalText, idTipoContText, idRelText;
    private Spinner idPortal, idTipoCont, idRel, idMapa;
    private Button guardarConfiguracion, check;
    private ArrayList<String> listaIdPortales, listaIdTipoCont, listaIdRelacion, listaIdMapa;
    private String idPortalGuardar, idRelGuardar, idTipoContGuardar, idMapaGuardar;
    private String urlText;
    private TextView mostrarUrl;
    private String idContMapaGuardar;
    private String TAG = "ConfigBusqueda";

    /**
     * Metodo principal de la clase.
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config_busqueda);

        Toolbar toolbar = findViewById(R.id.appbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Typeface face = Typeface.createFromAsset(getAssets(), "fonts/oxygenregular.ttf");
        prefs = getSharedPreferences(Config.NOMBRE_SHARED, Context.MODE_PRIVATE);
        urlText = prefs.getString(Config.URL_SHARED, "");

        iniciarVistas();

        mostrarUrl.setText(prefs.getString(Config.URL_SHARED, "URL"));
        setTipoDeLetra(face);

        llenarSpinners();

        check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                idPortalGuardar = null;
                idRelGuardar = null;
                idTipoContGuardar = null;
                idMapaGuardar = null;
                llenarSpinners();
            }
        });


        guardarConfiguracion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (idPortalGuardar != "error" &&
                        idPortalGuardar != null &&
                        idTipoContGuardar != "error" &&
                        idTipoContGuardar != null &&
                        idRelGuardar != "error" &&
                        idRelGuardar != null &&
                        idMapaGuardar != "error" &&
                        idMapaGuardar != null &&
                        idContMapaGuardar != "error" &&
                        idContMapaGuardar != null) {

                    Toast.makeText(ConfigBusqueda.this, "Configuraciones guardadas", Toast.LENGTH_SHORT).show();
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString(Config.ID_PORTAL, idPortalGuardar);
                    editor.putString(Config.ID_TIPO_CONT_SHARED_, idTipoContGuardar);
                    editor.putString(Config.ID_RELACION_SHARED, idRelGuardar);
                    editor.putString(Config.ID_MAPA, idMapaGuardar);
                    editor.putString(Config.ID_CONT_MAPA, idContMapaGuardar);
                    editor.commit();
                    //Regresar a pantalla principal.
                    goToMain();
                } else {
                    Toast.makeText(ConfigBusqueda.this, "Debe seleccionar todos los elementos", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    /**
     * Se encarga de referenciar cada una de las vistas para ser usadas en java
     */
    private void iniciarVistas() {
        idPortalText = findViewById(R.id.idPortalText);
        idTipoContText = findViewById(R.id.idTipoContText);
        idRelText = findViewById(R.id.idRelText);
        idPortal = findViewById(R.id.idPortal);
        idTipoCont = findViewById(R.id.idTipoCont);
        idRel = findViewById(R.id.idRel);
        idMapa = findViewById(R.id.idMapa);
        guardarConfiguracion = findViewById(R.id.guardarConfiguracion);
        check = findViewById(R.id.check);
        mostrarUrl = findViewById(R.id.mostrarUrl);
    }

    /**
     * Llama los metodos de llenado de los selectores.
     */
    private void llenarSpinners() {
        llenarSpinnerIdPortal();
        llenarSpinnerIdTipoCont();
        llenarSpinnerMap();
    }

    /**
     * Llena el spinner con los diferentes ID's y mapas del sistema.
     */
    private void llenarSpinnerMap() {
        Call<ElenaExcepciones> call = ApiUtils.obtenerCliente().obtenerIdPortal(urlText + "/exp/exp_modulo294.php", "em294_listarMapas");
        listaIdMapa = new ArrayList<>();
        call.enqueue(new Callback<ElenaExcepciones>() {
            @Override
            public void onResponse(Call<ElenaExcepciones> call, Response<ElenaExcepciones> response) {

                try {
                    ElenaExcepciones elenaExcepciones = response.body();
                    final JSONArray array = new JSONArray(elenaExcepciones.getExe_res());
                    int posDef = 0;
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject jsonObject = array.getJSONObject(i);
                        String id_portal = jsonObject.getString("objeto_id");
                        String id_portalNombre = jsonObject.getString("objeto_nomb");

                        if (prefs.getString(Config.ID_CONT_MAPA, "").equals(id_portal)) {
                            posDef = i;
                        }
                        listaIdMapa.add(id_portal + " - " + id_portalNombre);
                    }

                    SpinnerAdapter adapter = new SpinnerAdapter(listaIdMapa, ConfigBusqueda.this);
                    idMapa.setAdapter(adapter);
                    idMapa.setSelection(posDef);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        idMapa.setDropDownHorizontalOffset(100);
                    }

                    idMapa.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            try {
                                idContMapaGuardar = array.getJSONObject(i).getString("objeto_id");
                                idMapaGuardar = array.getJSONObject(i).getString("mapas_id");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> adapterView) {
                        }
                    });


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ElenaExcepciones> call, Throwable t) {
                listaIdMapa.add("Error");
                SpinnerAdapter adapter = new SpinnerAdapter(listaIdMapa, ConfigBusqueda.this);
                idMapa.setAdapter(adapter);
                Toast.makeText(ConfigBusqueda.this, "error :" + t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    /**
     * Cambia el tipo de letra utilizado en los diferentes campos de texto.
     *
     * @param face Tipo de letra usado por Idatum.
     */
    private void setTipoDeLetra(Typeface face) {
        guardarConfiguracion.setTypeface(face);
        idPortalText.setTypeface(face);
        idTipoContText.setTypeface(face);
        idRelText.setTypeface(face);
        mostrarUrl.setTypeface(face);
    }

    /**
     * Llenar selector de los diferentes id de portal del sistema.
     */
    private void llenarSpinnerIdPortal() {
        Call<ElenaExcepciones> call = ApiUtils.obtenerCliente().obtenerIdPortal(urlText + "/exp/exp_modulo294.php", "proceso_id_portal");
        listaIdPortales = new ArrayList<String>();
        call.enqueue(new Callback<ElenaExcepciones>() {
            @Override
            public void onResponse(Call<ElenaExcepciones> call, Response<ElenaExcepciones> response) {
                try {
                    ElenaExcepciones elenaExcepciones = response.body();
                    final JSONArray array = new JSONArray(elenaExcepciones.getExe_res());
                    int pos_ef = 0;
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject jsonObject = array.getJSONObject(i);
                        String id_portal = jsonObject.getString("portal_id");
                        String id_portal_nombre = jsonObject.getString("portal_nomb");

                        if (prefs.getString(Config.ID_PORTAL, "").equals(id_portal)) {
                            pos_ef = i;
                        }
                        listaIdPortales.add(id_portal + " - " + id_portal_nombre);
                    }
                    SpinnerAdapter adapter = new SpinnerAdapter(listaIdPortales, ConfigBusqueda.this);
                    idPortal.setAdapter(adapter);
                    idPortal.setSelection(pos_ef);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        idPortal.setDropDownHorizontalOffset(100);
                    }
                    idPortal.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            try {
                                idPortalGuardar = array.getJSONObject(i).getString("portal_id");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> adapterView) {
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ElenaExcepciones> call, Throwable t) {
                listaIdPortales.add("Error");
                SpinnerAdapter adapter = new SpinnerAdapter(listaIdPortales, ConfigBusqueda.this);
                idPortal.setAdapter(adapter);
                Toast.makeText(ConfigBusqueda.this, "error :" + t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Llenar selector de id de tipo de contenido
     */
    private void llenarSpinnerIdTipoCont() {
        Call<ElenaExcepciones> call = ApiUtils.obtenerCliente().obtenerIdTipoCont(urlText + "/exp/exp_modulo294.php", "proceso_id_tipocont");
        listaIdTipoCont = new ArrayList<String>();
        call.enqueue(new Callback<ElenaExcepciones>() {
            @Override
            public void onResponse(Call<ElenaExcepciones> call, Response<ElenaExcepciones> response) {

                try {
                    ElenaExcepciones elenaExcepciones = response.body();
                    final JSONArray array = new JSONArray(elenaExcepciones.getExe_res());
                    int posDef = 0;
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject jsonObject = array.getJSONObject(i);
                        String id_tipoCont = jsonObject.getString("tipo_objeto_id");
                        String nombre_tipoCont = jsonObject.getString("tipo_objeto_nomb");

                        if (prefs.getString(Config.ID_TIPO_CONT_SHARED_, "").equals(id_tipoCont)) {
                            posDef = i;
                            Log.e(TAG, "onResponse: " + i);
                        }
                        listaIdTipoCont.add(id_tipoCont + " - " + nombre_tipoCont);
                    }
                    SpinnerAdapter adapter = new SpinnerAdapter(listaIdTipoCont, ConfigBusqueda.this);
                    idTipoCont.setAdapter(adapter);
                    idTipoCont.setSelection(posDef);

                    idTipoCont.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                            try {
                                idTipoContGuardar = array.getJSONObject(i).getString("tipo_objeto_id");
                                //Toast.makeText(SistemaActivity.this, idTipoContGuardar, Toast.LENGTH_SHORT).show();
                                llenarSpinnerIdRelacion(idTipoContGuardar);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }


                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> adapterView) {

                        }
                    });


                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }

            @Override
            public void onFailure(Call<ElenaExcepciones> call, Throwable t) {
                // Toast.makeText(SistemaActivity.this, t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                listaIdTipoCont.add("Error");
                SpinnerAdapter adapter = new SpinnerAdapter(listaIdTipoCont, ConfigBusqueda.this);
                idTipoCont.setAdapter(adapter);

            }
        });
    }

    /**
     * Metodo para que vaya a la actividad principal.
     */
    private void goToMain() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    /**
     * Llena el selector de Id de relaciones del sistema.
     *
     * @param idTipoCont Id de tipo de contenido usado para buscar los diferentes id de relacion.
     */
    private void llenarSpinnerIdRelacion(String idTipoCont) {
        String idusuario = prefs.getString(Config.ID_USUARIO_SHARED, "");
        Call<ElenaExcepciones> call = ApiUtils.obtenerCliente().obtenerIdRelacion(urlText + "/exp/exp_modulo294.php", "proceso_id_relacion", idTipoCont, idusuario);
        listaIdRelacion = new ArrayList<String>();
        call.enqueue(new Callback<ElenaExcepciones>() {
            @Override
            public void onResponse(Call<ElenaExcepciones> call, Response<ElenaExcepciones> response) {
                try {
                    ElenaExcepciones elenaExcepciones = response.body();
                    final JSONArray array = new JSONArray(elenaExcepciones.getExe_res());

                    int posDef = 0;
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject jsonObject = array.getJSONObject(i);
                        String id_rel = jsonObject.getString("relacion_ug_id");
                        String nombre_relacion = jsonObject.getString("relacion_ug_nomb");
                        if (prefs.getString(Config.ID_RELACION_SHARED, "").equals(id_rel)) {
                            posDef = i;
                        }
                        listaIdRelacion.add(id_rel + " - " + nombre_relacion);
                    }

                    SpinnerAdapter adapter = new SpinnerAdapter(listaIdRelacion, ConfigBusqueda.this);
                    idRel.setAdapter(adapter);
                    idRel.setSelection(posDef);
                    idRel.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            try {
                                idRelGuardar = array.getJSONObject(i).getString("relacion_ug_id");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> adapterView) {

                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            /**
             * Metodo autogenerado en caso de que se produzca un error con la peticion http.
             * @param call Es la peticion http.
             * @param t El objeto que contiene toda la informacion acerca del error.
             */
            @Override
            public void onFailure(Call<ElenaExcepciones> call, Throwable t) {
                listaIdRelacion.add("Error");
                SpinnerAdapter adapter = new SpinnerAdapter(listaIdRelacion, ConfigBusqueda.this);
                idRel.setAdapter(adapter);
            }
        });
    }
}
