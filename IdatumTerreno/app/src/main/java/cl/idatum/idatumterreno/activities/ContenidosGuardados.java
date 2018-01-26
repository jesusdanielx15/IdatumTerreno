package cl.idatum.idatumterreno.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.StringWriter;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;

import cl.idatum.idatumterreno.R;
import cl.idatum.idatumterreno.adapters.RealmAdapter;
import cl.idatum.idatumterreno.api.ApiUtilsXML;
import cl.idatum.idatumterreno.models.Contenidos;
import cl.idatum.idatumterreno.response.Idatum;
import cl.idatum.idatumterreno.utils.AtributosDinamicos;
import cl.idatum.idatumterreno.utils.Config;
import cl.idatum.idatumterreno.utils.XmlDoc;
import co.moonmonkeylabs.realmrecyclerview.RealmRecyclerView;
import co.moonmonkeylabs.realmrecyclerview.RealmSimpleItemTouchHelperCallback;
import io.realm.Realm;
import io.realm.RealmResults;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Actividad para renderizar todos los contenidos guardados en la base de datos local del movil.
 *
 * @author Jesus Cañizales <jcanizales@siigsa.cl>
 * @version 2
 * @package cl.idatum.idatumterreno Proyecto IdatumTerreno
 * @link http://www.siigsa.cl
 * @copyright SIIGSA, Propiedad Intelectual y Derechos Patrimoniales de Software y Base de Datos i-datum. Registro Propiedad Intelectual Nº 211.351 y 211.352 respectivamente, con fecha 22 de noviembre del 2011
 * @since 25/10/2017
 */
public class ContenidosGuardados extends AppCompatActivity implements SearchView.OnQueryTextListener {

    private Realm realm;
    private RealmResults<Contenidos> contenidos;
    private Toolbar toolbar;
    private RealmAdapter realmAdapter;
    private RealmRecyclerView realmRecyclerView;
    private SharedPreferences prefs;
    private ProgressDialog loading;
    private TextView titulo;
    private Typeface typeface;
    private Paint p = new Paint();


