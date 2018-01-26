package cl.idatum.idatumterreno.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import cl.idatum.idatumterreno.R;
import cl.idatum.idatumterreno.adapters.RealmAdapter;
import cl.idatum.idatumterreno.adapters.RealmAdapterCambioEstado;
import cl.idatum.idatumterreno.models.CambioEstadosObject;
import cl.idatum.idatumterreno.models.Contenidos;
import cl.idatum.idatumterreno.utils.Config;
import co.moonmonkeylabs.realmrecyclerview.RealmRecyclerView;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

public class CambioEstados extends AppCompatActivity implements RealmAdapterCambioEstado.OnRealmRecyclerViewItemClickListener {

    private Realm realm;
    private Toolbar toolbar;
    private SharedPreferences prefs;
    private RealmResults<CambioEstadosObject> cambioEstadosObjects;
    private RealmAdapterCambioEstado realmAdapterCambioEstado;
    private RealmRecyclerView realmRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cambio_estados);

        realm = Realm.getDefaultInstance();
        setToolbar();
        prefs = getSharedPreferences(Config.NOMBRE_SHARED, Context.MODE_PRIVATE);

        cambioEstadosObjects = realm.where(CambioEstadosObject.class).contains("id_usuario", prefs.getString(Config.ID_USUARIO_SHARED, "")).findAll().sort("id", Sort.DESCENDING);
        realmAdapterCambioEstado = new RealmAdapterCambioEstado(this, realm, cambioEstadosObjects, true, true);
        realmRecyclerView = findViewById(R.id.realmRecyclerContenidos);
        realmRecyclerView.addItemDecoration(new DividerItemDecoration(this, 1));
        realmRecyclerView.setAdapter(realmAdapterCambioEstado);
        realmRecyclerView.setClickable(true);

        realmAdapterCambioEstado.setOnItemClickListener(this);
    }

    private void setToolbar() {
        toolbar = findViewById(R.id.appbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onRealmRecyclerViewItemClicked(final int position, final String id_msg, final String nombre, final String obj_id, final String estado_actual, final String estado_anterior, final String estado_actual_color, final String estado_anterior_color, final String fecha, View view) {

        Log.e("TAG", "onRealmRecyclerViewItemClicked: " + position);
        PopupMenu popupMenu = new PopupMenu(CambioEstados.this, view, Gravity.CENTER_HORIZONTAL);
        popupMenu.inflate(R.menu.menu_cambio_estado);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.detalles_cambio_estado:
                        showDetails(position, id_msg, nombre, obj_id, estado_actual, estado_anterior, estado_actual_color, estado_anterior_color, fecha);
                        break;
                    case R.id.quitar_cambio_estado:
                        quitarCambioEstado(position, id_msg);
                        break;
                }
                return true;
            }
        });

        popupMenu.show();

    }

    private void quitarCambioEstado(final int position, final String id_msg) {
        Log.e("TAG", "quitarCambioEstado: " + position);
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                CambioEstadosObject cambioEstadosObject = realm.where(CambioEstadosObject.class)
                        .contains("id_usuario", prefs.getString(Config.ID_USUARIO_SHARED, ""))
                        .contains("id_msg", id_msg)
                        .findFirst();
                cambioEstadosObject.deleteFromRealm();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.e("TAG", "onPause: " );
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e("TAG", "onStop: " );
    }

    private void showDetails(final int position, final String id_msg, String nombre, String obj_id, String estado_actual, String estado_anterior, String estado_actual_color, String estado_anterior_color, String fecha) {

        AlertDialog.Builder builder = new AlertDialog.Builder(CambioEstados.this);
        builder.setTitle("Detalles");
        //Vistas
        String message = "Producto: " + obj_id + " - " + nombre + "<br><br>" +
                "Ha pasado de <b><font color=" + estado_anterior_color + ">Etapa " + estado_anterior + "</font></b>" +
                " a <b><font color=" + estado_actual_color + ">Etapa " + estado_actual + "</font></b>" +
                "<br>Con fecha de modificacion de Etapa: " + fecha + ".";
        builder.setMessage(Html.fromHtml(message));
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, final int i) {
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        CambioEstadosObject cambioEstadosObject = realm.where(CambioEstadosObject.class)
                                .contains("id_usuario", prefs.getString(Config.ID_USUARIO_SHARED, ""))
                                .contains("id_msg", id_msg)
                                .findFirst();
                        if (cambioEstadosObject.getLeido()) {
                            return;
                        } else {
                            cambioEstadosObject.setLeido(true);
                        }
                        realm.copyToRealmOrUpdate(cambioEstadosObject);
                        Log.e("TAG", "execute: " + id_msg + " position: " + position);
                        realmAdapterCambioEstado.notifyItemChanged(position);
                        //realmAdapterCambioEstado.notifyDataSetChanged();

                    }
                });
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.getWindow().setLayout(600, 700);
        alertDialog.show();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_eliminar_cambio_estados, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.eliminar_todos:
                eliminarTodos();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void eliminarTodos() {
        AlertDialog.Builder builder = new AlertDialog.Builder(CambioEstados.this);
        builder.setTitle("AVISO");
        builder.setMessage("¿Esta seguro que desea eliminar todos los elementos?")
                .setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        realm.executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                realm.delete(CambioEstadosObject.class);
                            }
                        });
                    }
                }).setNegativeButton("Cancelar", null);
        builder.create();
        builder.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }
}
