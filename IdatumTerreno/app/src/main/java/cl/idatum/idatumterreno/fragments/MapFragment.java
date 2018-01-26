package cl.idatum.idatumterreno.fragments;


import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.TileProvider;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.maps.android.data.kml.KmlLayer;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import cl.idatum.idatumterreno.R;
import cl.idatum.idatumterreno.adapters.CustomSpinnerAdapterMap;
import cl.idatum.idatumterreno.api.ApiUtils;
import cl.idatum.idatumterreno.api.ApiUtilsXML;
import cl.idatum.idatumterreno.elena.Constante;
import cl.idatum.idatumterreno.elena.ElenaExcepciones;
import cl.idatum.idatumterreno.models.Contenidos;
import cl.idatum.idatumterreno.response.Idatum;
import cl.idatum.idatumterreno.utils.AtributosDinamicos;
import cl.idatum.idatumterreno.utils.Config;
import cl.idatum.idatumterreno.utils.CubiertaObject;
import cl.idatum.idatumterreno.utils.OSMTileProvider;
import cl.idatum.idatumterreno.utils.WMSTileProviderGoogle;
import cl.idatum.idatumterreno.utils.XmlDoc;
import io.realm.Realm;
import io.realm.RealmResults;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Fragmento de mapa donde se encuentran las funcionalidades de busqueda de tareas, georreferenciacion de fichas, y otras.
 *
 * @author Jesus Cañizales <jcanizales@siigsa.cl>
 * @version 2
 * @package cl.idatum.idatumterreno Proyecto IdatumTerreno
 * @link http://www.siigsa.cl
 * @copyright SIIGSA, Propiedad Intelectual y Derechos Patrimoniales de Software y Base de Datos i-datum. Registro Propiedad Intelectual Nº 211.351 y 211.352 respectivamente, con fecha 22 de noviembre del 2011
 * @since 25/10/2017
 */