    /**
     * Metodo principal de la clase
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contenidos_guardados);

        realm = Realm.getDefaultInstance();
        //Configurar toolbar.
        setToolbar();

        prefs = getSharedPreferences(Config.NOMBRE_SHARED, Context.MODE_PRIVATE);
        titulo = findViewById(R.id.nombre_app);
        typeface = Typeface.createFromAsset(getAssets(), "fonts/oxygenregular.ttf");
        titulo.setTypeface(typeface);

        contenidos = realm.where(Contenidos.class).contains("id_usuario", prefs.getString(Config.ID_USUARIO_SHARED, "")).findAll().sort("id");

        realmAdapter = new RealmAdapter(ContenidosGuardados.this, realm, contenidos, true, true);
        realmRecyclerView = findViewById(R.id.realmRecyclerContenidos);
        realmRecyclerView.addItemDecoration(new DividerItemDecoration(ContenidosGuardados.this, 1));
        realmRecyclerView.setAdapter(realmAdapter);
        realmRecyclerView.setClickable(true);

        realmAdapter.setOnItemClickListener(new RealmAdapter.OnRealmRecyclerViewItemClickListener() {
            @Override
            public void onRealmRecyclerViewItemClicked(int position, String id_contenido, String id_usuario, String nombre_contenido, String id_tipo_contenido, String latitud, String longitud, String id_relacion, View view) {
                crearPopUp(position, view, id_contenido, id_usuario, nombre_contenido, id_tipo_contenido, id_relacion, latitud, longitud);
            }
        });

        initSwipe();
    }

    private void setToolbar() {
        toolbar = findViewById(R.id.appbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    /**
     * Metodo que permite detectar si el item se mueve a
     * la izquierda o a la derecha y genera una accion.
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
                    if (contenidos.get(position).getLatitud() != null && contenidos.get(position).getLongitud() != null) {

                        guardarCoordenadas(contenidos.get(position).getId_contenido(),
                                contenidos.get(position).getTipo_id_contenido(),
                                contenidos.get(position).getId_relacion(),
                                contenidos.get(position).getLatitud(),
                                contenidos.get(position).getLongitud());
                    } else {
                        Toast.makeText(ContenidosGuardados.this, "El contenido no ha sido georreferenciado.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    georreferenciar(contenidos.get(position).getId_contenido(), contenidos.get(position).getId_usuario(), contenidos.get(position).getNombre_contenido());
                }
                realmAdapter.notifyItemChanged(position);
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
                        icon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher_refresh_coor);
                        RectF icon_dest = new RectF(itemView.getRight() - 2 * width, itemView.getTop() + width, itemView.getRight() - width, itemView.getBottom() - width);
                        c.drawBitmap(icon, null, icon_dest, p);
                    }
                    c.restore();

                }

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };
        ItemTouchHelper itemTouch = new ItemTouchHelper(simpleItemTouchHelper);
        itemTouch.attachToRecyclerView(realmRecyclerView.getRecycleView());
    }

    /**
     * Crea una menu popup para georreferenciar el contenido o actualizar la ficha.
     *
     * @param position
     * @param view              Es la vista donde se aparecera el menu de opciones.
     * @param id_contenido      Id de contenido de la ficha.
     * @param id_usuario        id de Usuario que esta conectado en la aplicacion.
     * @param nombre_contenido  Nombre de la ficha de contenido.
     * @param id_tipo_contenido Id de tipo de contenido de la ficha.
     * @param id_relacion       Id de relacion ug.
     * @param latitud           Latitud de la coordenada.
     * @param longitud          Longitud de la coordenada.
     */
    private void crearPopUp(final int position, View view, final String id_contenido, final String id_usuario, final String nombre_contenido, final String id_tipo_contenido, final String id_relacion, final String latitud, final String longitud) {
        PopupMenu popupMenu = new PopupMenu(ContenidosGuardados.this, view, Gravity.CENTER_HORIZONTAL);
        popupMenu.inflate(R.menu.menu_realm);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.sincronizar:
                        if (latitud != null && longitud != null) {
                            //Se encarga de guardar las coordenadas.
                            guardarCoordenadas(id_contenido, id_tipo_contenido, id_relacion, latitud, longitud);
                        } else {
                            Toast.makeText(ContenidosGuardados.this, "El contenido no ha sido georreferenciado.", Toast.LENGTH_SHORT).show();
                        }

                        break;
                    case R.id.georreferenciar:
                        georreferenciar(id_contenido, id_usuario, nombre_contenido);
                        break;
                    case R.id.eliminar_contenido:
                        eliminarContenido(position, id_usuario, id_contenido);
                        break;
                }
                return true;
            }
        });

        popupMenu.show();

    }

    private void eliminarContenido(final int position, final String id_usuario, final String id_contenido) {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                Contenidos contenido = realm.where(Contenidos.class)
                        .equalTo("id_usuario", id_usuario)
                        .equalTo("id_contenido", id_contenido)
                        .findFirst();
                contenido.deleteFromRealm();
            }
        });
    }

    /**
     * Georreferencia el contenido guardado con un par de coordenadas.
     *
     * @param id_contenido     Id de contenido de la ficha.
     * @param id_usuario       id de Usuario que esta conectado en la aplicacion.
     * @param nombre_contenido Nombre de la ficha de contenido.
     */
    private void georreferenciar(String id_contenido, String id_usuario, String nombre_contenido) {
        Bundle bundle = new Bundle();
        bundle.putString("tipoTarea", "3");
        bundle.putString("id_contenido", id_contenido);
        bundle.putString("nombre_contenido", nombre_contenido);
        bundle.putString("id_usuario", id_usuario);

        Intent intent = new Intent(getApplicationContext(), CacheDownloader.class);
        intent.putExtras(bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);

    }

    /**
     * Crea un dialogo de alerta, en caso de confirmar..
     * Se encarga de sincronizar el contenido elegido de la base de datos local con un par de coordenadas
     *
     * @param id_contenido      Id de contenido de la ficha.
     * @param id_tipo_contenido Id de tipo de contenido de la ficha.
     * @param id_relacion       Id de relacion ug.
     * @param latitud           Latitud de la coordenada.
     * @param longitud          longitud de la coordenada.
     */
    private void guardarCoordenadas(final String id_contenido,
                                    final String id_tipo_contenido,
                                    final String id_relacion,
                                    final String latitud,
                                    final String longitud) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ContenidosGuardados.this);
        builder.setTitle("AVISO");

        builder.setMessage("Ficha Id: " + id_contenido + "\n\n¿Esta seguro que desea guardar las coordenadas? \n" + "\nLatitud: " + latitud + "\nLongitud: " + longitud)
                .setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        loading = ProgressDialog.show(ContenidosGuardados.this, "Obteniendo datos", "Por favor, espere...", false, false);

                        String urlBusqueda = prefs.getString(Config.URL_SHARED, "no disp") + "/exp/exp_modulo294.php";
                        StringWriter writer = buscarXml(id_contenido, id_tipo_contenido, id_relacion, latitud, longitud);
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
                                        Toast.makeText(ContenidosGuardados.this, "Error : " + mensajeTexto, Toast.LENGTH_LONG).show();
                                    }
                                    if (estadoTexto.equals("0")) {
                                        // Toast.makeText(context, "Proceso Manual Finalizado", Toast.LENGTH_SHORT).show();
                                        Snackbar.make(findViewById(R.id.localLayout), "Proceso local Finalizado",
                                                Snackbar.LENGTH_SHORT)
                                                .show();
                                        /*
                                        realm.executeTransaction(new Realm.Transaction() {
                                            @Override
                                            public void execute(Realm realm) {
                                                Contenidos contenido = realm.where(Contenidos.class).equalTo("id_usuario", prefs.getString(Config.ID_USUARIO_SHARED,"")).equalTo("id_contenido", id_contenido).findFirst();
                                                contenido.deleteFromRealm();
                                            }
                                        });*/

                                    }
                                } else {
                                    loading.dismiss();
                                    Toast.makeText(ContenidosGuardados.this, "Respuesta fallo", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onFailure(Call<Idatum> call, Throwable t) {
                                loading.dismiss();
                                Toast.makeText(ContenidosGuardados.this, "Error: " + t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();

                            }
                        });

                    }
                }).setNegativeButton("Cancel", null);
        builder.create();
        builder.show();

    }

    /**
     * Realiza la estructura XML que se encarga de comunicarse con el WebService.
     *
     * @param id_contenido      Id de contenido de la ficha.
     * @param id_tipo_contenido Id de tipo de contenido de la ficha.
     * @param id_relacion       Id de relacion ug.
     * @param latitudSend       Latitud de la coordenada.
     * @param longitudSend      longitud de la coordenada.
     * @return Retorna la estructura XML dentro de un StringWriter.
     */
    private StringWriter buscarXml(String id_contenido, String id_tipo_contenido, String id_relacion, String latitudSend, String longitudSend) {
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
                id_contenido,
                id_relacion,
                Double.parseDouble(latitudSend),
                Double.parseDouble(longitudSend),
                id_tipo_contenido,
                tipo_rel,
                "",
                "",
                "",
                "",
                new ArrayList<AtributosDinamicos>());

        return writer;
    }

    /**
     * Crea el menu de opciones en la barra de acciones
     *
     * @param menu Menu a crear
     * @return Retorna el menu configurado
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.contenidos_guardados, menu);
        MenuItem menuItem = menu.findItem(R.id.buscarCont);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setQueryHint("Contenidos Locales");
        searchView.setOnQueryTextListener(this);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Realiza las acciones al presionar cada item del menu
     *
     * @param item El item seleccionado
     * @return retorna una accion
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.eliminar_todos:
                eliminarTodos();

                break;
            default:
                return super.onOptionsItemSelected(item);

        }
        return super.onOptionsItemSelected(item);
    }

    private void eliminarTodos() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ContenidosGuardados.this);
        builder.setTitle("AVISO");
        builder.setMessage("¿Esta seguro que desea eliminar todos los elementos?")
                .setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        realm.executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                realm.delete(Contenidos.class);
                            }
                        });
                    }
                }).setNegativeButton("Cancelar", null);
        builder.create();
        builder.show();
    }
        /*
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        menu.setHeaderTitle("Contenido: " + contenidos.get(info.position).getId_contenido());

        getMenuInflater().inflate(R.menu.context_realm, menu);
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.borrar_contenido:
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        contenidos.get(info.position).deleteFromRealm();
                    }
                });
                break;

        }
        return super.onContextItemSelected(item);
    }
        */

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    /**
     * Realiza una accion cada vez que el texto cambia
     *
     * @param newText Se refiere a el texto que cambio
     * @return retorna una accion
     */
    @Override
    public boolean onQueryTextChange(String newText) {
        realmAdapter.getFilter().filter(newText);
        return true;
    }

    /**
     * Cierra la base de datos al cerrar la aplicacion.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }
}
