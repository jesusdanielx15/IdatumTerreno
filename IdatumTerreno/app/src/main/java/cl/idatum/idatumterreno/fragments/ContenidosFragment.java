package cl.idatum.idatumterreno.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import cl.idatum.idatumterreno.R;
import cl.idatum.idatumterreno.activities.NuevoContenido;
import cl.idatum.idatumterreno.api.ApiUtils;
import cl.idatum.idatumterreno.api.ApiUtilsXML;
import cl.idatum.idatumterreno.elena.ElenaExcepciones;
import cl.idatum.idatumterreno.models.Contenidos;
import cl.idatum.idatumterreno.response.Contenido;
import cl.idatum.idatumterreno.response.Idatum;
import cl.idatum.idatumterreno.utils.AtributosDinamicos;
import cl.idatum.idatumterreno.utils.Config;
import cl.idatum.idatumterreno.adapters.RestAdapter;
import cl.idatum.idatumterreno.utils.XmlDoc;
import io.realm.Realm;
import io.realm.RealmResults;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Fragmento encargado de realizar la busqueda de contenidos del sistema.
 *
 * @author Jesus Cañizales <jcanizales@siigsa.cl>
 * @version 2
 * @package cl.idatum.idatumterreno Proyecto IdatumTerreno
 * @link http://www.siigsa.cl
 * @copyright SIIGSA, Propiedad Intelectual y Derechos Patrimoniales de Software y Base de Datos i-datum. Registro Propiedad Intelectual Nº 211.351 y 211.352 respectivamente, con fecha 22 de noviembre del 2011
 * @since 25/10/2017
 */
public class ContenidosFragment extends Fragment {
    private List<Contenido> kk;
    private RecyclerView recyclerView;
    private RestAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private SearchView searchView;
    private MenuItem menuItem;
    private ProgressDialog loading_contenido;
    private SharedPreferences prefs;
    private Context context;
    private ContenidosFragment contenidosFragment;
    private FrameLayout frameLayout;
    private Realm realm;
    private String idrealm, nombrerealm;
    private Bundle bundle;
    private RealmResults<Contenidos> contenidosRealmResults;
    private Paint p = new Paint();
    private FloatingActionButton agregarNuevo;
    private String TAG = "ContenidosFragment";

    /**
     * Constructor de la clase de busqueda de contenidos.
     */
    public ContenidosFragment() {
        // Required empty public constructor
    }

    /**
     * Metodo principal de la clase.
     *
     * @param savedInstanceState
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity();
        setHasOptionsMenu(true);
    }

    /**
     * Crea la vista del fragmento y ejecuta todos los metodos principales de la clase.
     *
     * @param inflater           Infla la vista.
     * @param container          Es el contenedor del fragmento
     * @param savedInstanceState
     * @return la vista del fragmento.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contenidos, container, false);

        realm = Realm.getDefaultInstance();
        prefs = context.getSharedPreferences(Config.NOMBRE_SHARED, Context.MODE_PRIVATE);
        frameLayout = (FrameLayout) view.findViewById(R.id.contenidoFragment);
        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        agregarNuevo = (FloatingActionButton) view.findViewById(R.id.agregarNuevo);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(layoutManager);
        //recyclerView.addItemDecoration(new DividerItemDecoration(context, 1));

        agregarNuevo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (prefs.getString(Config.ID_TIPO_CONT_SHARED_, "") != "") {
                    agregarNuevaFichaDeContenido();
                } else {
                    Toast.makeText(context, "Debe tener configurado un id de tipo de contenido.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        initSwipe();
        return view;

    }

    /**
     * Metodo que permite detectar si el item se mueve a la izquierda o a la derecha y genera una accion.
     */
    private void initSwipe() {

        ItemTouchHelper.SimpleCallback simpleItemTouchHelper = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                if (direction == ItemTouchHelper.LEFT) {
                    guardarContenidoBaseDeDatos(kk.get(position).getId(), kk.get(position).getNombre(), position);
                } else {
                    georreferenciar(kk.get(position).getId(), kk.get(position).getNombre());
                }
            }


            @Override
            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                Bitmap icon;
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    View itemView = viewHolder.itemView;
                    Float height = Float.valueOf(itemView.getBottom() - itemView.getTop());
                    Float width = height / 3;

