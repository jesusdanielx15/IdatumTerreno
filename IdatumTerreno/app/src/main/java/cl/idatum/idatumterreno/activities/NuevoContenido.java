package cl.idatum.idatumterreno.activities;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import cl.idatum.idatumterreno.R;
import cl.idatum.idatumterreno.api.ApiUtils;
import cl.idatum.idatumterreno.api.ApiUtilsXML;
import cl.idatum.idatumterreno.elena.ElenaExcepciones;
import cl.idatum.idatumterreno.response.Idatum;
import cl.idatum.idatumterreno.utils.AtributosDinamicos;
import cl.idatum.idatumterreno.utils.Config;
import cl.idatum.idatumterreno.utils.MinMaxFilter;
import cl.idatum.idatumterreno.utils.ObjetoDB;
import cl.idatum.idatumterreno.utils.XmlDoc;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 * Clase encargada de generar un nuevo contenido
 * que genera dinamicamente todos los campos de textos,
 * selectores y checkboxes.
 *
 * @author Jesus Cañizales <jcanizales@siigsa.cl>
 * @version 2
 * @package cl.idatum.idatumterreno.activities Proyecto IdatumTerreno
 * @link http://www.siigsa.cl
 * @copyright SIIGSA, Propiedad Intelectual y Derechos Patrimoniales de Software y Base de Datos i-datum. Registro Propiedad Intelectual Nº 211.351 y 211.352 respectivamente, con fecha 22 de noviembre del 2011
 * @since 6/11/2017
 */
public class NuevoContenido extends AppCompatActivity {

    private SharedPreferences prefs;
    private Context context;
    private LinearLayout mainLayout;
    private android.support.v7.widget.Toolbar toolbar;
    private HashMap<Integer, EditText> hashMapAtr;
    private HashMap<Integer, Spinner> hashMapSpinner;
    private HashMap<Integer, JSONArray> hashMapSpinnerJson;
    private HashMap<String, EditText> hashMapDB;
    private HashMap<Integer, HashMap> hashMapCheckBox;
    private HashMap<Integer, CheckBox> hashMapCheckBoxId;
    private HashMap<Integer, JSONArray> hashMapCheckBoxJson;
    private String urlText;
    private String json;
    private String objeto_id;
    private ProgressDialog loading;
    protected static final String TAG = "NuevoContenido";

    /**
     * Metodo principal de la clase que se ejecuta
     * automaticamente cuando la actividad es creada.
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nuevo_contenido);
        context = NuevoContenido.this;
        prefs = getSharedPreferences(Config.NOMBRE_SHARED, Context.MODE_PRIVATE);

        urlText = prefs.getString(Config.URL_SHARED, "");

        setToolbar();

        mainLayout = (LinearLayout) findViewById(R.id.agregarContenido);
        evaluarExtras();
    }


    /**
     * Configura la barra de acciones como una toolbar.
     */
    private void setToolbar() {
        toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.appbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    /**
     * Evalua los datos que son enviados desde la actividad anterior.
     * Recibe los datos de la estructura del PVC en formato Json
     */
    private void evaluarExtras() {
        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            json = extras.getString("pvc"); //Obtaining data
            objeto_id = extras.getString("id_cont");

            crearVista(json, objeto_id);
        }
    }

    /**
     * Inicia el proceso de creado de las vistas dinamicas de la actividad.
     *
     * @param json    Estructura del PVC en formato Json con todos los datos a usar.
     * @param id_cont Es el Id de contenido que se esta creando.
     */
    private void crearVista(final String json, final String id_cont) {

        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray jsonArray = jsonObject.getJSONArray("plaVisConEstAtrRels");

            //Layout linear que sera creado para insertar todas las nuevas vistas.
            LinearLayout layout = new LinearLayout(
                    NuevoContenido.this);

            layout.setOrientation(LinearLayout.VERTICAL);

            //Se inician cada uno de los diferentes HashMaps a usarse
            //para el funcionamiento de referenciacion de vistas.
            hashMapDB = new HashMap<>();
            hashMapAtr = new HashMap<>();
            hashMapSpinner = new HashMap<>();
            hashMapSpinnerJson = new HashMap<>();
            hashMapCheckBox = new HashMap<>();
            hashMapCheckBoxId = new HashMap<>();
            hashMapCheckBoxJson = new HashMap<>();

            //Ciclo que recorre el arreglo de objetos Json que contiene
            //la informacion de cada atributo.
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObjectInfo = jsonArray.getJSONObject(i);
                switch (jsonObjectInfo.getString("plaVisConEstAtrRelTip")) {
                    case ("DB"):
                        //Crea un dato basico.
                        crearDatosBasicos(jsonObjectInfo, id_cont, layout);
                        break;
                    case ("Est"):
                        //Crea un dato basico.
                        //crearEst(jsonObjectInfo, layout);
                        break;
                    case ("Atr"):
                        //crea un atributo.
                        crearAtr(jsonObjectInfo, layout);
                        break;
                }
            }
            //Agrega este nuevo layout creado al layout principal de la actividad
            //una vez haya terminado el llenado dinamico.