public class MapFragment extends Fragment implements View.OnClickListener, OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    private Context context;
    private GoogleMap mMap;
    private MapView mapView;
    private View rootView;
    private final static int MY_PERMISSION_FINE_LOCATION = 101;
    private Double myLatitude = null;
    private Double myLongitude = null;
    private Double myLatitudeMarker = null;
    private Double myLongitudeMarker = null;
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    protected static final String TAG = "MapFragment";
    private Marker marker;
    private ProgressDialog loading;
    private SharedPreferences sharedPreferences;
    private FusedLocationProviderClient mFusedLocationClient;
    private FloatingActionButton fabTipoMapa, actual, web, buscarTarea, guardarCoorde, fabTools, borrar, contenidoLocal, botonKml;
    private Animation fabRotate1, fabRotate2, fabOpen, fabClose;
    private Boolean isOpen = false;
    private TextView resultadoCont;
    private PopupWindow popupWindow;
    private LayoutInflater layoutInflater;
    private CardView cardView;
    private CoordinatorLayout coordinatorLayout;
    private int tipoTarea;
    private String id_usuario, id_relacion, id_obj_cont, id_tipo_cont, id_tarea, id_obj_cont_interno, nombre_objeto;
    private String id_obj_cont_local, id_usuario_local;
    Realm realm;
    private RealmResults<Contenidos> contenidos;
    private JSONArray jsonArrayCubiertas, jsonArrayMapaBase;
    private Boolean abrirPopUp = false;
    private TileOverlay tileOverlay;
    private String nombreMapaActual;
    private TileOverlay osmTileOverlay;
    private List<String> listKml;
    private KmlLayer kmlLayer;
    private ArrayList<CubiertaObject> kmlObjectArrayList;
    private ArrayList<CubiertaObject> wmsCubiertas;
    private ArrayList<String> wmsList;
    private List<String> list2;
    private ArrayList<String> listCubDef;
    private final static int WRITE_EXTERNAL_PERMISSION_REQUEST_CODE = 34;

    //El constructor de la clase que se requiere vacio.
    public MapFragment() {

    }

    /**
     * Metodo principal de la clase.
     *
     * @param savedInstanceState
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        context = getActivity();
        super.onCreate(savedInstanceState);
    }

    /**
     * Crea la vista del fragmento
     *
     * @param inflater           Infla la vista.
     * @param container          Es el contenedor del fragmento
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_map, container, false);
        return rootView;
    }

    /**
     * Se ejecuta al crearse la vista del fragmento.
     *
     * @param view               Es la vista creada.
     * @param savedInstanceState
     */
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mapView = rootView.findViewById(R.id.map);
        if (mapView != null) ;
        {
            mapView.onCreate(null);
            mapView.onResume();
            mapView.getMapAsync(this);
        }
        //Referencia todas las vistas para trabajar con ellas en java
        iniciarVistas();


        if (getArguments() != null) {
            evaluarArguments();
        }

        construirGoogleApiClient();
        construirLocationRequest();
        checkFabTools();

        fabTipoMapa.setOnClickListener(this);
        botonKml.setOnClickListener(this);
        fabTools.setOnClickListener(this);
        actual.setOnClickListener(this);
        borrar.setOnClickListener(this);
        web.setOnClickListener(this);
        buscarTarea.setOnClickListener(this);
        contenidoLocal.setOnClickListener(this);
        guardarCoorde.setOnClickListener(this);
    }


    private void evaluarArguments() {
        //Log.e(TAG, getArguments().getString("tipoTarea") + " - " + getArguments().getString("id_contenido"));
        if (getArguments().get("tipotarea") != null) {
            buscarTareaMetodo();
        }
        if (getArguments().get("id") != null) {
            resultadoCont.setText("Ficha a georreferenciar: \n" + getArguments().get("id").toString() + " - " + getArguments().get("nombre") + "\nTarea Manual");
            cardView.setVisibility(View.VISIBLE);
            //resultadoCont.setVisibility(View.VISIBLE);
            id_obj_cont_interno = getArguments().get("id").toString();
            tipoTarea = 1;
        }
        if ("3".equals(getArguments().getString("tipoTarea"))) {
            tipoTarea = 3;
            id_obj_cont_local = getArguments().getString("id_contenido");
            id_usuario_local = getArguments().getString("id_usuario");
            resultadoCont.setText("Ficha a georreferenciar: \n" + id_obj_cont_local + " - " + getArguments().get("nombre_contenido") + "\nTarea Local");
            cardView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Se ejecuta cada vez que una vista a la que se le agrego un click listener es presionado.
     */
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.botonKml:
                checkPermisosSD();

                break;
            case R.id.fabTipoMapa:
                if (sharedPreferences.getString(Config.ID_MAPA, "") != "") {
                    if (jsonArrayCubiertas != null && jsonArrayMapaBase != null) {
                        abrirPopUpWindow();
                    } else {
                        Toast.makeText(context, "Error al cargar info del mapa, abra y cierre la aplicacion.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(context, "Debe configurar un mapa.", Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.fabTools:
                if (isOpen) {
                    cerrarBotones();
                } else {
                    abrirbotones();
                }
                break;
            case R.id.actual:
                marcarActual();
                checkCondicionesBotonGuardar();
                break;
            case R.id.borrar:
                irBorrar();
                checkCondicionesBotonGuardar();
                break;
            case R.id.web:
                irWeb();
                break;
            case R.id.buscarTarea:
                if (sharedPreferences.getBoolean(Config.LOGIN_SHARED, false))
                    buscarTareaMetodo();
                else Toast.makeText(context, "Debe ingresar al sistema", Toast.LENGTH_SHORT).show();
                break;
            case R.id.contenidoLocal:
                buscarContenidosLocales();
                break;
            case R.id.guardarCoorde:
                evaluarTipoTarea();
                break;
        }
    }

    private void checkPermisosSD() {
        int writeExternalStoragePermissionCheck = ContextCompat.checkSelfPermission(context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (writeExternalStoragePermissionCheck == PackageManager.PERMISSION_GRANTED) {
            listarArchivosKml();
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_EXTERNAL_PERMISSION_REQUEST_CODE);
            }
        }
    }

    /**
     * Genera el pop up que lista los archivos KML contenidos en la carpeta de idatum mobile.
     *
     * @param listKml            Es la lista que contiene los nombres de los archivos .kml.
     * @param kmlObjectArrayList Es la lista de ObjetosKml que contiene la informacion del archivo .kml
     *                           es decir si esta activa o no, el nombre del archivo, y la cubierta kmlLayer.
     */
    private void abrirPopUpWindowKml(final List<String> listKml, final ArrayList<CubiertaObject> kmlObjectArrayList) {

        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View activityCapasMapa = layoutInflater.inflate(R.layout.kml_list, null);

        final ListView listViewKml = activityCapasMapa.findViewById(R.id.kmlList);


        popupWindow = new PopupWindow(activityCapasMapa, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            popupWindow.setElevation(6);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            popupWindow.showAsDropDown(botonKml, 0, 0, Gravity.CENTER_HORIZONTAL);
        } else {
            popupWindow.showAtLocation(coordinatorLayout, Gravity.CENTER_HORIZONTAL, 0, 0);
        }
        popupWindow.setOutsideTouchable(false);

        //adaptador del ListView
        ArrayAdapter<String> adapterKMl = new ArrayAdapter<>(context, android.R.layout.simple_list_item_multiple_choice, listKml);
        listViewKml.setAdapter(adapterKMl);
        listViewKml.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        for (CubiertaObject cubiertaObject : kmlObjectArrayList) {
            if (cubiertaObject.getEstado() == true) {
                listViewKml.setItemChecked(kmlObjectArrayList.indexOf(cubiertaObject), true);
            } else {
                listViewKml.setItemChecked(kmlObjectArrayList.indexOf(cubiertaObject), false);
            }
        }

        listViewKml.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.e(TAG, "onItemClick: " + listViewKml.isItemChecked(i));
                if (!listViewKml.isItemChecked(i)) {
                    listViewKml.setItemChecked(i, false);
                    kmlObjectArrayList.get(i).setEstado(false);
                    habilitarKml(listKml.get(i), false, kmlObjectArrayList.get(i));

                } else {
                    listViewKml.setItemChecked(i, true);
                    kmlObjectArrayList.get(i).setEstado(true);
                    habilitarKml(listKml.get(i), true, kmlObjectArrayList.get(i));

                }
            }
        });

    }

    /**
     * Se encarga de recolectar los archivos .kml en la carpeta de idatum del celular
     * y agregarlo a una lista objetos.
     */
    private void listarArchivosKml() {

        if (kmlObjectArrayList != null && kmlObjectArrayList.size() > 0 && listKml != null && listKml.size() > 0) {
            abrirPopUpWindowKml(listKml, kmlObjectArrayList);
            return;
        }
        kmlObjectArrayList = new ArrayList<>();
        listKml = new ArrayList<>();

        //Carpeta donde se busca los archivos kml.
        File f = new File(Environment.getExternalStorageDirectory() + File.separator + "idatumKml");

        boolean success = true;
        if (!f.exists()) {
            success = f.mkdirs();
            Log.e(TAG, "listarArchivosKml: " + success);
        }
        if (success) {
            File[] files = f.listFiles();

            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                if (file.isDirectory()) {
                    listKml.add(file.getName() + "/");
                } else {
                    CubiertaObject cubiertaObject = new CubiertaObject();
                    cubiertaObject.setNombre(file.getName());
                    kmlObjectArrayList.add(cubiertaObject);
                    listKml.add(file.getName());
                }
            }

            if (listKml.size() > 0) {
                abrirPopUpWindowKml(listKml, kmlObjectArrayList);
            } else {
                Toast.makeText(context, "No hay archivos KML.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.e(TAG, "listarArchivosKml: Error al crear carpeta.");
        }
    }


    /**
     * Agrega una cubierta kml al mapa.
     *
     * @param nombre         Es el nombre de la cubierta kml.
     * @param b              Indica y se debe habilitar kml o desahiblitar kml.
     * @param cubiertaObject
     */
    private void habilitarKml(String nombre, boolean b, CubiertaObject cubiertaObject) {
        try {
            Log.e(TAG, "habilitarKml: " + b);
            if (b) {

                Toast.makeText(context, "Habilitar: " + nombre, Toast.LENGTH_SHORT).show();
                FileInputStream fileInputStream = new FileInputStream(Environment.getExternalStorageDirectory() +
                        File.separator + "idatumKml" + File.separator + nombre);
                kmlLayer = new KmlLayer(mMap, fileInputStream, context);
                kmlLayer.addLayerToMap();
                cubiertaObject.setKmlLayer(kmlLayer);

            } else {
                if (cubiertaObject.getKmlLayer() != null) {

                    Toast.makeText(context, "Deshabilitar: " + nombre, Toast.LENGTH_SHORT).show();
                    cubiertaObject.getKmlLayer().removeLayerFromMap();

                }
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Se ejecuta cuando el mapa esta listo para
     * ser mostrado en el fragmento.
     *
     * @param googleMap Es el objeto de mapa de google.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        checkIfGPSIsEnable();
        mMap.setMapType(GoogleMap.MAP_TYPE_NONE);

        //Se configura un listener en el mapa al hacer click largo
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                if (marker != null) {
                    marker.remove();
                }
                myLatitudeMarker = latLng.latitude;
                myLongitudeMarker = latLng.longitude;
                MarkerOptions options = new MarkerOptions()
                        .title("Lat " + myLatitudeMarker.toString().substring(0, 6) + ", " + "Lon " + myLongitudeMarker.toString().substring(0, 6))
                        .position(latLng);
                marker = mMap.addMarker(options);
                checkCondicionesBotonGuardar();
                //checkMarker();
            }
        });
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.getUiSettings().setMapToolbarEnabled(false);

        //Chequea si los permisos fueron garantizados y enciende el servicio de locacion de google.
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            View locationButton = ((View) mapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
            RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
            // position on right bottom
            rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
            rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            rlp.setMargins(0, 0, 30, 30);
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSION_FINE_LOCATION);
            }
        }

        if (sharedPreferences.getString(Config.ID_MAPA, "") != "") {
            //Busca la info del mapa de idatum, mapas bases y cubiertas.
            buscarInfoMapa();

        } else {
            mMap.setMapType(GoogleMap.MAP_TYPE_NONE);
        }
    }

    /**
     * Obtiene la informacion del mapa, dado un id de mapa,
     * se obtienen las diferentes cubiertas que este tiene.
     */
    private void buscarInfoMapa() {


        String urlBusqueda = sharedPreferences.getString(Config.URL_SHARED, "") + "/exp/exp_modulo294.php";
        String idMapa = sharedPreferences.getString(Config.ID_MAPA, "");


        Call<ElenaExcepciones> elenaExcepcionesCall = ApiUtils.obtenerCliente().obtenerInfoMap(urlBusqueda, "em294_obtenerInfoMapa", idMapa);
        elenaExcepcionesCall.enqueue(new Callback<ElenaExcepciones>() {
            @Override
            public void onResponse(Call<ElenaExcepciones> call, Response<ElenaExcepciones> response) {
                if (response.isSuccessful()) {
                    ElenaExcepciones elenaExcepciones = response.body();
                    //Maneja el elena de la respuesta.
                    obtenerInfoMapa(elenaExcepciones);
                } else {
                    Log.e(TAG, "onResponse: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<ElenaExcepciones> call, Throwable t) {
                Log.e(TAG, "onFailure: " + t.getLocalizedMessage());
            }
        });


    }

    /**
     * Obtiene la informacion del mapa, dado un id de mapa,
     * se obtienen las diferentes cubiertas que este tiene.
     *
     * @param elenaExcepciones
     */
    private void obtenerInfoMapa(ElenaExcepciones elenaExcepciones) {

        try {
            wmsList = new ArrayList<>();
            wmsCubiertas = new ArrayList<>();

            final JSONArray array = new JSONArray(elenaExcepciones.getExe_res());
            //listaIdPortales.add("Seleccione");
            jsonArrayMapaBase = array.getJSONArray(0);
            jsonArrayCubiertas = array.getJSONArray(2);

            String mapaBaseDef = null;
            for (int i = 0; i < jsonArrayMapaBase.length(); i++) {


                JSONObject jsonObject = jsonArrayMapaBase.getJSONObject(i);
                String ser_img_fun = jsonObject.getString("ser_img_fun");
                String nombre = getNombre(ser_img_fun);

                //Obtengo el nombre del mapa base por defecto
                if (jsonObject.getString("map_ser_img_def").equals("t")) {
                    mapaBaseDef = nombre;
                }
            }
            if (mapaBaseDef != null) {
                cambiarMapaBaseDef(mapaBaseDef);
            }

            listCubDef = new ArrayList<>();
            for (int i = 0; i < jsonArrayCubiertas.length(); i++) {
                CubiertaObject cubiertaObject = new CubiertaObject();
                JSONObject jsonObject = jsonArrayCubiertas.getJSONObject(i);
                String id_cubierta = "c" + jsonObject.getString("cubiertas_id");
                cubiertaObject.setNombre(jsonObject.getString("objeto_nomb"));
                cubiertaObject.setId(id_cubierta);
                wmsList.add(jsonObject.getString("objeto_nomb"));

                if (jsonObject.getString("cubiertas_estado").equals("1")) {
                    cubiertaObject.setEstado(true);
                    listCubDef.add(id_cubierta);
                }

                wmsCubiertas.add(cubiertaObject);
            }
            habilitarCubiertas(listCubDef);

        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    /**
     * Lista los mapas bases que tiene el mapa configurado.
     *
     * @param spinner Es el selector que contendra los distintos mapas bases.
     */
    private void listarMapaBase(final Spinner spinner) {
        try {
            //Nombre del mapa base por defecto
            String mapaBaseDef = null;
            final List<String> list = new ArrayList<>();

            for (int i = 0; i < jsonArrayMapaBase.length(); i++) {
                JSONObject jsonObject = jsonArrayMapaBase.getJSONObject(i);
                //Log.e(TAG, "listarMapaBase: " + jsonObject.getString("ser_img_fun"));

                String ser_img_fun = jsonObject.getString("ser_img_fun");
                String nombre = getNombre(ser_img_fun);

                //Obtengo el nombre del mapa base por defecto
                if (jsonObject.getString("map_ser_img_def").equals("t") && !abrirPopUp) {
                    mapaBaseDef = nombre;
                } else {
                    mapaBaseDef = nombreMapaActual;
                }

                if (nombre != null)
                    list.add(nombre);
            }
            CustomSpinnerAdapterMap customSpinnerAdapterMap = new CustomSpinnerAdapterMap(list, getActivity());
            spinner.setAdapter(customSpinnerAdapterMap);


            if (mapaBaseDef != null && list != null) {
                seleccionarBaseDef(mapaBaseDef, list, spinner);
            }

            spinner.setPrompt("Mapa Base");

            final Boolean[] seleccionar = {false};

            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    if (seleccionar[0]) {
                        if (spinner.getSelectedItem() != null) {

                            cambiarMapaBaseDef(list.get(i).toString());
                            //cambiarMapaBaseDef(spinner.getSelectedItem().toString());

                        }
                    } else {
                        seleccionar[0] = true;
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
     * Cambia el tipo de mapa base.
     *
     * @param nombreMapa Es el nombre del mapa base.
     */
    private void cambiarMapaBaseDef(String nombreMapa) {
        nombreMapaActual = nombreMapa;
        switch (nombreMapa) {
            case "Google Calles":
                if (osmTileOverlay != null) {
                    osmTileOverlay.remove();
                }
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                break;
            case "Google Hibrido":
                if (osmTileOverlay != null) {
                    osmTileOverlay.remove();
                }
                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                break;
            case "Google Satelite":
                if (osmTileOverlay != null) {
                    osmTileOverlay.remove();
                }
                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                break;
            case "Google Terreno":
                if (osmTileOverlay != null) {
                    osmTileOverlay.remove();
                }
                mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                break;
            case "OSM Mapnik MERCATOR":
                if (tileOverlay != null) {
                    tileOverlay.remove();
                }
                OSMTileProvider osmTileProvider = new OSMTileProvider(256, 256, "http://a.tile.openstreetmap.org/{z}/{x}/{y}.png");
                mMap.setMapType(GoogleMap.MAP_TYPE_NONE);
                osmTileOverlay = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(osmTileProvider));
                if (list2 != null) {
                    habilitarCubiertas(list2);
                } else {
                    habilitarCubiertas(listCubDef);
                }
                break;
        }
    }

    /**
     * Selecciona en el selector el mapa base por defecto.
     *
     * @param mapaBaseDef Nombre del mapa base por defecto.
     * @param list        Lista de mapas bases.
     * @param spinner     El selector de los mapas bases.
     */
    private void seleccionarBaseDef(String mapaBaseDef, List<String> list, Spinner spinner) {
        for (int i = 0; i < list.size(); i++) {
            if (mapaBaseDef == list.get(i)) {
                spinner.setSelection(i);
            }
        }
    }

    /**
     * Configura el nombre del mapa base.
     *
     * @param ser_img_fun Parametro usado para encontrar el nombre del mapa base.
     * @return devuelve el nombre del mapa base.
     */
    private String getNombre(String ser_img_fun) {
        String nombre = null;
        if (ser_img_fun.contains("Google Calles Mercator")) {
            nombre = "Google Calles";
        }
        if (ser_img_fun.contains("google.maps.MapTypeId.HYBRID")) {
            nombre = "Google Hibrido";
        }
        if (ser_img_fun.contains("google.maps.MapTypeId.SATELLITE")) {
            nombre = "Google Satelite";
        }
        if (ser_img_fun.contains("google.maps.MapTypeId.TERRAIN")) {
            nombre = "Google Terreno";
        }

        if (ser_img_fun.contains("OSM Mapnik MERCATOR")) {
            nombre = "OSM Mapnik MERCATOR";
        }
        return nombre;
    }


    /**
     * Se encarga de mostrar las cubiertas en el mapa que esten habilitadas
     *
     * @param list Lista con las cubiertas habilitadas para mostrar.
     */
    private void habilitarCubiertas(List<String> list) {
        if (tileOverlay != null) {
            tileOverlay.remove();
            tileOverlay.clearTileCache();
            tileOverlay = null;
        }
        if (list.size() > 0) {
            String capas = TextUtils.join(",", list); //+ ",ctmp_0,ctmp_1,ctmp_2,ctmp_buffer,ctmp";
            Log.d(TAG, "Capas: " + capas);
            TileProvider wms = new WMSTileProviderGoogle(256, 256,
                    sharedPreferences.getString(Config.ID_CONT_MAPA, ""),
                    capas,
                    sharedPreferences.getString(Config.URL_SHARED, ""));

            tileOverlay = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(wms));
        } else {
            Toast.makeText(context, "Sin Capas", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Realiza una peticion de permisos para usar el GPS en la aplicacion.
     *
     * @param requestCode  Es el codigo de peticion
     * @param permissions
     * @param grantResults El resultado del permiso solicitado.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSION_FINE_LOCATION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        mMap.setMyLocationEnabled(true);
                    }
                } else {
                    Toast.makeText(getActivity(), "la aplicacion requiere activar permisos de locacion", Toast.LENGTH_LONG).show();
                    getActivity().finish();
                }
                break;
            case WRITE_EXTERNAL_PERMISSION_REQUEST_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    listarArchivosKml();
                } else {
                    Toast.makeText(context, "Esta funcion necesita permisos de escritura", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    /**
     * Abre una ventana popup donde permite escoger el tipo de mapa que se mostrara en el fragmento.
     */
    private void abrirPopUpWindow() {
        //Encuentra la vista de los 4 diferentes mapas de google, hecha en xml
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layoutMapaBase = layoutInflater.inflate(R.layout.activity_capas_mapa, null);


        popupWindow = new PopupWindow(layoutMapaBase, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            popupWindow.showAsDropDown(fabTipoMapa, 0, 0, Gravity.CENTER_HORIZONTAL);
        } else {
            popupWindow.showAtLocation(coordinatorLayout, Gravity.CENTER_HORIZONTAL, 0, 0);
        }
        popupWindow.setOutsideTouchable(false);
        popupWindow.setFocusable(true);

        if (Build.VERSION.SDK_INT >= 21) {
            popupWindow.setElevation(5.0f);
        }

        Button botonCerrar = layoutMapaBase.findViewById(R.id.botonCerrarPopUp);
        botonCerrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupWindow.dismiss();
            }
        });
        //Spinner donde se despliegan los mapas bases.
        Spinner spinner = layoutMapaBase.findViewById(R.id.simpleSpinner);
        listarMapaBase(spinner);
        //List view donde estan todos los items.
        final ListView listView = layoutMapaBase.findViewById(R.id.wmsList);
        //adaptador del ListView
        ArrayAdapter<String> adapterWms = new ArrayAdapter<>(context, android.R.layout.simple_list_item_multiple_choice, wmsList);
        listView.setAdapter(adapterWms);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        for (CubiertaObject cubiertaObject : wmsCubiertas) {
            if (cubiertaObject.getEstado() == true) {
                listView.setItemChecked(wmsCubiertas.indexOf(cubiertaObject), true);
            } else {
                listView.setItemChecked(wmsCubiertas.indexOf(cubiertaObject), false);
            }
        }

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (!listView.isItemChecked(i)) {
                    listView.setItemChecked(i, false);
                    wmsCubiertas.get(i).setEstado(false);

                } else {
                    listView.setItemChecked(i, true);
                    wmsCubiertas.get(i).setEstado(true);
                }
                Log.d(TAG, "onCheckedChanged: ----------------");
                list2 = new ArrayList<>();

                try {
                    for (CubiertaObject cubiertaObject : wmsCubiertas) {
                        if (cubiertaObject.getEstado() == true) {
                            list2.add(cubiertaObject.getId());
                        }
                    }
                    habilitarCubiertas(list2);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Configura el intervalo de tiempo que tendran las peticiones de locacion para el mapa.
     */
    private void construirLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(10 * 1000)
                .setFastestInterval(5 * 1000)
                .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
    }

    /**
     * Construye el cliente de Google que presta los servicios de mapas.
     */
    private void construirGoogleApiClient() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    /**
     * Inicia todas las vistas de la interfaz del Mapa.
     */
    private void iniciarVistas() {
        realm = Realm.getDefaultInstance();
        sharedPreferences = context.getSharedPreferences(Config.NOMBRE_SHARED, Context.MODE_PRIVATE);

        actual = rootView.findViewById(R.id.actual);
        contenidoLocal = rootView.findViewById(R.id.contenidoLocal);
        borrar = rootView.findViewById(R.id.borrar);
        web = rootView.findViewById(R.id.web);
        buscarTarea = rootView.findViewById(R.id.buscarTarea);
        guardarCoorde = rootView.findViewById(R.id.guardarCoorde);
        fabTipoMapa = rootView.findViewById(R.id.fabTipoMapa);
        fabTools = rootView.findViewById(R.id.fabTools);
        botonKml = rootView.findViewById(R.id.botonKml);
        resultadoCont = rootView.findViewById(R.id.resultadoCont);
        cardView = rootView.findViewById(R.id.cardView);
        coordinatorLayout = rootView.findViewById(R.id.mapFragment);

        fabRotate1 = AnimationUtils.loadAnimation(context, R.anim.rotate_clockwise);
        fabRotate2 = AnimationUtils.loadAnimation(context, R.anim.rotate_anticlockwise);
        fabOpen = AnimationUtils.loadAnimation(context, R.anim.fab_open);
        fabClose = AnimationUtils.loadAnimation(context, R.anim.fab_close);

        final Typeface face = Typeface.createFromAsset(context.getAssets(), "fonts/oxygenregular.ttf");
        resultadoCont.setTypeface(face);
    }

    /**
     * Cierra todos los botones con una animacion.
     */
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

    /**
     * Abre todos los botones con una animacion.
     */
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

    /**
     * Se encarga de buscar tarea en caso de que el usuario
     * haya presionado el boton en la ficha de contenido externa.
     */
    private void buscarTareaMetodo() {

        loading = ProgressDialog.show(context, "Buscando tarea", "por favor espere..", false, false);
        String urlBusqueda = sharedPreferences.getString(Config.URL_SHARED, "") + "/exp/exp_modulo294.php";
        String usuarioId = sharedPreferences.getString(Config.ID_USUARIO_SHARED, "");

        Call<ElenaExcepciones> elenaExcepcionesCall = ApiUtils.obtenerCliente().buscarTarea(urlBusqueda, "solicitar_proceso", usuarioId);
        elenaExcepcionesCall.enqueue(new Callback<ElenaExcepciones>() {
            @Override
            public void onResponse(Call<ElenaExcepciones> call, Response<ElenaExcepciones> response) {
                loading.dismiss();
                if (response.isSuccessful()) {

                    ElenaExcepciones elenaExcepciones = response.body();
                    if (("0").equals(elenaExcepciones.getExe_men())) {
                        Snackbar.make(rootView.findViewById(R.id.mapFragment), "No hay tarea disponible",
                                Snackbar.LENGTH_SHORT)
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
                Snackbar.make(rootView.findViewById(R.id.mapFragment), "Error : intente de nuevo",
                        Snackbar.LENGTH_SHORT)
                        .show();
            }
        });

    }

    /**
     * Chequea si el gps esta activado en el dispositivo
     */
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

    /**
     * Muestra dialogo de alerta para activar el gps del movil, o cancelar la peticion.
     */
    private void showInfoAlert() {
        new AlertDialog.Builder(context)
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
                        getActivity().finish();
                    }
                })
                .show();
    }

    /**
     * Se ejecuta cuando la api de google maps este conectada.
     *
     * @param bundle
     */
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        requestLocationUpdates();
    }

    /**
     * Realiza la peticion para obtener acualizaciones locacion.
     */
    private void requestLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);

            mFusedLocationClient.getLastLocation().addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                /**
                 * Se encarga de obtener la ultima locacion.
                 * @param location Locacion actual.
                 */
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        LatLng myLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 15));
                    }
                }
            });
        }
    }

    /**
     * Se ejecuta en caso de que la conexion sea suspendida.
     *
     * @param i
     */
    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "connection Suspended");
    }

    /**
     * Se ejecuta en caso de que la conexion
     * con el servicio de google map falle.
     *
     * @param connectionResult Error que arroja el resultado.
     */
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i(TAG, "Connection Failed: ConnectionResult.getErrorCode() = " + connectionResult.getErrorCode());
    }

    /**
     * Se ejecuta en caso de que la locacion actual cambie.
     *
     * @param location Locacion actual.
     */
    @Override
    public void onLocationChanged(Location location) {
        myLatitude = location.getLatitude();
        myLongitude = location.getLongitude();
    }

    /**
     * Se ejecuta automaticamente cuando la actividad haya sido creada
     * y inicia el proceso de conexion del cliente de google maps.
     */
    @Override
    public void onStart() {
        super.onStart();
        googleApiClient.connect();
    }

    /**
     * Se ejecuta cuando la aplicacion este en pausa y remueve la conexion
     * con los servicios de google maps.
     */
    @Override
    public void onPause() {
        super.onPause();
        try {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
                Log.d(TAG, "onPause: Removed");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Se ejecuta cuando la aplicacion se abre nuevamente y ya ha sido creada, realizando
     * de nuevo la conexion a los servicios de google maps.
     */
    @Override
    public void onResume() {
        super.onResume();
        if (googleApiClient.isConnected()) {
            requestLocationUpdates();
        }
    }

    /**
     * Se ejecuta cuando la aplicacion se detiene y desconecta totalmente del cliente de google.
     */
    @Override
    public void onStop() {
        super.onStop();
        googleApiClient.disconnect();
        Log.d(TAG, "onStop: " + googleApiClient.isConnected());
    }

    /**
     * Posiciona un marker justamente en la posicion actual en que se encuentra el usuario.
     */
    private void marcarActual() {
        LatLng myLocation = new LatLng(myLatitude, myLongitude);
        myLatitudeMarker = myLatitude;
        myLongitudeMarker = myLongitude;
        if (marker != null) {
            marker.remove();
        }
        MarkerOptions options = new MarkerOptions()
                .title("Lat " + myLatitudeMarker.toString().substring(0, 6) + ", Lon " + myLongitudeMarker.toString().substring(0, 6))
                .position(myLocation)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN));
        marker = mMap.addMarker(options);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 15));
    }

    /**
     * Borra todos los markers posicionados en el mapa.
     */
    private void irBorrar() {
        if (marker != null) {
            marker.remove();
            marker = null;
        }
        myLatitudeMarker = null;
        myLongitudeMarker = null;
    }

    /**
     * Redirecciona el usuario a la pagina web del sistema al que esta conectado.
     */
    private void irWeb() {
        String url = sharedPreferences.getString(Config.URL_SHARED, "");
        String portal = sharedPreferences.getString(Config.ID_PORTAL, "");
        if (url != null && !url.isEmpty() && portal != null && !portal.isEmpty()) {
            Intent intentWeb = new Intent();
            intentWeb.setAction(Intent.ACTION_VIEW);
            intentWeb.setData(Uri.parse(url + "/?pid=" + portal));
            startActivity(Intent.createChooser(intentWeb, "Elige navegador"));
        } else {
            Toast.makeText(context, "Debe configurar un id de Portal", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Obntiene una respuesta dentro de un objeto elena
     *
     * @param response La respuesta desde servidor externo.
     */
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

    /**
     * Evalua el tipo de tarea que se esta realizando.
     * 1 si es tarea manual.
     * 2 si es tarea desde Boton-Ficha.
     * 3 si es tarea local.
     */
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
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
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
                                Snackbar.make(rootView.findViewById(R.id.mapFragment), "Proceso Local Finalizado",
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

    /**
     * Guarda las coordenadas para georreferenciar la ficha de contenido.
     * Tarea desde la ficha-boton.
     */
    private void guardarCoordenadasFicha(final Double latitudSend, final Double longitudSend) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("AVISO");
        builder.setMessage("Ficha Id: " + id_obj_cont + "\n\n¿Esta seguro que desea guardar las coordenadas? \n" + "\nLatitud: " + latitudSend + "\nLongitud: " + longitudSend)
                .setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        loading = ProgressDialog.show(context, "Obteniendo datos", "Por favor, espere...", false, false);

                        String urlBusqueda = sharedPreferences.getString(Config.URL_SHARED, "no disp") + "/exp/exp_modulo294.php";
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
     * Guarda las coordenadas para georreferenciar la ficha de contenido.
     * Tarea manual.
     */
    private void guardarCoordenadasManual(final Double latitudSend, final Double longitudSend) {


        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("AVISO");

        builder.setMessage("Ficha Id: " + id_obj_cont_interno + "\n\n¿Esta seguro que desea guardar las coordenadas? \n" + "\nLatitud: " + latitudSend + "\nLongitud: " + longitudSend)
                .setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        loading = ProgressDialog.show(context, "Obteniendo datos", "Por favor, espere...", false, false);

                        String urlBusqueda = sharedPreferences.getString(Config.URL_SHARED, "no disp") + "/exp/exp_modulo294.php";
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
                                        Snackbar.make(rootView.findViewById(R.id.mapFragment), "Proceso Manual Finalizado",
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

    /**
     * Se encarga de crear la estructura del XML a enviar.
     *
     * @param latitudSend  Latitud a enviar.
     * @param longitudSend Longitud a enviar.
     * @return Retorna el xml a enviar al webservice.
     */
    private StringWriter buscarXml(Double latitudSend, Double longitudSend) {
        StringWriter writer;
        XmlDoc xmlDoc = null;
        try {
            xmlDoc = new XmlDoc();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        String id_tipo_cont = sharedPreferences.getString(Config.ID_TIPO_CONT_SHARED_, "no disponible");
        String id_relacion = sharedPreferences.getString(Config.ID_RELACION_SHARED, "no disponible");
        String usuario = sharedPreferences.getString(Config.USUARIO_SHARED, "no disponible");
        String contraseña = sharedPreferences.getString(Config.CLAVE_SHARED, "no es posible");

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
        String usuario = sharedPreferences.getString(Config.USUARIO_SHARED, "no disponible");
        String contraseña = sharedPreferences.getString(Config.CLAVE_SHARED, "no es posible");
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
        String urlBusqueda = sharedPreferences.getString(Config.URL_SHARED, "") + "/exp/exp_modulo294.php";
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
     * Obtiene la respuesta final del servidor confirmando que cerro el proceso de georreferenciacion desde la Ficha-Boton.
     *
     * @param elena Objeto elena que transporta la informacion de la respuesta.
     */
    private void obtenerRespuestaFinal(ElenaExcepciones elena) {

        if (elena.getExe_est_res().equals("true")) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(Config.ID_TAREA_SHARED, "");
            id_obj_cont_interno = null;
            id_obj_cont = null;
            id_relacion = null;
            id_tipo_cont = null;
            myLatitudeMarker = null;
            myLongitudeMarker = null;

            editor.commit();
            Snackbar.make(rootView.findViewById(R.id.mapFragment), "Proceso Finalizado",
                    Snackbar.LENGTH_SHORT)
                    .show();
            irBorrar();
            resultadoCont.setText("");
            cardView.setVisibility(View.INVISIBLE);
            tipoTarea = 0;
            checkCondicionesBotonGuardar();
            //checkMarker();
            getActivity().finish();

        } else {
            Toast.makeText(context, "Hubo error al finalizar",
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Crea la lista de objetos a enviar dentro del elena que contiene toda la informacion.
     *
     * @param list
     * @return
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
     * Chequea si el boton para guardar coordenadas debe estar visible o no.
     * Para que este visible debe tener una tarea levantada y un punto en el mapa, si no, debe estar invisible.
     */
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

    /**
     * Chequea si hay un marker visible en el mapa para que aparezca el boton de borrar markers.
     * Si no hay markers en el mapa, este boton permanece invisible.
     */
    /*
    private void checkMarker() {
        if (isOpen) {
            if (marker != null) {
                borrar.setVisibility(View.VISIBLE);
            } else {
                borrar.setVisibility(View.INVISIBLE);
            }
        }
    }*/

    /**
     * Se encarga de buscar todos los contenidos guardados en la base de datos y mostrarlos
     * en el mapa en caso de que ya hayan sido georreferenciados previamente.
     */
    private void buscarContenidosLocales() {
        if (sharedPreferences.getBoolean(Config.LOGIN_SHARED, false)) {
            final ArrayList<LatLng> puntos = new ArrayList<>();
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    contenidos = realm.where(Contenidos.class).contains("id_usuario", sharedPreferences.getString(Config.ID_USUARIO_SHARED, "")).findAll();

                    for (Contenidos contenidoRecorrido : contenidos) {

                        if (contenidoRecorrido.getLatitud() != null) {
                            LatLng latLng = new LatLng(Double.parseDouble(contenidoRecorrido.getLatitud()), Double.parseDouble(contenidoRecorrido.getLongitud()));
                            puntos.add(latLng);
                            MarkerOptions options = new MarkerOptions()
                                    .title(contenidoRecorrido.getNombre_contenido())
                                    .position(latLng)
                                    .snippet("Lat " + String.valueOf(latLng.latitude).substring(0, 6) + ", Lon " + String.valueOf(latLng.longitude).substring(0, 6));
                            marker = mMap.addMarker(options);
                        }
                    }
                }
            });
            //checkMarker();
            if (puntos.size() == 0) {
                Toast.makeText(context, "No hay contenidos locales georreferenciados.", Toast.LENGTH_SHORT).show();
                return;
            }
            zoomToBoundingBox(puntos);
        } else {
            Toast.makeText(context, "Debe ingresar al sistema", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Hace el calculo de conseguirel bounding box entre los distintos
     * puntos de coordenadas para hacer un zoom a esa zona.
     *
     * @param puntos Lista de puntos de coordenadas.
     */
    private void zoomToBoundingBox(ArrayList<LatLng> puntos) {
        double norte = 0, sur = 0, oeste = 0, este = 0;

        for (int i = 0; i < puntos.size(); i++) {
            if (puntos.get(i) == null) continue;
            double lat = puntos.get(i).latitude;
            double lon = puntos.get(i).longitude;

            if ((i == 0) || (lat > norte)) norte = lat;
            if ((i == 0) || (lat < sur)) sur = lat;
            if ((i == 0) || (lon < oeste)) oeste = lon;
            if ((i == 0) || (lon > este)) este = lon;

        }
        Log.e(TAG, "zoomToBoundingBox: " + " " + norte + " " + este + " " + sur + " " + oeste);
        LatLngBounds latLngBounds = new LatLngBounds(new LatLng(sur, oeste), new LatLng(norte, este));
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 100));
    }

    /**
     * Chequea si el boton de herramientas debe estar vivible o invisible.
     */
    public void checkFabTools() {
        if (sharedPreferences.getBoolean(Config.LOGIN_SHARED, false)) {
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

    /**
     * Oculta el boton de funciones
     */
    private void esconderFabTools() {
        fabTools.startAnimation(fabClose);
        fabTools.setClickable(false);
    }

    /**
     * Se ejecuta cuando la aplicacion se cierra, cerrando automaticamente la base de datos.
     */
    @Override
    public void onDestroy() {
        realm.close();
        super.onDestroy();
    }


}
