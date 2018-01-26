package cl.idatum.idatumterreno.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.cachemanager.CacheManager;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.infowindow.InfoWindow;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import cl.idatum.idatumterreno.R;
import cl.idatum.idatumterreno.api.ApiUtils;
import cl.idatum.idatumterreno.api.ApiUtilsXML;
import cl.idatum.idatumterreno.elena.Constante;
import cl.idatum.idatumterreno.elena.ElenaExcepciones;
import cl.idatum.idatumterreno.models.Contenidos;
import cl.idatum.idatumterreno.response.Idatum;
import cl.idatum.idatumterreno.utils.AtributosDinamicos;
import cl.idatum.idatumterreno.utils.Config;
import cl.idatum.idatumterreno.utils.XmlDoc;
import io.realm.Realm;
import io.realm.RealmResults;
import okhttp3.Cache;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CacheDownloader extends AppCompatActivity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener, TextWatcher, CacheManager.CacheManagerCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    ProgressDialog progressBar;

    Button botonCache, executeJob;
    SeekBar zoom_min;
    SeekBar zoom_max;
    EditText cache_north, cache_south, cache_east, cache_west;
    TextView cache_estimate;
    CacheManager mgr;
    AlertDialog downloadPrompt = null;
    CacheManager.CacheManagerTask downloadingTask = null;

    Context context;

    private FloatingActionButton fabTipoMapa, actual, web, buscarTarea, guardarCoorde, fabTools, borrar, contenidoLocal;
    private final static int MULTIPLE_PERMISSION_REQUEST_CODE = 200;

    private MapView map;
    private Marker marker;
    Double myLatitudeMarker = null;
    Double myLongitudeMarker = null;
    private String TAG = "OsmFragment";
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private ProgressDialog loading;

    private CardView cardView;
    private TextView resultadoCont;
    private Animation fabRotate1, fabRotate2, fabOpen, fabClose;
    private Boolean isOpen = false;
    private Location lastLocation;
    private SharedPreferences prefs;

    private int tipoTarea;
    private String id_usuario, id_relacion, id_obj_cont, id_tipo_cont, id_tarea, id_obj_cont_interno, nombre_objeto;
    private String id_obj_cont_local, id_usuario_local;
    Realm realm;
    private RealmResults<Contenidos> contenidos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cache_downloader);
        context = CacheDownloader.this;
        realm = Realm.getDefaultInstance();
        setToolbar();
        iniciarVistas();
        checkPermissionsState();
        checkFabTools();
        construirLocationRequest();
        construirGoogleApiClient();

        if (getIntent().getExtras() != null) {
            Bundle bundle = getIntent().getExtras();
            //Log.e(TAG, getArguments().getString("tipoTarea") + " - " + getArguments().getString("id_contenido"));
            if (bundle.get("tipotarea") != null) {
                buscarTareaMetodo();
            }
            if (bundle.get("id") != null) {
                resultadoCont.setText("Ficha a georreferenciar: \n" + bundle.get("id").toString() + " - " + bundle.get("nombre") + "\nTarea Manual");
                cardView.setVisibility(View.VISIBLE);
                //resultadoCont.setVisibility(View.VISIBLE);
                id_obj_cont_interno = bundle.get("id").toString();
                tipoTarea = 1;
            }
            if ("3".equals(bundle.getString("tipoTarea"))) {
                tipoTarea = 3;
                id_obj_cont_local = bundle.getString("id_contenido");
                id_usuario_local = bundle.getString("id_usuario");

                resultadoCont.setText("Ficha a georreferenciar: \n" + id_obj_cont_local + " - " + bundle.get("nombre_contenido") + "\nTarea Local");
                cardView.setVisibility(View.VISIBLE);

            }
        }


        fabTools.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isOpen) {
                    cerrarBotones();
                } else {
                    abrirbotones();
                }
            }
        });

        actual.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                marcarActual();
                checkCondicionesBotonGuardar();
                //checkMarker();
            }
        });
        borrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                irBorrar();
                checkCondicionesBotonGuardar();
                //checkMarker();
            }
        });
        web.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                irWeb();
            }
        });
        buscarTarea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (prefs.getBoolean(Config.LOGIN_SHARED, false))
                    buscarTareaMetodo();
                else Toast.makeText(context, "Debe ingresar al sistema", Toast.LENGTH_SHORT).show();
            }
        });
        guardarCoorde.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                evaluarTipoTarea();
            }
        });

        contenidoLocal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buscarContenidosLocales();
            }
        });

    }

    private void setToolbar() {
        android.support.v7.widget.Toolbar toolbar = findViewById(R.id.appbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void buscarContenidosLocales() {
        if (prefs.getBoolean(Config.LOGIN_SHARED, false)) {
            final ArrayList<OverlayItem> items = new ArrayList<OverlayItem>();
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    contenidos = realm.where(Contenidos.class).contains("id_usuario", prefs.getString(Config.ID_USUARIO_SHARED, "")).findAll();

                    for (Contenidos contenidoRecorrido : contenidos) {
                        if (contenidoRecorrido.getLatitud() != null) {
                            items.add(new OverlayItem(contenidoRecorrido.getNombre_contenido(),
                                    "Lat " + contenidoRecorrido.getLatitud().substring(0, 6) + ", Lon " + contenidoRecorrido.getLongitud().substring(0, 6),
                                    new GeoPoint(Double.parseDouble(contenidoRecorrido.getLatitud()), Double.parseDouble(contenidoRecorrido.getLongitud()))));
                        }
                    }
                }
            });
            //checkMarker();
            if (items.size() == 0) {
                Toast.makeText(context, "No hay contenidos locales georreferenciados.", Toast.LENGTH_SHORT).show();
                return;
            }

            ItemizedOverlayWithFocus<OverlayItem> markersOverlay = new ItemizedOverlayWithFocus<>(
                    items,
                    new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                        @Override
                        public boolean onItemSingleTapUp(int index, OverlayItem item) {
                            return true;
                        }

                        @Override
                        public boolean onItemLongPress(int index, OverlayItem item) {
                            return false;
                        }
                    }, context);
            markersOverlay.setFocusItemsOnTap(true);
            map.getOverlays().add(markersOverlay);
            map.invalidate();

            zoomToBoundingBox(items);
        } else {
            Toast.makeText(context, "Debe ingresar al sistema", Toast.LENGTH_SHORT).show();
        }
    }

    private void zoomToBoundingBox(ArrayList<OverlayItem> items) {
        double norte = 0, sur = 0, oeste = 0, este = 0;
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i) == null) continue;

            double lat = items.get(i).getPoint().getLatitude();
            double lon = items.get(i).getPoint().getLongitude();

            if ((i == 0) || (lat > norte)) norte = lat;
            if ((i == 0) || (lat < sur)) sur = lat;
            if ((i == 0) || (lon < oeste)) oeste = lon;
            if ((i == 0) || (lon > este)) este = lon;

        }


        map.zoomToBoundingBox(new BoundingBox(norte, este, sur, oeste), true);


    }

    private void evaluarTipoTarea() {
        if (myLatitudeMarker != null && myLongitudeMarker != null) {
            if (tipoTarea == 1) {
                //Toast.makeText(context, "tarea manual para id: " + id_obj_cont_interno, Toast.LENGTH_SHORT).show();
                guardarCoordenadasManual(myLatitudeMarker, myLongitudeMarker);
            } else if (tipoTarea == 2) {
                //Toast.makeText(context, "tarea asignada desde ficha externa", Toast.LENGTH_SHORT).show();
                guardarCoordenadasFicha(myLatitudeMarker, myLongitudeMarker);
            } else if (tipoTarea == 3) {
                //Toast.makeText(context, "tarea local", Toast.LENGTH_SHORT).show();
                guardarCoordenadasLocal();
            } else {
                Toast.makeText(context, "No hay tareas asignadas", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(context, "Debe seleccionar un punto en el mapa", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Crea un dialogo de alerta.
     * Guarda las coordenadas en la base de datos local del movil.
     */
    private void guardarCoordenadasLocal() {
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(context);
        builder.setTitle("AVISO");
        builder.setMessage("Ficha Id: " + id_obj_cont_local + "\n\n¿Esta seguro que desea guardar las coordenadas? \n" + "\nLatitud: " + myLatitudeMarker + "\nLongitud: " + myLongitudeMarker)
                .setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        realm.executeTransactionAsync(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                Contenidos contenido = realm.where(Contenidos.class)
                                        .equalTo("id_usuario", id_usuario_local)
                                        .equalTo("id_contenido", id_obj_cont_local)
                                        .findFirst();
                                contenido.setLatitud(myLatitudeMarker.toString());
                                contenido.setLongitud(myLongitudeMarker.toString());
                            }
                        }, new Realm.Transaction.OnSuccess() {
                            @Override
                            public void onSuccess() {
                                Snackbar.make(findViewById(R.id.activity_cache), "Proceso Local Finalizado",
                                        Snackbar.LENGTH_SHORT)
                                        .show();
                                irBorrar();
                                resultadoCont.setText("");
                                cardView.setVisibility(View.INVISIBLE);
                                tipoTarea = 0;
                                id_obj_cont_local = null;
                                checkCondicionesBotonGuardar();
                                //checkMarker();
                            }
                        }, new Realm.Transaction.OnError() {
                            @Override
                            public void onError(Throwable error) {
                                Toast.makeText(context, error.toString(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }).setNegativeButton("Mover Ubicacion", null);
        builder.create();
        builder.show();
    }

    private void guardarCoordenadasManual(final Double latitudSend, final Double longitudSend) {
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(context);
        builder.setTitle("AVISO");

        builder.setMessage("Ficha Id: " + id_obj_cont_interno + "\n\n¿Esta seguro que desea guardar las coordenadas? \n" + "\nLatitud: " + latitudSend + "\nLongitud: " + longitudSend)
                .setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        loading = ProgressDialog.show(context, "Obteniendo datos", "Por favor, espere...", false, false);

                        String urlBusqueda = prefs.getString(Config.URL_SHARED, "no disp") + "/exp/exp_modulo294.php";
                        StringWriter writer = buscarXml(latitudSend, longitudSend);
                        Call<Idatum> idatumCall = ApiUtilsXML.obtenerClienteXml().xmlWebService(urlBusqueda, "proceso_webservice", writer.toString());
                        idatumCall.enqueue(new Callback<Idatum>() {
                            @Override
                            public void onResponse(Call<Idatum> call, Response<Idatum> response) {

                                if (response.isSuccessful()) {
                                    String estadoTexto = response.body().getContenidos().get(0).getRespuesta().getEstado();
                                    String mensajeTexto = response.body().getContenidos().get(0).getRespuesta().getMensaje();
                                    loading.dismiss();
                                    //Toast.makeText(MapsActivity.this, "mensaje: " + mensajeTexto, Toast.LENGTH_SHORT).show();

                                    if (estadoTexto.equals("1")) {
                                        Toast.makeText(context, "Error : " + mensajeTexto, Toast.LENGTH_LONG).show();
                                    }
                                    if (estadoTexto.equals("0")) {
                                        // Toast.makeText(context, "Proceso Manual Finalizado", Toast.LENGTH_SHORT).show();
                                        Snackbar.make(findViewById(R.id.activity_cache), "Proceso Manual Finalizado",
                                                Snackbar.LENGTH_SHORT)
                                                .show();
                                        irBorrar();
                                        resultadoCont.setText("");
                                        cardView.setVisibility(View.INVISIBLE);
                                        tipoTarea = 0;
                                        id_obj_cont_interno = null;
                                        checkCondicionesBotonGuardar();
                                        //checkMarker();
                                    }
                                } else {
                                    loading.dismiss();
                                    Toast.makeText(context, "Respuesta fallo", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onFailure(Call<Idatum> call, Throwable t) {
                                loading.dismiss();
                                Toast.makeText(context, "Error: " + t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();

                            }
                        });

                    }
                }).setNegativeButton("Mover Ubicacion", null);
        builder.create();
        builder.show();
    }

    private StringWriter buscarXml(Double latitudSend, Double longitudSend) {
        StringWriter writer;
        XmlDoc xmlDoc = null;
        try {
            xmlDoc = new XmlDoc();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        String id_tipo_cont = prefs.getString(Config.ID_TIPO_CONT_SHARED_, "no disponible");
        String id_relacion = prefs.getString(Config.ID_RELACION_SHARED, "no disponible");
        String usuario = prefs.getString(Config.USUARIO_SHARED, "no disponible");
        String contraseña = prefs.getString(Config.CLAVE_SHARED, "no es posible");

        String texto_libre = "";
        String tipo_rel = "2";
        String accion = "2";
        writer = xmlDoc.generateXML(texto_libre,
                accion,
                usuario,
                contraseña,
                id_obj_cont_interno,
                id_relacion,
                latitudSend,
                longitudSend,
                id_tipo_cont,
                tipo_rel,
                "",
                "",
                "",
                "",
                new ArrayList<AtributosDinamicos>());

        return writer;
    }

    /**
     * Guarda las coordenadas para georreferenciar la ficha de contenido.
     * Tarea desde la ficha-boton.
     */
    private void guardarCoordenadasFicha(final Double latitudSend, final Double longitudSend) {
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(context);
        builder.setTitle("AVISO");
        builder.setMessage("Ficha Id: " + id_obj_cont + "\n\n¿Esta seguro que desea guardar las coordenadas? \n" + "\nLatitud: " + latitudSend + "\nLongitud: " + longitudSend)
                .setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        loading = ProgressDialog.show(context, "Obteniendo datos", "Por favor, espere...", false, false);

                        String urlBusqueda = prefs.getString(Config.URL_SHARED, "no disp") + "/exp/exp_modulo294.php";
                        StringWriter writer = buscarXmlFicha(latitudSend, longitudSend);
                        Call<Idatum> idatumCall = ApiUtilsXML.obtenerClienteXml().xmlWebService(urlBusqueda, "proceso_webservice", writer.toString());
                        idatumCall.enqueue(new Callback<Idatum>() {
                            @Override
                            public void onResponse(Call<Idatum> call, Response<Idatum> response) {
                                if (response.isSuccessful()) {
                                    String estadoTexto = response.body().getContenidos().get(0).getRespuesta().getEstado();
                                    String mensajeTexto = response.body().getContenidos().get(0).getRespuesta().getMensaje();
                                    loading.dismiss();
                                    //Toast.makeText(MapsActivity.this, "mensaje: " + mensajeTexto, Toast.LENGTH_SHORT).show();

                                    if (estadoTexto.equals("1")) {
                                        Toast.makeText(context, "Error mensaje: " + mensajeTexto, Toast.LENGTH_LONG).show();
                                    }
                                    if (estadoTexto.equals("0")) {
                                        finalizarTarea();
                                    }
                                } else {
                                    loading.dismiss();
                                    Toast.makeText(context, "Respuesta fallo", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onFailure(Call<Idatum> call, Throwable t) {
                                loading.dismiss();
                                Toast.makeText(context, "Error: " + t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }).setNegativeButton("Mover ubicacion", null);
        builder.create();
        builder.show();

    }

    /**
     * Se encarga de crear la estructura del XML a enviar.
     *
     * @param latitudSend  Latitud a enviar.
     * @param longitudSend Longitud a enviar.
     * @return Retorna el xml a enviar al webservice.
     */
    private StringWriter buscarXmlFicha(Double latitudSend, Double longitudSend) {
        StringWriter writer;
        XmlDoc xmlDoc = null;
        try {
            xmlDoc = new XmlDoc();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        String usuario = prefs.getString(Config.USUARIO_SHARED, "no disponible");
        String contraseña = prefs.getString(Config.CLAVE_SHARED, "no es posible");
        String texto_libre = "";
        String tipo_rel = "2";
        String accion = "2";
        writer = xmlDoc.generateXML(texto_libre,
                accion,
                usuario,
                contraseña,
                id_obj_cont,
                id_relacion,
                latitudSend,
                longitudSend,
                id_tipo_cont,
                tipo_rel,
                "",
                "",
                "",
                "",
                new ArrayList<AtributosDinamicos>());
        return writer;
    }

    /**
     * Se encarga de finalizar el proceso desde Ficha-Boton. Con el fin de refrescar la ficha donde se presiono el boton.
     */
    private void finalizarTarea() {
        String urlBusqueda = prefs.getString(Config.URL_SHARED, "") + "/exp/exp_modulo294.php";
        List<Object> list = new ArrayList<Object>();
        list = crearLista(list);
        ElenaExcepciones elena = null;
        try {
            elena = new ElenaExcepciones();
            elena.setExe_res(list);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Call<ElenaExcepciones> elenaExcepcionesCall2 = ApiUtils
                .obtenerCliente()
                .finalizarTarea(urlBusqueda,
                        "proceso_finalizado",
                        elena.jsonEncode(elena, Constante.GC_BULEANO_F).toString());

        elenaExcepcionesCall2.enqueue(new Callback<ElenaExcepciones>() {
            @Override
            public void onResponse(Call<ElenaExcepciones> call, retrofit2.Response<ElenaExcepciones> response) {
                ElenaExcepciones elenaExcepciones = response.body();
                obtenerRespuestaFinal(elenaExcepciones);
            }

            @Override
            public void onFailure(Call<ElenaExcepciones> call, Throwable t) {
                Toast.makeText(context, "Error: " + t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Se encarga de finalizar el proceso desde Ficha-Boton. Con el fin de refrescar la ficha donde se presiono el boton.
     */
    private List<Object> crearLista(List<Object> list) {
        HashMap<String, Object> res = new HashMap<String, Object>();
        res.put("id_usuario", id_usuario);
        res.put("id_relacion", id_relacion);
        res.put("id_obj_cont", id_obj_cont);
        res.put("id_tipo_cont", id_tipo_cont);
        res.put("id_tarea", id_tarea);
        res.put("resp_proceso", true);
        list.add(res);
        return list;
    }

    /**
     * Obtiene la respuesta final del servidor confirmando que cerro el proceso de georreferenciacion desde la Ficha-Boton.
     *
     * @param elena Objeto elena que transporta la informacion de la respuesta.
     */
    private void obtenerRespuestaFinal(ElenaExcepciones elena) {

        if (elena.getExe_est_res().equals("true")) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(Config.ID_TAREA_SHARED, "");
            id_obj_cont_interno = null;
            id_obj_cont = null;
            id_relacion = null;
            id_tipo_cont = null;
            myLatitudeMarker = null;
            myLongitudeMarker = null;

            editor.commit();
            Snackbar.make(findViewById(R.id.activity_cache), "Proceso Finalizado",
                    Snackbar.LENGTH_SHORT)
                    .show();
            irBorrar();
            resultadoCont.setText("");
            cardView.setVisibility(View.INVISIBLE);
            tipoTarea = 0;
            checkCondicionesBotonGuardar();
            //checkMarker();
            finish();

        } else {
            Toast.makeText(context, "Hubo error al finalizar",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void irWeb() {
        String url = prefs.getString(Config.URL_SHARED, "");
        String portal = prefs.getString(Config.ID_PORTAL, "");
        if (url != null && !url.isEmpty() && portal != null && !portal.isEmpty()) {
            Intent intentWeb = new Intent();
            intentWeb.setAction(Intent.ACTION_VIEW);
            intentWeb.setData(Uri.parse(url + "/?pid=" + portal));
            startActivity(Intent.createChooser(intentWeb, "Elige navegador"));
        } else {
            Toast.makeText(context, "Debe configurar un id de Portal", Toast.LENGTH_SHORT).show();
        }
    }

    private void irBorrar() {
        marker.remove(map);
        marker = null;
        myLatitudeMarker = null;
        myLongitudeMarker = null;
    }

    private void marcarActual() {
        if (marker != null) {
            marker.remove(map);
        }
        map.getController().setCenter(new GeoPoint(lastLocation));
        map.getController().setZoom(15);
        marker = new Marker(map);
        marker.setPosition(new GeoPoint(lastLocation));
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        marker.setTitle(String.valueOf("Lat " + lastLocation.getLatitude()).substring(0, 6) + ", Lon " + String.valueOf(lastLocation.getLongitude()).substring(0, 6));
        map.getOverlays().add(marker);
        checkCondicionesBotonGuardar();
    }

    private void buscarTareaMetodo() {

        loading = ProgressDialog.show(context, "Buscando tarea", "por favor espere..", false, false);
        String urlBusqueda = prefs.getString(Config.URL_SHARED, "") + "/exp/exp_modulo294.php";
        String usuarioId = prefs.getString(Config.ID_USUARIO_SHARED, "");

        Call<ElenaExcepciones> elenaExcepcionesCall = ApiUtils.obtenerCliente().buscarTarea(urlBusqueda, "solicitar_proceso", usuarioId);
        elenaExcepcionesCall.enqueue(new Callback<ElenaExcepciones>() {
            @Override
            public void onResponse(Call<ElenaExcepciones> call, Response<ElenaExcepciones> response) {
                loading.dismiss();
                if (response.isSuccessful()) {

                    ElenaExcepciones elenaExcepciones = response.body();
                    if (("0").equals(elenaExcepciones.getExe_men())) {
                        Snackbar.make(findViewById(R.id.activity_cache), "No hay tarea disponible",
                                Snackbar.LENGTH_LONG)
                                .show();

                    } else {
                        obtenerRespuesta(elenaExcepciones);
                    }
                } else {
                    Toast.makeText(context, "Intente de nuevo", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ElenaExcepciones> call, Throwable t) {
                loading.dismiss();
                Snackbar.make(findViewById(R.id.activity_cache), "Error : intente de nuevo",
                        Snackbar.LENGTH_SHORT)
                        .show();
            }
        });

    }

    private void obtenerRespuesta(ElenaExcepciones response) {

        try {
            JSONArray array = new JSONArray(response.getExe_res());
            JSONObject jsonObject = array.getJSONObject(0);
            id_usuario = jsonObject.getString("personas_id");
            id_relacion = jsonObject.getString("relacion_ug_id");
            id_obj_cont = jsonObject.getString("objeto_id");
            id_tipo_cont = jsonObject.getString("tipo_objeto_id");
            id_tarea = jsonObject.getString("mob_tab_id");
            nombre_objeto = jsonObject.getString("objeto_nomb");

            if (!id_usuario.equals("") && !id_relacion.equals("") && !id_obj_cont.equals("") && !id_tipo_cont.equals("") && !id_tarea.equals("")) {
                resultadoCont.setText("Ficha a georreferenciar: \n" + id_obj_cont + " - " + nombre_objeto + "\nTarea desde Botón-Ficha");
                tipoTarea = 2;
                cardView.setVisibility(View.VISIBLE);
                checkCondicionesBotonGuardar();
            } else {
                Toast.makeText(context, "Problemas Base de datos", Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void construirLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(10 * 1000)
                .setFastestInterval(5 * 1000)
                .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
    }

    private void construirGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    public void checkFabTools() {
        if (prefs.getBoolean(Config.LOGIN_SHARED, false)) {

            fabTools.startAnimation(fabOpen);
            fabTools.setClickable(true);

        } else {
            if (isOpen) {
                cerrarBotones();

                esconderFabTools();

            }
            esconderFabTools();
        }
    }

    private void esconderFabTools() {
        fabTools.startAnimation(fabClose);
        fabTools.setClickable(false);

    }

    private void cerrarBotones() {
        fabTools.startAnimation(fabRotate2);
        actual.startAnimation(fabClose);
        web.startAnimation(fabClose);
        buscarTarea.startAnimation(fabClose);
        contenidoLocal.startAnimation(fabClose);

        //guardarCoorde.startAnimation(fabClose);
        guardarCoorde.setVisibility(View.INVISIBLE);
        borrar.setVisibility(View.INVISIBLE);
        actual.setClickable(false);
        web.setClickable(false);
        buscarTarea.setClickable(false);
        contenidoLocal.setClickable(false);

        isOpen = false;


    }

    private void abrirbotones() {
        fabTools.startAnimation(fabRotate1);
        actual.startAnimation(fabOpen);
        web.startAnimation(fabOpen);
        buscarTarea.startAnimation(fabOpen);
        contenidoLocal.startAnimation(fabOpen);
        actual.setClickable(true);
        web.setClickable(true);
        buscarTarea.setClickable(true);
        contenidoLocal.setClickable(true);

        isOpen = true;
        checkCondicionesBotonGuardar();
        //checkMarker();
    }

    private void checkPermissionsState() {
        int internetPermissionCheck = ContextCompat.checkSelfPermission(context,
                Manifest.permission.INTERNET);

        int networkStatePermissionCheck = ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_NETWORK_STATE);

        int writeExternalStoragePermissionCheck = ContextCompat.checkSelfPermission(context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);

        int coarseLocationPermissionCheck = ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_COARSE_LOCATION);

        int fineLocationPermissionCheck = ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION);

        int wifiStatePermissionCheck = ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_WIFI_STATE);

        if (internetPermissionCheck == PackageManager.PERMISSION_GRANTED &&
                networkStatePermissionCheck == PackageManager.PERMISSION_GRANTED &&
                writeExternalStoragePermissionCheck == PackageManager.PERMISSION_GRANTED &&
                coarseLocationPermissionCheck == PackageManager.PERMISSION_GRANTED &&
                fineLocationPermissionCheck == PackageManager.PERMISSION_GRANTED &&
                wifiStatePermissionCheck == PackageManager.PERMISSION_GRANTED) {

            setupMap();

        } else {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(
                        new String[]{
                                Manifest.permission.INTERNET,
                                Manifest.permission.ACCESS_NETWORK_STATE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_WIFI_STATE},
                        MULTIPLE_PERMISSION_REQUEST_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MULTIPLE_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    boolean somePermissionWasDenied = false;
                    for (int result : grantResults) {
                        if (result == PackageManager.PERMISSION_DENIED) {
                            somePermissionWasDenied = true;
                        }
                    }
                    if (somePermissionWasDenied) {
                        Toast.makeText(context, "No se puede cargar los mapas sin dar los permisos.", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.e(TAG, "onRequestPermissionsResult: configurar mapa");
                        setupMap();
                    }
                } else {
                    Toast.makeText(context, "No se puede cargar los mapas sin dar los permisos.", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }

    private void setupMap() {
        checkIfGPSIsEnable();
        Log.d(TAG, "setupMap: Ok");
        map = (MapView) findViewById(R.id.osmMapview);
        mgr = new CacheManager(map);

        ITileSource layer = TileSourceFactory.DEFAULT_TILE_SOURCE;
        map.setTileSource(layer);

        map.setClickable(true);
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);
        map.getController().setZoom(15);

        //Encender mi locacion
        GpsMyLocationProvider gpsMyLocationProvider = new GpsMyLocationProvider(context);
        MyLocationNewOverlay mapLocationOverlay = new MyLocationNewOverlay(gpsMyLocationProvider, map);
        //mapLocationOverlay.enableFollowLocation();
        mapLocationOverlay.enableMyLocation();
        map.getOverlays().add(mapLocationOverlay);

        map.invalidate();

        //brujula
        CompassOverlay compassOverlay = new CompassOverlay(context, map);
        compassOverlay.enableCompass();
        map.getOverlays().add(compassOverlay);


        //atrapa todos los eventos de la capa de mapa.
        MapEventsOverlay mapEventsOverlay = new MapEventsOverlay(context, new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                InfoWindow.closeAllInfoWindowsOn(map);
                Log.e(TAG, "singleTapConfirmedHelper: cerrando borbujas");
                return true;
            }

            @Override
            public boolean longPressHelper(GeoPoint p) {
                if (marker != null) {
                    marker.remove(map);
                }
                myLatitudeMarker = p.getLatitude();
                myLongitudeMarker = p.getLongitude();

                Log.d(TAG, "punto: " + myLatitudeMarker + " - " + myLongitudeMarker);
                marker = new Marker(map);
                marker.setPosition(p);
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                marker.setTitle("Lan " + myLatitudeMarker.toString().substring(0, 6) + ", Lon " + myLongitudeMarker.toString().substring(0, 6));
                map.getOverlays().add(marker);

                checkCondicionesBotonGuardar();
                //checkMarker();
                return true;
            }
        });
        map.getOverlays().add(0, mapEventsOverlay);
    }

    private void checkIfGPSIsEnable() {
        try {
            int gpsSignal = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);
            if (gpsSignal == 0) {
                showInfoAlert();
            }
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void showInfoAlert() {
        new android.support.v7.app.AlertDialog.Builder(context)
                .setTitle("GPS signal")
                .setMessage("Gps desactivado, desea activarlo?")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(intent);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                })
                .show();
    }


    private void checkCondicionesBotonGuardar() {
        if (isOpen) {
            if (id_obj_cont_interno != null || id_obj_cont != null || id_obj_cont_local != null) {
                if (myLatitudeMarker != null && myLongitudeMarker != null) {
                    guardarCoorde.setVisibility(View.VISIBLE);
                    borrar.setVisibility(View.VISIBLE);
                } else {
                    guardarCoorde.setVisibility(View.INVISIBLE);
                    borrar.setVisibility(View.INVISIBLE);
                }
            } else {

                borrar.setVisibility(View.INVISIBLE);
                guardarCoorde.setVisibility(View.INVISIBLE);
            }
        }
    }

    private void iniciarVistas() {

        botonCache = findViewById(R.id.botonCache);
        botonCache.setOnClickListener(this);
        prefs = context.getSharedPreferences(Config.NOMBRE_SHARED, Context.MODE_PRIVATE);
        actual = findViewById(R.id.actual);
        contenidoLocal = findViewById(R.id.contenidoLocal);
        borrar = findViewById(R.id.borrar);
        web = findViewById(R.id.web);
        buscarTarea = findViewById(R.id.buscarTarea);
        guardarCoorde = findViewById(R.id.guardarCoorde);
        fabTipoMapa = findViewById(R.id.fabTipoMapa);
        fabTools = findViewById(R.id.fabTools);

        resultadoCont = findViewById(R.id.resultadoCont);
        cardView = findViewById(R.id.cardView);

        fabRotate1 = AnimationUtils.loadAnimation(context, R.anim.rotate_clockwise);
        fabRotate2 = AnimationUtils.loadAnimation(context, R.anim.rotate_anticlockwise);
        fabOpen = AnimationUtils.loadAnimation(context, R.anim.fab_open);
        fabClose = AnimationUtils.loadAnimation(context, R.anim.fab_close);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.executeJob:
                updateEstimate(true);
                break;
            case R.id.botonCache:
                showCacheManagerDialog();
                break;
        }
    }


    private void showCacheManagerDialog() {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(CacheDownloader.this);
        // set title
        alertDialogBuilder.setTitle("Manejador de Cache");
        //.setMessage(R.string.cache_manager_description);

        // set dialog message
        alertDialogBuilder.setItems(new CharSequence[]{
                        ("Espacio total en cache"),
                        ("Descargar cache"),
                        ("Cancelar todos"),
                        ("Mostrar descargas pendientes"),
                        ("Cerrar")
                }, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                showCurrentCacheInfo();
                                break;
                            case 1:
                                downloadJobAlert();
                                break;
                            case 2:
                                mgr.cancelAllJobs();
                                Toast.makeText(context, "Descargas canceladas.", Toast.LENGTH_LONG).show();
                                break;
                            case 3:
                                Toast.makeText(context, "Descargas pendientes: " + mgr.getPendingJobs(), Toast.LENGTH_LONG).show();
                                break;
                        }
                        dialog.dismiss();
                    }
                }
        );


        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();

        //mgr.possibleTilesInArea(mMapView.getBoundingBox(), 0, 18);

    }

    private void downloadJobAlert() {
        //prompt for input params
        AlertDialog.Builder builder = new AlertDialog.Builder(CacheDownloader.this);

        View view = View.inflate(context, R.layout.sample_cachemgr_input, null);

        BoundingBox boundingBox = map.getBoundingBox();
        zoom_max = view.findViewById(R.id.slider_zoom_max);
        zoom_max.setMax(map.getMaxZoomLevel());
        zoom_max.setOnSeekBarChangeListener(CacheDownloader.this);
        zoom_min = view.findViewById(R.id.slider_zoom_min);
        zoom_min.setMax(map.getMaxZoomLevel());
        zoom_min.setProgress(map.getMinZoomLevel());
        zoom_min.setOnSeekBarChangeListener(CacheDownloader.this);
        cache_east = view.findViewById(R.id.cache_east);
        cache_east.setText(boundingBox.getLonEast() + "");
        cache_north = view.findViewById(R.id.cache_north);
        cache_north.setText(boundingBox.getLatNorth() + "");
        cache_south = view.findViewById(R.id.cache_south);
        cache_south.setText(boundingBox.getLatSouth() + "");
        cache_west = view.findViewById(R.id.cache_west);
        cache_west.setText(boundingBox.getLonWest() + "");
        cache_estimate = view.findViewById(R.id.cache_estimate);

        //change listeners for both validation and to trigger the download estimation
        cache_east.addTextChangedListener(this);
        cache_north.addTextChangedListener(this);
        cache_south.addTextChangedListener(this);
        cache_west.addTextChangedListener(this);
        executeJob = view.findViewById(R.id.executeJob);
        executeJob.setOnClickListener(this);
        builder.setView(view);
        builder.setCancelable(true);
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                cache_east = null;
                cache_south = null;
                cache_estimate = null;
                cache_north = null;
                cache_west = null;
                executeJob = null;
                zoom_min = null;
                zoom_max = null;
            }
        });
        downloadPrompt = builder.create();
        downloadPrompt.show();
    }

    private void updateEstimate(boolean startJob) {
        try {
            if (cache_east != null &&
                    cache_west != null &&
                    cache_north != null &&
                    cache_south != null &&
                    zoom_max != null &&
                    zoom_min != null) {
                double n = Double.parseDouble(cache_north.getText().toString());
                double s = Double.parseDouble(cache_south.getText().toString());
                double e = Double.parseDouble(cache_east.getText().toString());
                double w = Double.parseDouble(cache_west.getText().toString());

                int zoommin = zoom_min.getProgress();
                int zoommax = zoom_max.getProgress();
                //nesw
                BoundingBox bb = new BoundingBox(n, e, s, w);
                int tilecount = mgr.possibleTilesInArea(bb, zoommin, zoommax);
                cache_estimate.setText(tilecount + " capas");
                if (startJob) {
                    if (downloadPrompt != null) {
                        downloadPrompt.dismiss();
                        downloadPrompt = null;
                    }
                    // prepare for a progress bar dialog ( do this first! )
                    progressBar = new ProgressDialog(CacheDownloader.this);
                    progressBar.setCancelable(true);
                    progressBar.setMessage("Descargando...");
                    progressBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                    progressBar.setProgress(0);
                    progressBar.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            //cancel the job wit the dialog is closed
                            downloadingTask.cancel(true);
                            System.out.println("Pending jobs " + mgr.getPendingJobs());
                        }
                    });
                    //this triggers the download
                    downloadingTask = mgr.downloadAreaAsyncNoUI(context, bb, zoommin, zoommax, this);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void showCurrentCacheInfo() {
        Toast.makeText(context, "Calculando...", Toast.LENGTH_SHORT).show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                        CacheDownloader.this);
                // set title
                alertDialogBuilder.setTitle("Manejador de Cache")
                        .setMessage("Capacidad Cache (megabytes): " + (mgr.cacheCapacity() / (1024) / 1024) + "\n" +
                                "Usado (megabytes): " + (mgr.currentCacheUsage() / (1024) / 1024));

                // set dialog message
                alertDialogBuilder.setItems(new CharSequence[]{

                                "cancel"
                        }, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }
                );

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // show it
                        // create alert dialog
                        final AlertDialog alertDialog = alertDialogBuilder.create();
                        alertDialog.show();
                    }
                });

            }
        }).start();
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        updateEstimate(false);
    }

    @Override
    public void afterTextChanged(Editable editable) {

    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        updateEstimate(false);

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onTaskComplete() {
        progressBar.dismiss();
        progressBar = null;
        Toast.makeText(context, "Download complete!", Toast.LENGTH_LONG).show();

    }

    @Override
    public void updateProgress(int progress, int currentZoomLevel, int zoomMin, int zoomMax) {
        if (progressBar != null) {
            progressBar.setProgress(progress);
        }

    }

    @Override
    public void downloadStarted() {

        if (progressBar != null) {
            progressBar.show();
        }
    }

    @Override
    public void setPossibleTilesInArea(int total) {

        if (progressBar != null) {
            progressBar.setMax(total);
        }
    }

    @Override
    public void onTaskFailed(int errors) {
        if (progressBar != null)
            progressBar.dismiss();
        progressBar = null;
        Toast.makeText(context, "Download complete with " + errors + " errors", Toast.LENGTH_LONG).show();

    }


    @Override
    protected void onStart() {
        super.onStart();
        googleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        googleApiClient.disconnect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        requestLocationUpdates();
    }

    private void requestLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
        lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);

        if (checkPermission())
            posicionarCentroMapa();
    }

    private void posicionarCentroMapa() {
        if (lastLocation != null) {
            Log.i(TAG, "se centro");
            map.getController().setCenter(new GeoPoint(lastLocation.getLatitude(), lastLocation.getLongitude()));
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "connection Suspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i(TAG, "Connection Failed: ConnectionResult.getErrorCode() = " + connectionResult.getErrorCode());
    }

    @Override
    public void onLocationChanged(Location location) {
        lastLocation = location;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (googleApiClient.isConnected()) {
            requestLocationUpdates();
        }
        Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context));
    }

    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }
}