                    if (dX > 0) {
                        p.setColor(Color.parseColor("#388e3c"));
                        RectF background = new RectF(itemView.getLeft(), itemView.getTop(), dX, itemView.getBottom());
                        c.drawRect(background, p);
                        c.clipRect(background);
                        icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_action_actual);
                        RectF icon_dest = new RectF(itemView.getLeft() + width, itemView.getTop() + width, itemView.getLeft() + 2 * width, itemView.getBottom() - width);
                        c.drawBitmap(icon, null, icon_dest, p);
                    }
                    if (dX < 0) {
                        p.setColor(Color.parseColor("#2994C3"));
                        RectF background = new RectF(itemView.getRight() + dX, itemView.getTop(), itemView.getRight(), itemView.getBottom());
                        c.drawRect(background, p);
                        c.clipRect(background);
                        icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_action_save);
                        RectF icon_dest = new RectF(itemView.getRight() - 2 * width, itemView.getTop() + width, itemView.getRight() - width, itemView.getBottom() - width);
                        c.drawBitmap(icon, null, icon_dest, p);
                    }
                    c.restore();

                }

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };
        ItemTouchHelper itemTouch = new ItemTouchHelper(simpleItemTouchHelper);
        itemTouch.attachToRecyclerView(recyclerView);
    }


    private void agregarNuevaFichaDeContenido() {
        loading_contenido = ProgressDialog.show(context, "Buscando Datos", "Por favor, espere...", false, false);

        String urlBusqueda = prefs.getString(Config.URL_SHARED, "no disp") + "/exp/exp_modulo294.php";
        StringWriter writer = generarXmlCrearFicha("", "", "");
        Call<Idatum> idatumCall = ApiUtilsXML.obtenerClienteXml().xmlWebService(urlBusqueda, "proceso_webservice", writer.toString());
        idatumCall.enqueue(new Callback<Idatum>() {
            @Override
            public void onResponse(Call<Idatum> call, Response<Idatum> response) {
                if (response.isSuccessful()) {
                    String estadoTexto = response.body().getContenidos().get(0).getRespuesta().getEstado();
                    String mensajeTexto = response.body().getContenidos().get(0).getRespuesta().getMensaje();

                    if (estadoTexto.equals("1")) {
                        Toast.makeText(context, "Error mensaje: " + mensajeTexto, Toast.LENGTH_LONG).show();
                        loading_contenido.dismiss();
                    }
                    if (estadoTexto.equals("0")) {

                        obtenerPVC(response.body().getContenidos().get(0).getId());
                        //Snackbar.make(rootView.findViewById(R.id.mapFragment), "Ficha de Contenidos creada exitosamente: " + response.body().getContenidos().get(0).getId(),
                        //      Snackbar.LENGTH_SHORT)
                        //    .show();

                    }
                } else {
                    loading_contenido.dismiss();
                    Toast.makeText(context, "Error, intente de nuevo", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Idatum> call, Throwable t) {
                loading_contenido.dismiss();
                Toast.makeText(context, "Error: " + t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private StringWriter generarXmlCrearFicha(String nombre_contenido, String descripcion_contenido, String codigo_insti) {
        StringWriter writer;
        XmlDoc xmlDoc = null;
        try {
            xmlDoc = new XmlDoc();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        String usuario = prefs.getString(Config.USUARIO_SHARED, "no disponible");
        String contraseña = prefs.getString(Config.CLAVE_SHARED, "no es posible");

        String accion = "1";
        writer = xmlDoc.generateXML("",
                accion,
                usuario,
                contraseña,
                "",
                "",
                null,
                null,
                prefs.getString(Config.ID_TIPO_CONT_SHARED_, ""),
                "",
                nombre_contenido,
                descripcion_contenido,
                codigo_insti,
                "",
                new ArrayList<AtributosDinamicos>());

        return writer;
    }

    private void obtenerPVC(final String id_cont) {
        String urlBusqueda = prefs.getString(Config.URL_SHARED, "no disp") + "/exp/exp_modulo294.php";
        final Call<ElenaExcepciones> idatumCall = ApiUtils.obtenerCliente().obtenerIdPvc(urlBusqueda, "em294_getExtructuraPvc", id_cont);
        idatumCall.enqueue(new Callback<ElenaExcepciones>() {
            @Override
            public void onResponse(Call<ElenaExcepciones> call, Response<ElenaExcepciones> response) {
                loading_contenido.dismiss();
                ElenaExcepciones elenaExcepciones = response.body();
                if ("true".equals(elenaExcepciones.getExe_est_res())) {
                    //Inicia un Intent para que vaya a la actividad de NuevoContenido.
                    Intent intent = new Intent(getActivity(), NuevoContenido.class);
                    String json = new Gson().toJson(elenaExcepciones.getExe_res().get(0));
                    intent.putExtra("pvc", json);
                    intent.putExtra("id_cont", id_cont);
                    startActivity(intent);
                } else {
                    Toast.makeText(context, "No existe PVC asociado al tipo de contenido. " + elenaExcepciones.getExe_men(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ElenaExcepciones> call, Throwable t) {
                loading_contenido.dismiss();
                Log.e(TAG, "Error: " + t.getLocalizedMessage());
                Toast.makeText(context, "Error, intente de nuevo", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Metodo que se encarga de enviar una peticion XML al webservice para realizar una busqueda de contenidos.
     *
     * @param query parametro que indica el texto a buscar en los contenidos.
     */
    private void enviarXml(String query) {
        recyclerView.setAdapter(null);
        final String urlText = prefs.getString(Config.URL_SHARED, "");
        if (!urlText.equals("")) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.
                    INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);

            loading_contenido = new ProgressDialog(context);
            loading_contenido.setMessage("Por favor espere..");
            loading_contenido.setTitle("Buscando datos");
            loading_contenido.setIndeterminate(true);
            loading_contenido.show();

            String texto_libre = query;
            StringWriter writer;
            String usuarioText = prefs.getString(Config.USUARIO_SHARED, "");
            String claveText = prefs.getString(Config.CLAVE_SHARED, "");

            XmlDoc xmlDoc = null;
            try {
                xmlDoc = new XmlDoc();
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            }
            String id_contenido = "",
                    id_relacion = prefs.getString(Config.ID_RELACION_SHARED, ""),
                    id_tipo_cont = prefs.getString(Config.ID_TIPO_CONT_SHARED_, ""),
                    tipo_rel = "2",
                    accion = "4";

            Double latitudSend = null,
                    longitudSend = null;
            writer = xmlDoc.generateXML(texto_libre,
                    accion,
                    usuarioText,
                    claveText,
                    id_contenido,
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

            /**
             * Peticion HTTP a servidor externo
             */
            Call<Idatum> createCall = ApiUtilsXML.obtenerClienteXml().obtenerContenido(urlText + "/exp/exp_modulo294.php", "proceso_webservice", writer.toString());
            createCall.enqueue(new Callback<Idatum>() {
                @Override
                public void onResponse(Call<Idatum> call, Response<Idatum> response) {
                    loading_contenido.dismiss();
                    kk = response.body().contenidos;
                    ArrayList<Contenido> p = new ArrayList<>(kk);
                    if (p.size() > 0 && p.get(0).getNombre() != null) {
                        contenidosRealmResults = realm.where(Contenidos.class).contains("id_usuario", prefs.getString(Config.ID_USUARIO_SHARED, "")).findAll();
                        adapter = new RestAdapter(p, context, contenidosFragment, contenidosRealmResults);
                        recyclerView.setAdapter(adapter);
                        adapter.setOnItemClickListener(new RestAdapter.OnRecyclerViewItemClickListener() {
                            @Override
                            public void onRecyclerViewItemClicked(int position, final String id, final String nombre, View view) {
                                crearPopUp(view, id, nombre, position);
                            }
                        });
                    } else {
                        Toast.makeText(context, "No se encontraron contenidos", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Idatum> call, Throwable t) {
                    loading_contenido.dismiss();
                    Log.e(TAG, "Error :" + t.getLocalizedMessage());
                    Toast.makeText(context, "Error, intente de nuevo", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(context, "El sistema no ha sido configurado", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Metodo que crea una ventana popup para crear un menu de opciones.
     *
     * @param view     vista que recibe para crear el menu.
     * @param idCont   id de contenido de la ficha.
     * @param nombre   nombre de la ficha de contenido.
     * @param position posicion que tiene el item en el recycler view.
     */
    private void crearPopUp(View view, final String idCont, final String nombre, final int position) {

        PopupMenu popupMenu = new PopupMenu(context, view, Gravity.CENTER_HORIZONTAL);
        popupMenu.inflate(R.menu.menu_opciones_cont);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.guardarCont:
                        guardarContenidoBaseDeDatos(idCont, nombre, position);
                        break;
                    case R.id.georreferenciar:
                        georreferenciar(idCont, nombre);
                        break;
                }
                return true;
            }
        });
        popupMenu.setGravity(Gravity.CENTER_HORIZONTAL);
        popupMenu.show();
    }

    /**
     * Georreferencia un contenido del sistema con un par de coordenadas.
     *
     * @param idCont Id de contenido de la ficha.
     * @param nombre Nombre de la ficha.
     */
    private void georreferenciar(String idCont, String nombre) {
        idrealm = idCont;
        nombrerealm = nombre;
        bundle = new Bundle();
        bundle.putString("id", idrealm);
        bundle.putString("nombre", nombrerealm);
        menuItem.collapseActionView();
    }

    /**
     * Guarda un contenido seleccionado en la base de datos local del movil.
     *
     * @param idCont   Id de contenido de la ficha.
     * @param nombre   Nombre de la ficha.
     * @param position Posicion en que se encuentra en el listado de android.
     */
    private void guardarContenidoBaseDeDatos(final String idCont, final String nombre, final int position) {
        if (prefs.getString(Config.ID_TIPO_CONT_SHARED_, "") != null && prefs.getString(Config.ID_TIPO_CONT_SHARED_, "") != "") {
            realm.executeTransactionAsync(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    Contenidos contenido = realm.where(Contenidos.class)
                            .equalTo("id_usuario", prefs.getString(Config.ID_USUARIO_SHARED, ""))
                            .equalTo("id_contenido", idCont)
                            .findFirst();
                    Log.d("ContenidosFragment", "execute: " + contenido.getId_contenido());
                }
            }, new Realm.Transaction.OnSuccess() {
                @Override
                public void onSuccess() {
                    Snackbar.make(frameLayout, "Ficha " + idCont + " ya existe", Snackbar.LENGTH_SHORT).show();
                    adapter.notifyItemChanged(position);
                }
            }, new Realm.Transaction.OnError() {
                @Override
                public void onError(Throwable error) {
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            Contenidos contenidos = new Contenidos(idCont,
                                    prefs.getString(Config.ID_USUARIO_SHARED, null),
                                    nombre,
                                    prefs.getString(Config.ID_TIPO_CONT_SHARED_, null),
                                    null,
                                    null,
                                    prefs.getString(Config.ID_RELACION_SHARED, null));
                            realm.copyToRealm(contenidos);

                            Snackbar.make(frameLayout, "Ficha " + idCont + " Guardada", Snackbar.LENGTH_SHORT).show();
                            adapter.notifyDataSetChanged();
                            adapter.notifyDataSetChanged();
                        }
                    });
                }
            });
        } else {
            Toast.makeText(context, "Debe configurar tipo de contenido y de relacion antes de realizar esta accion", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Muestra el menu que contiene a los items en la barra de opciones.
     *
     * @param menu Menu a mostrar.
     */
    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menuItem = menu.findItem(R.id.buscarCont);
        searchView = (SearchView) menuItem.getActionView();
        menuItem.expandActionView();
        searchView.setQueryHint("Contenidos en Servidor");
        searchView.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View view) {
            }

            /**
             * Se ejecuta al cerrar la ventana del searchView. y cambia el fragmento de busqueda al fragmento de mapa.
             * @param view Vista del searchview.
             */
            @Override
            public void onViewDetachedFromWindow(View view) {
                try {
                    Fragment fragment = new MapFragment();
                    if (bundle != null) {
                        fragment.setArguments(bundle);
                    }
                    //Fragment f = getFragmentManager().findFragmentById(R.id.content_frame);
                    //if (!(f instanceof MapFragment) && f != null) {
                    getFragmentManager()
                            .beginTransaction()
                            .replace(R.id.content_frame, fragment)
                            .commit();
                    //}
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            /**
             * Se ejecuta cuando el usuario hace una busqueda y
             * llama al metodo que forma la estructura XML dado
             * un parametro para buscar.
             * @param query Palabra a buscar.
             * @return Retorna la accion de busqueda.
             */
            @Override
            public boolean onQueryTextSubmit(String query) {
                enviarXml(query);
                return true;
            }

            /**
             * Se ejecuta cuando el campo del SearchView detecta un cambio.
             * @param newText Nueva palabra de busqueda
             * @return Retorna la accion de busqueda.
             */
            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        super.onPrepareOptionsMenu(menu);
    }

    /**
     * Metodo que se ejecuta al cerrar el fragmento para cerrar la base de datos.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        realm.close();
    }

}