            LinearLayout layoutHorizontal = new LinearLayout(context);
            layoutHorizontal.setOrientation(LinearLayout.HORIZONTAL);
            layoutHorizontal.setWeightSum(2);

            //Boton descartar
            Button buttonDesc = new Button(NuevoContenido.this);
            buttonDesc.setText("Descartar");
            LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT);
            llp.setMargins(50, 40, 50, 20);
            llp.weight = 1;
            buttonDesc.setLayoutParams(llp);
            buttonDesc.setBackgroundResource(R.drawable.button_background);
            buttonDesc.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    descartarFicha(id_cont);
                }
            });

            //Boton guardar
            Button button = new Button(NuevoContenido.this);
            button.setText("Guardar");


            button.setLayoutParams(llp);
            button.setBackgroundResource(R.drawable.button_background);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    crearFormularioDinamico(json);
                }
            });

            layoutHorizontal.addView(buttonDesc);
            layoutHorizontal.addView(button);
            layout.addView(layoutHorizontal);
            mainLayout.addView(layout);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void descartarFicha(String id_cont) {
        loading = ProgressDialog.show(context, "Descartando contenido.", "Por favor, espere...", false, false);
        String urlBusqueda = prefs.getString(Config.URL_SHARED, "no disp") + "/exp/exp_modulo294.php";
        //Genera la estructura XML y la guarda en un StringWriter.
        StringWriter writer = generarXmlDescartarFicha(id_cont);

        Call<Idatum> idatumCall = ApiUtilsXML.obtenerClienteXml().xmlWebService(urlBusqueda, "proceso_webservice", writer.toString());
        idatumCall.enqueue(new Callback<Idatum>() {
            @Override
            public void onResponse(Call<Idatum> call, Response<Idatum> response) {
                //Si la respuesta fue un exito.
                if (response.isSuccessful()) {
                    String estadoTexto = response.body().getContenidos().get(0).getRespuesta().getEstado();
                    String mensajeTexto = response.body().getContenidos().get(0).getRespuesta().getMensaje();
                    loading.dismiss();

                    //Evalua si el estado es igual a 1, significa que hubo error.
                    if (estadoTexto.equals("1")) {
                        Toast.makeText(context, "Error mensaje: " + mensajeTexto, Toast.LENGTH_LONG).show();
                    }
                    //Evalua si el estado es igual a 0, significa que se completo exitosamente.
                    if (estadoTexto.equals("0")) {
                        finish();
                    }
                } else {
                    loading.dismiss();
                    Toast.makeText(context, "Error, intente de nuevo", Toast.LENGTH_SHORT).show();
                }
            }

            /**
             * Se ejecuta en caso de que se genere algun error.
             * @param call Es la llamada POST
             * @param t Contiene la informacion del error.
             */
            @Override
            public void onFailure(Call<Idatum> call, Throwable t) {
                loading.dismiss();
                Toast.makeText(context, "Error: " + t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private StringWriter generarXmlDescartarFicha(String id_cont) {
        StringWriter writer;
        XmlDoc xmlDoc = null;
        try {
            xmlDoc = new XmlDoc();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }

        //Extrae el usuario y contraseña logueado actualmente en el sistema.
        String usuario = prefs.getString(Config.USUARIO_SHARED, "no disponible");
        String contraseña = prefs.getString(Config.CLAVE_SHARED, "no es posible");


        //hace la llamada al metodo para generar el XML y le pasa
        // todos los parametros necesarios para esta accion.
        writer = xmlDoc.generateXML("",
                "3",
                usuario,
                contraseña,
                id_cont,
                "",
                null,
                null,
                prefs.getString(Config.ID_TIPO_CONT_SHARED_, ""),
                "",
                "",
                "",
                "",
                "",
                new ArrayList<AtributosDinamicos>());

        return writer;
    }

    private void crearEst(JSONObject jsonObjectInfo, LinearLayout layout) {

        try {
            TextView textView = new TextView(NuevoContenido.this);
            LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            llp.setMargins(10, 0, 0, 0); // llp.setMargins(left, top, right, bottom);
            textView.setLayoutParams(llp);
            textView.setText(jsonObjectInfo.getString("nomCEAR"));

            //Crea un Spinner.
            Spinner spinner = new Spinner(NuevoContenido.this, Spinner.MODE_DIALOG);
            spinner.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));


            layout.addView(textView);
            layout.addView(spinner);
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    /**
     * Crea la vista de los atributos, dependiendo del tipo de atributo.
     *
     * @param jsonObjectInfo Objeto Json con la informacion del atributo actual.
     * @param layout         Es el layout donde se insertan todas las vistas creadas de manera dinamica.
     */
    private void crearAtr(JSONObject jsonObjectInfo, LinearLayout layout) {
        try {
            //Es el objeto JSON que contiene los datos de cada atributo.
            JSONObject jsonObjectDatos = jsonObjectInfo.getJSONObject("datos");

            //Evalua el tipo de atributo para crear su vista correspondiente.
            switch (jsonObjectDatos.getInt("atributoTipo")) {
                case Config.GC_ATRIBUTO_TIPO_NUMERICO:
                    crearAtrTipoNumerico(jsonObjectInfo, jsonObjectDatos, layout);
                    break;
                case Config.GC_ATRIBUTO_TIPO_CONCEPTO:
                    crearAtrTipoConcepto(jsonObjectInfo, jsonObjectDatos, layout);
                    break;
                case Config.GC_ATRIBUTO_TIPO_ATRIBUTO:
                    crearAtrTipoAtributo(jsonObjectInfo, jsonObjectDatos, layout);
                    break;
                case Config.GC_ATRIBUTO_TIPO_CALCULABLE:
                    break;
                case Config.GC_ATRIBUTO_TIPO_FECHA:
                    crearAtrTipoFecha(jsonObjectInfo, jsonObjectDatos, layout);
                    break;
                case Config.GC_ATRIBUTO_TIPO_ARCHIVO:
                    break;
                case Config.GC_ATRIBUTO_TIPO_ENRIQUECIDO:
                    break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Crea la vista del atributo tipo fecha, en este caso se genera un EditText
     * y un DatePicker para elegir la fecha correspondiente.
     *
     * @param jsonObjectInfo  Objeto Json con la informacion del atributo actual.
     * @param jsonObjectDatos Es el objeto JSON que contiene los datos de cada atributo.
     * @param layout          Es el layout donde se insertan todas las vistas creadas de manera dinamica.
     */
    private void crearAtrTipoFecha(JSONObject jsonObjectInfo, JSONObject jsonObjectDatos, LinearLayout layout) {

        try {

            TextInputLayout textInputLayout = new TextInputLayout(NuevoContenido.this);
            textInputLayout.setLayoutParams(new TextInputLayout.LayoutParams(TextInputLayout.LayoutParams.MATCH_PARENT, TextInputLayout.LayoutParams.WRAP_CONTENT));
            final TextInputEditText editText = new TextInputEditText(NuevoContenido.this);
            textInputLayout.setHintTextAppearance(R.style.income);

            //Guarda la referencia del editText en un hasmap donde
            //la clave es el id del atributo.
            hashMapAtr.put(jsonObjectDatos.getInt("atributoId"), editText);

            editText.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            editText.setGravity(Gravity.LEFT);
            editText.setSingleLine();
            editText.setFocusable(false);
            editText.setClickable(true);
            editText.setHint(jsonObjectInfo.getString("nomCEAR"));

            //Si encuentra un valor por defecto, lo muestra en el editText.
            if (jsonObjectDatos.getString("atrValDef") != "") {
                editText.setText(jsonObjectDatos.getString("atrValDef"));
            }

            editText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Muestra la fecha actual en el datepicker.
                    Calendar mcurrentDate = Calendar.getInstance();
                    int mYear = mcurrentDate.get(Calendar.YEAR);
                    int mMonth = mcurrentDate.get(Calendar.MONTH);
                    int mDay = mcurrentDate.get(Calendar.DAY_OF_MONTH);

                    //Genera el DatePicker y al confirmar escribe la fecha elegida
                    //en el editText.
                    DatePickerDialog mDatePicker = new DatePickerDialog(NuevoContenido.this, new DatePickerDialog.OnDateSetListener() {
                        public void onDateSet(DatePicker datepicker, int selectedyear, int selectedmonth, int selectedday) {
                            editText.setText(selectedday + "-" + selectedmonth + "-" + selectedyear);
                        }
                    }, mYear, mMonth, mDay);

                    mDatePicker.getDatePicker().setSpinnersShown(true);

                    mDatePicker.getDatePicker().setCalendarViewShown(false);
                    mDatePicker.setTitle("Selecciona Fecha");
                    mDatePicker.show();
                }
            });
            //Agrega las vistas "hijos" al "padre".
            textInputLayout.addView(editText);
            layout.addView(textInputLayout);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Crea la vista del atributo tipo concepto, hay dos opciones,
     * para ello evalua "atributoMultiple", si es true es de tipo
     * concepto multiple y si es false es de tipo concepto simple.
     * multiple = CheckBox.
     * simple = Spinner.
     *
     * @param jsonObjectInfo  Objeto Json con la informacion del atributo actual.
     * @param jsonObjectDatos Es el objeto JSON que contiene los datos de cada atributo.
     * @param layout          Es el layout donde se insertan todas las vistas creadas de manera dinamica.
     */
    private void crearAtrTipoConcepto(JSONObject jsonObjectInfo, JSONObject jsonObjectDatos, LinearLayout layout) {
        try {
            LinearLayout layout2 = new LinearLayout(
                    NuevoContenido.this);
            layout2.setOrientation(LinearLayout.VERTICAL);


            //evalua esa condicion para determinar si es un atributo conceptual multiple.
            if (jsonObjectDatos.getBoolean("atributoMultiple") == true) {

                buscarDatosCheckBox(jsonObjectInfo.getString("plaVisConEstAtrRelTip"),
                        jsonObjectDatos.getInt("atributoTipo"),
                        jsonObjectDatos.getInt("atributoId"),
                        layout2,
                        jsonObjectDatos);

                layout.addView(layout2);
            }
            //Condicion para generar un Spinner.
            else {
                //
                TextView textView = new TextView(NuevoContenido.this);
                LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                llp.setMargins(10, 0, 0, 0); // llp.setMargins(left, top, right, bottom);
                textView.setLayoutParams(llp);
                textView.setText(jsonObjectDatos.getString("atributoNomb"));

                //Crea un Spinner.
                Spinner spinner = new Spinner(NuevoContenido.this, Spinner.MODE_DIALOG);
                spinner.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));

                //Busca los datos para poblar de informacion el spinner.
                buscarListaDatosSelector(jsonObjectInfo.getString("plaVisConEstAtrRelTip"),
                        jsonObjectDatos.getInt("atributoTipo"),
                        jsonObjectDatos.getInt("atributoId"), spinner, jsonObjectDatos);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    spinner.setDropDownHorizontalOffset(100);
                }
                //Se agregan las vistas credas al layout
                layout2.addView(textView);
                layout2.addView(spinner);
                layout.addView(layout2);
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }


    }


    /**
     * Busca toda la informacion correspondiente al atributo conceptual
     * multiple que se esta creando, y genera los diferentes checkboxes
     * con sus respectivos valores.
     *
     * @param plaVisConEstAtrRelTip Es el tipo de datos, en este caso es Atributo.
     * @param atributoTipo          Es el tipo de atributo.
     * @param atributoId            Es el id del atributo.
     * @param layout                Es el layout donde se insertan todas las vistas creadas de manera dinamica.
     * @param jsonObjectDatos       Es el objeto JSON que contiene los datos de cada atributo.
     */
    private void buscarDatosCheckBox(String plaVisConEstAtrRelTip, int atributoTipo, int atributoId, final LinearLayout layout, final JSONObject jsonObjectDatos) {

        //Crea la llamada para realizar la peticion http.
        Call<ElenaExcepciones> call = ApiUtils.obtenerCliente()
                .obtenerDatosAtributos(urlText + "/exp/exp_modulo294.php",
                        "em294_datosPorDefectosPvc",
                        plaVisConEstAtrRelTip,
                        atributoTipo,
                        atributoId);

        //Ejecuta la peticion http.
        call.enqueue(new Callback<ElenaExcepciones>() {
            @Override
            public void onResponse(Call<ElenaExcepciones> call, Response<ElenaExcepciones> response) {
                if (response.body().getExe_est_res().equals("true")) {
                    //Manipula la respuesta del servidor.
                    try {
                        ElenaExcepciones elenaExcepciones = response.body();
                        final JSONArray array = new JSONArray(elenaExcepciones.getExe_res());
                        //Crea una referencia para el Json que contiene los datos de los checkboxes
                        //que luego sera necesitada para rellenar la estructura xml.
                        hashMapCheckBoxJson.put(jsonObjectDatos.getInt("atributoId"), array);
                        //Crea un textView con el nombre del atributo.
                        TextView textView = new TextView(NuevoContenido.this);
                        LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        llp.setMargins(10, 0, 0, 0); // llp.setMargins(left, top, right, bottom);
                        textView.setLayoutParams(llp);
                        textView.setText(jsonObjectDatos.getString("atributoNomb"));

                        layout.addView(textView);
                        //Recorre el arreglo de objetos JSON.
                        //Contiene la informacion de los checkBoxes.
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject jsonObject = array.getJSONObject(i);
                            String atributoConceptoNomb = jsonObject.getString("atributoConceptoNomb");
                            //Crea el checkBox y le agrega una referencia para ser recuperado luego
                            //por su id.
                            CheckBox checkBox = new CheckBox(NuevoContenido.this);
                            hashMapCheckBoxId.put(jsonObject.getInt("atributoConceptoId"), checkBox);
                            checkBox.setText(atributoConceptoNomb);
                            if (jsonObject.getString("atrConValDef").equals("true")) {
                                checkBox.setChecked(true);
                            }
                            layout.addView(checkBox);
                        }
                        hashMapCheckBox.put(jsonObjectDatos.getInt("atributoId"), hashMapCheckBoxId);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(NuevoContenido.this, response.body().getExe_men(), Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onFailure(Call<ElenaExcepciones> call, Throwable t) {
                Log.e(TAG, "onFailure: " + t.getLocalizedMessage());
            }
        });
    }

    /**
     * Busca los datos para poblar de informacion el spinner.
     *
     * @param plaVisConEstAtrRelTip Es el tipo de datos, en este caso es Atributo.
     * @param atributoTipo          Es el tipo de atributo.
     * @param atributoId            Es el id del atributo.
     * @param spinner               Es el spinner a poblar de informacion.
     * @param jsonObjectDatos       Es el objeto JSON que contiene los datos de cada atributo.
     */
    private void buscarListaDatosSelector(String plaVisConEstAtrRelTip, int atributoTipo, int atributoId, final Spinner spinner, final JSONObject jsonObjectDatos) {
        //Crea la llamada para realizar la peticion http.
        Call<ElenaExcepciones> call = ApiUtils.obtenerCliente()
                .obtenerDatosAtributos(urlText + "/exp/exp_modulo294.php",
                        "em294_datosPorDefectosPvc",
                        plaVisConEstAtrRelTip,
                        atributoTipo,
                        atributoId);
        //Es la lista de datos tipo String que contiene los items del spinner.
        final ArrayList<String> lista = new ArrayList<String>();
        //Ejecuta la peticion http.
        call.enqueue(new Callback<ElenaExcepciones>() {
            @Override
            public void onResponse(Call<ElenaExcepciones> call, Response<ElenaExcepciones> response) {
                if (response.body().getExe_est_res().equals("true")) {
                    //Manipula la respuesta del servidor.
                    try {
                        ElenaExcepciones elenaExcepciones = response.body();
                        final JSONArray array = new JSONArray(elenaExcepciones.getExe_res());
                        hashMapSpinnerJson.put(jsonObjectDatos.getInt("atributoId"), array);
                        //Recorre el arreglo de objetosJSON.
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject jsonObject = array.getJSONObject(i);
                            String atributoConceptoNomb = jsonObject.getString("atributoConceptoNomb");
                            //int id = jsonObject.getInt("atributoId");

                            //Agrega a la lista cada item encontrado en el objeto JSON.
                            lista.add(atributoConceptoNomb);
                        }
                        //Guarda la referencia del spinner con una clave, el cual es el id del atributo.
                        hashMapSpinner.put(jsonObjectDatos.getInt("atributoId"), spinner);


                        //Configura el adaptador para mostrar la lista de items en elspinner.
                        spinner.setAdapter(new ArrayAdapter<String>(NuevoContenido.this, android.R.layout.simple_spinner_item, lista));

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(NuevoContenido.this, response.body().getExe_men(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ElenaExcepciones> call, Throwable t) {
                lista.add("Error al cargar los datos");
                spinner.setAdapter(new ArrayAdapter<String>(NuevoContenido.this, android.R.layout.simple_spinner_item, lista));
            }
        });

    }

    /**
     * Crea el atributo de tipo Atributo.
     *
     * @param jsonObjectInfo  Objeto Json con la informacion del atributo actual.
     * @param jsonObjectDatos Es el objeto JSON que contiene los datos de cada atributo.
     * @param layout          Es el layout donde se insertan todas las vistas creadas de manera dinamica.
     */
    private void crearAtrTipoAtributo(JSONObject jsonObjectInfo, JSONObject jsonObjectDatos, LinearLayout layout) {
        try {
            //Genera el campo de texto.
            TextInputLayout textInputLayout = new TextInputLayout(NuevoContenido.this);
            textInputLayout.setLayoutParams(new TextInputLayout.LayoutParams(TextInputLayout.LayoutParams.MATCH_PARENT, TextInputLayout.LayoutParams.WRAP_CONTENT));
            TextInputEditText editText = new TextInputEditText(NuevoContenido.this);
            textInputLayout.setHintTextAppearance(R.style.income);

            //guarda la referencia del editText con clave id de atributo.
            hashMapAtr.put(jsonObjectDatos.getInt("atributoId"), editText);

            //Se evalua la siguiente condicion para hacer que el campo de texto
            //sea largo.
            if (jsonObjectDatos.getBoolean("atrTexLar") == true) {
                editText.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));
            }
            //Se configura el campo de texto para que sea corto.
            else {
                editText.setLayoutParams(new LinearLayout.LayoutParams(
                        440,
                        LinearLayout.LayoutParams.WRAP_CONTENT));
                editText.setTextSize(10);
            }
            editText.setGravity(Gravity.LEFT);
            editText.setSingleLine();
            editText.setHint(jsonObjectInfo.getString("nomCEAR"));
            //Si trae algun valor por defecto, es configurado en el campo de texto.
            if (jsonObjectDatos.getString("atrValDef") != "") {
                editText.setText(jsonObjectDatos.getString("atrValDef"));
            }
            //Agrega las vistas "hijos" a las vistas "padres".
            textInputLayout.addView(editText);
            layout.addView(textInputLayout);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Crea un atributo de tipo numerico.
     *
     * @param jsonObjectInfo  Objeto Json con la informacion del atributo actual.
     * @param jsonObjectDatos Es el objeto JSON que contiene los datos de cada atributo.
     * @param layout          Es el layout donde se insertan todas las vistas creadas de manera dinamica.
     */
    private void crearAtrTipoNumerico(JSONObject jsonObjectInfo, JSONObject jsonObjectDatos, LinearLayout layout) {

        try {
            TextInputLayout textInputLayout = new TextInputLayout(NuevoContenido.this);
            textInputLayout.setLayoutParams(new TextInputLayout.LayoutParams(TextInputLayout.LayoutParams.MATCH_PARENT, TextInputLayout.LayoutParams.WRAP_CONTENT));
            TextInputEditText editText = new TextInputEditText(NuevoContenido.this);
            textInputLayout.setHintTextAppearance(R.style.income);
            hashMapAtr.put(jsonObjectDatos.getInt("atributoId"), editText);

            editText.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            editText.setGravity(Gravity.LEFT);

            //set filter
            //Log.d(TAG, jsonObjectInfo.getString("nomCEAR"));
            if (!jsonObjectDatos.isNull("atributoMin") && !jsonObjectDatos.isNull("atributoMax")) {
                editText.setFilters(new InputFilter[]{new MinMaxFilter(jsonObjectDatos.getString("atributoMin"),
                        jsonObjectDatos.getString("atributoMax"))});
            }
            //configura el metodo de entrada para que sea numerico.
            editText.setInputType(InputType.TYPE_CLASS_NUMBER);
            editText.setSingleLine();
            editText.setHint(jsonObjectInfo.getString("nomCEAR"));
            if (jsonObjectDatos.getString("atrValDef") != "") {
                editText.setText(jsonObjectDatos.getString("atrValDef"));
            }

            textInputLayout.addView(editText);

            layout.addView(textInputLayout);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Crea los campos de texto para los diferentes Datos Basicos.
     *
     * @param jsonObjectInfo Objeto Json con la informacion del atributo actual.
     * @param id_cont        Es el id de contenido que se esta creando.
     * @param layout         Es el layout donde se insertan todas las vistas creadas de manera dinamica.
     */
    private void crearDatosBasicos(JSONObject jsonObjectInfo, String id_cont, LinearLayout layout) {
        try {
            TextInputLayout textInputLayout = new TextInputLayout(NuevoContenido.this);
            textInputLayout.setLayoutParams(new TextInputLayout.LayoutParams(TextInputLayout.LayoutParams.MATCH_PARENT, TextInputLayout.LayoutParams.WRAP_CONTENT));
            TextInputEditText editText = new TextInputEditText(NuevoContenido.this);
            textInputLayout.setHintTextAppearance(R.style.income);
            //Toast.makeText(context, jsonObjectInfo.getString("plaVisConEstAtrRelId"), Toast.LENGTH_SHORT).show(); ;

            //Crea la referencia de cada editText creado con la clave que sera el nombre de cado dato basico.
            hashMapDB.put(jsonObjectInfo.getString("plaVisConEstAtrRelDatBas"), editText);

            //Si el dato basico es un id o un codigo institucional, su campo de texto sera pequeño.
            if (jsonObjectInfo.getString("plaVisConEstAtrRelDatBas").equals("objeto_id") || jsonObjectInfo.getString("plaVisConEstAtrRelDatBas").equals("objeto_codigo")) {
                editText.setLayoutParams(new LinearLayout.LayoutParams(
                        200,
                        LinearLayout.LayoutParams.WRAP_CONTENT));
            }
            //Si no, sera largo.
            else {
                editText.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));
            }
            editText.setGravity(Gravity.LEFT);
            editText.setSingleLine();
            editText.setHint(jsonObjectInfo.getString("nomCEAR"));

            //Si el dato basico a crear es el id de contenido,
            //Se colocara el id en ese campo de texto y se configura
            //para que no sea editable este campo.
            if (jsonObjectInfo.getString("plaVisConEstAtrRelDatBas").equals("objeto_id")) {
                editText.setText(id_cont);
                editText.setTextColor(Color.RED);
                editText.setFocusable(false);
                editText.setClickable(false);
            }
            textInputLayout.addView(editText);
            layout.addView(textInputLayout);

        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    /**
     * Inicia el proceso para generar la estructura dinamica XML.
     *
     * @param json Es la estructura del PVC en formato JSON.
     */
    private void crearFormularioDinamico(String json) {
        //Crea una lista de objetos que contendra todos los atributos necesarios.
        ArrayList<AtributosDinamicos> list = new ArrayList<>();
        ObjetoDB objetoDB = new ObjetoDB();

        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray jsonArray = jsonObject.getJSONArray("plaVisConEstAtrRels");

            //Recorre el arreglo de objetos JSON que contiene la informacion de cada atributo.
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObjectInfo = jsonArray.getJSONObject(i);

                switch (jsonObjectInfo.getString("plaVisConEstAtrRelTip")) {
                    case ("DB"):
                        encontrarReferenciaCamposDB(jsonObjectInfo, objetoDB);
                        break;

                    case ("Atr"):
                        encontrarReferenciaCamposAtr(jsonObjectInfo, list);
                        break;
                }
            }

            agregarNuevoContenido(list, objetoDB);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Encuentra las refenrencias de cada EditText, spinner y checkBox que coreesponda a cada Atributo.
     *
     * @param jsonObjectInfo Objeto Json con la informacion del atributo actual.
     * @param list           Es la lista de objetos de Atributos Dinamicos donde se guardara toda la informacion recolectada.
     */
    private void encontrarReferenciaCamposAtr(JSONObject jsonObjectInfo, ArrayList<AtributosDinamicos> list) {

        try {
            JSONObject jsonObjectDatos = jsonObjectInfo.getJSONObject("datos");

            // Si el tipo de atributo es numerico(1) o o atributo(3) extrae sus valores y crea el objeto atributo dinamico.
            if (jsonObjectDatos.getInt("atributoTipo") == Config.GC_ATRIBUTO_TIPO_NUMERICO ||
                    jsonObjectDatos.getInt("atributoTipo") == Config.GC_ATRIBUTO_TIPO_ATRIBUTO) {
                //Crea el objeto
                AtributosDinamicos atributoDinamico = new AtributosDinamicos();
                int id = jsonObjectDatos.getInt("atributoId");
                //Extrae el editText correspondiente al id del atributo.
                EditText valorText = hashMapAtr.get(id);

                //Guarda los valores correspondientes.
                atributoDinamico.setId(id);
                atributoDinamico.setTipo(jsonObjectDatos.getInt("atributoTipo"));
                atributoDinamico.setValor(valorText.getText().toString());
                atributoDinamico.setFecha("");
                //Lo agrega a la lista de objetos.
                list.add(atributoDinamico);

            }

            //Si el atributo es de tipo Fecha.
            if (jsonObjectDatos.getInt("atributoTipo") == Config.GC_ATRIBUTO_TIPO_FECHA) {
                //Crea el objeto.
                AtributosDinamicos atributoDinamico = new AtributosDinamicos();
                int id = jsonObjectDatos.getInt("atributoId");
                //Extrae el editText correspondiente.
                EditText valorText = hashMapAtr.get(id);
                //Log.d("NuevoContenido", "valoresAtrNumerico: " + valorText.getText().toString());
                atributoDinamico.setId(id);
                atributoDinamico.setTipo(jsonObjectDatos.getInt("atributoTipo"));
                atributoDinamico.setFecha(valorText.getText().toString());
                atributoDinamico.setValor("");

                //Agrega el objeto a la lista.
                list.add(atributoDinamico);
            }

            //si el atributo es de tipo concepto.
            if (jsonObjectDatos.getInt("atributoTipo") == Config.GC_ATRIBUTO_TIPO_CONCEPTO) {
                //Atributos Multiples, checkboxes.
                if (jsonObjectDatos.getBoolean("atributoMultiple") == true) {

                    int id = jsonObjectDatos.getInt("atributoId");
                    HashMap<Integer, CheckBox> hashMap = hashMapCheckBox.get(id);

                    //Extrae el arreglo de Json de datos relacionados con el checkbox actual.
                    JSONArray array = hashMapCheckBoxJson.get(id);

                    AtributosDinamicos atributoDinamico = new AtributosDinamicos();
                    atributoDinamico.setId(id);
                    atributoDinamico.setTipo(jsonObjectDatos.getInt("atributoTipo"));
                    atributoDinamico.setFecha("");
                    atributoDinamico.setMultiple(true);

                    List<AtributosDinamicos.AtributosConceptualMultiple> listAtributosMultiple = new ArrayList<>();
                    //Recorre el arreglo de objetos JSON que contiene la informacion.
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject jsonObject = array.getJSONObject(i);
                        CheckBox checkBox = hashMap.get(jsonObject.getInt("atributoConceptoId"));


                        //Solo guardara los valores si el checkBox esta marcado.
                        if (checkBox.isChecked()) {

                            AtributosDinamicos.AtributosConceptualMultiple atributosConceptualMultiple = new AtributosDinamicos.AtributosConceptualMultiple();
                            String atributoConceptoValor = String.valueOf(jsonObject.getInt("atributoConceptoValor"));
                            //Log.e("NuevoContenido", "CheckBoxSelected: " + atributoConceptoValor);
                            atributosConceptualMultiple.setAtributoValor(atributoConceptoValor);
                            listAtributosMultiple.add(atributosConceptualMultiple);
                            //Agrega el objeto a la lista.

                        }
                    }
                    atributoDinamico.setList(listAtributosMultiple);
                    list.add(atributoDinamico);
                }
                //Atributos simples, spinners.
                else {
                    int id = jsonObjectDatos.getInt("atributoId");
                    Spinner spinner = hashMapSpinner.get(id);

                    //Validar que el spinner tenga algun valor y no sea nulo.
                    if (spinner.getSelectedItem() != null) {
                        AtributosDinamicos atributoDinamico = new AtributosDinamicos();
                        atributoDinamico.setId(id);
                        atributoDinamico.setTipo(jsonObjectDatos.getInt("atributoTipo"));
                        //Extrae el arreglo de Json de datos relacionados con el spinner actual.
                        JSONArray array = hashMapSpinnerJson.get(id);
                        //Recorre el arreglo de objetos JSON que contiene la informacion de los items del spinner.
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject jsonObject = array.getJSONObject(i);
                            String atributoConceptoNomb = jsonObject.getString("atributoConceptoNomb");
                            //Condicion que se debe cumplir para guardar el valor correspondiente al nombre elegido en el spinner.
                            if (atributoConceptoNomb.equals(spinner.getSelectedItem().toString())) {
                                int atributoConceptoValor = jsonObject.getInt("atributoConceptoValor");
                                atributoDinamico.setValor(String.valueOf(atributoConceptoValor));
                                atributoDinamico.setFecha("");
                                Log.e("NuevoContenido", "Selected: " + atributoConceptoValor);
                            }
                        }
                        //agrega el objeto a la ficha.
                        list.add(atributoDinamico);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Encuentra las referencias de cada editText que corresponda a los Datos Basicos.
     * los valores configurados.
     *
     * @param jsonObjectInfo Objeto Json con la informacion del atributo actual.
     * @param objetoDB       Es el objeto que se usa para guardar cada uno de los Datos Basicos.
     */
    private void encontrarReferenciaCamposDB(JSONObject jsonObjectInfo, ObjetoDB objetoDB) {
        try {
            objetoDB.setObjeto_id(objeto_id);
            String nombre = jsonObjectInfo.getString("plaVisConEstAtrRelDatBas");
            EditText editText = hashMapDB.get(nombre);
            switch (nombre) {
                case "objeto_codigo":
                    objetoDB.setObjeto_codigo(editText.getText().toString());
                    break;
                case "objeto_nomb":
                    objetoDB.setObjeto_nomb(editText.getText().toString());
                    break;
                case "objeto_desc":
                    objetoDB.setObjeto_desc(editText.getText().toString());
                    break;
                case "objeto_obs":
                    objetoDB.setObjeto_obs(editText.getText().toString());
                    break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Una vez teniendo toda la informacion necesaria, se procede a generar la estructura XML
     * para crear el nuevo contenido
     *
     * @param list     Es la lista de objetos de Atributos Dinamicos.
     * @param objetoDB Es el objeto que contiene todos los Datos Basicos.
     */
    private void agregarNuevoContenido(ArrayList<AtributosDinamicos> list, ObjetoDB objetoDB) {
        loading = ProgressDialog.show(context, "Guardando cambios.", "Por favor, espere...", false, false);
        String urlBusqueda = prefs.getString(Config.URL_SHARED, "no disp") + "/exp/exp_modulo294.php";
        //Genera la estructura XML y la guarda en un StringWriter.
        StringWriter writer = generarXmlCrearFicha(list, objetoDB);

        Call<Idatum> idatumCall = ApiUtilsXML.obtenerClienteXml().xmlWebService(urlBusqueda, "proceso_webservice", writer.toString());
        idatumCall.enqueue(new Callback<Idatum>() {
            @Override
            public void onResponse(Call<Idatum> call, Response<Idatum> response) {
                //Si la respuesta fue un exito.
                if (response.isSuccessful()) {
                    String estadoTexto = response.body().getContenidos().get(0).getRespuesta().getEstado();
                    String mensajeTexto = response.body().getContenidos().get(0).getRespuesta().getMensaje();
                    loading.dismiss();

                    //Evalua si el estado es igual a 1, significa que hubo error.
                    if (estadoTexto.equals("1")) {
                        Toast.makeText(context, "Error mensaje: " + mensajeTexto, Toast.LENGTH_LONG).show();
                    }
                    //Evalua si el estado es igual a 0, significa que se completo exitosamente.
                    if (estadoTexto.equals("0")) {
                        //Snackbar.make(findViewById(R.id.linearNuevoContenido), "Ficha de Contenidos creada exitosamente",
                        //    Snackbar.LENGTH_SHORT)
                        //.show();
                        finish();
                        //goToMain();
                    }
                } else {
                    loading.dismiss();
                    Toast.makeText(context, "Error, intente de nuevo", Toast.LENGTH_SHORT).show();
                }
            }

            /**
             * Se ejecuta en caso de que se genere algun error.
             * @param call Es la llamada POST
             * @param t Contiene la informacion del error.
             */
            @Override
            public void onFailure(Call<Idatum> call, Throwable t) {
                loading.dismiss();
                Toast.makeText(context, "Error: " + t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    /**
     * Genera la estructura XML que se comunicara con el WebService.
     *
     * @param list     Es la lista de objetos de Atributos Dinamicos.
     * @param objetoDB Es el objeto que contiene todos los Datos Basicos.
     * @return La estructura XML.
     */
    private StringWriter generarXmlCrearFicha(ArrayList<AtributosDinamicos> list, ObjetoDB objetoDB) {
        StringWriter writer;
        XmlDoc xmlDoc = null;
        try {
            xmlDoc = new XmlDoc();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }

        //Extrae el usuario y contraseña logueado actualmente en el sistema.
        String usuario = prefs.getString(Config.USUARIO_SHARED, "no disponible");
        String contraseña = prefs.getString(Config.CLAVE_SHARED, "no es posible");


        if (objetoDB.getObjeto_nomb() == null) {
            objetoDB.setObjeto_nomb("");
        }
        if (objetoDB.getObjeto_desc() == null) {
            objetoDB.setObjeto_desc("");
        }
        if (objetoDB.getObjeto_codigo() == null) {
            objetoDB.setObjeto_codigo("");
        }
        if (objetoDB.getObjeto_obs() == null) {
            objetoDB.setObjeto_obs("");
        }
        //hace la llamada al metodo para generar el XML y le pasa
        // todos los parametros necesarios para esta accion.
        writer = xmlDoc.generateXML("",
                "2",
                usuario,
                contraseña,
                objetoDB.getObjeto_id(),
                "",
                null,
                null,
                prefs.getString(Config.ID_TIPO_CONT_SHARED_, ""),
                "",
                objetoDB.getObjeto_nomb(),
                objetoDB.getObjeto_desc(),
                objetoDB.getObjeto_codigo(),
                objetoDB.getObjeto_obs(),
                list);

        return writer;
    }

    /**
     * Metodo usado para ir a la actividad principal y eliminar la actividad actual.
     */
    private void goToMain() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
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
}

