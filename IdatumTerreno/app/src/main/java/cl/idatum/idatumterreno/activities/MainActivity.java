package cl.idatum.idatumterreno.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import cl.idatum.idatumterreno.R;
import cl.idatum.idatumterreno.api.ApiUtils;
import cl.idatum.idatumterreno.elena.ElenaExcepciones;
import cl.idatum.idatumterreno.fragments.ContenidosFragment;
import cl.idatum.idatumterreno.fragments.MapFragment;
import cl.idatum.idatumterreno.utils.Config;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Actividad principal de la aplicacion que contiene el navigation drawer y el contenedor de fragmentos
 *
 * @author Jesus Cañizales <jcanizales@siigsa.cl>
 * @version 2
 * @package cl.idatum.idatumterreno Proyecto IdatumTerreno
 * @link http://www.siigsa.cl
 * @copyright SIIGSA, Propiedad Intelectual y Derechos Patrimoniales de Software y Base de Datos i-datum. Registro Propiedad Intelectual Nº 211.351 y 211.352 respectivamente, con fecha 22 de noviembre del 2011
 * @since 25/10/2017
 */
public class MainActivity extends AppCompatActivity {


    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private TextView navHeaderTitle, navHeaderSubTitle;
    private SharedPreferences prefs;
    private String nav_userText;
    private Boolean conectado = false;
    private EditText contenidos;
    private Toolbar toolbar;
    private String TAG = "Main";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        registrarToken();
        setToolbar();
        iniciarVistas();
        setNavName();
        setFragment();

        contenidos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cambiarContenidosFragment();
            }
        });

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                boolean fragmentTransaction = false;
                Fragment fragment = null;
                switch (item.getItemId()) {
                    case R.id.mapFrag:
                        fragment = new MapFragment();
                        fragmentTransaction = true;
                        break;
                    case R.id.osmDownload:
                        startActivity(new Intent(getApplicationContext(), CacheDownloader.class));
                        break;
                    case R.id.menu_cerrar:
                        if (prefs.getBoolean(Config.LOGIN_SHARED, false)) {
                            cerrarSesion();
                        } else {
                            Toast.makeText(MainActivity.this, "No hay usuario logueado.", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case R.id.login:
                        ingresar();
                        break;
                    case R.id.configBusqueda:
                        irConfig();
                        break;
                    case R.id.contenidos_guardados:
                        irContenidosLocales();
                        break;
                    case R.id.cambios_estados:
                        if (prefs.getBoolean(Config.LOGIN_SHARED, false)) {
                            startActivity(new Intent(MainActivity.this,CambioEstados.class));
                        } else {
                            Toast.makeText(MainActivity.this, "Primero debe ingresar al sistema", Toast.LENGTH_SHORT).show();
                        }
                        break;
                }
                if (fragmentTransaction) {
                    changeFragment(fragment, item);
                    drawerLayout.closeDrawers();
                }
                return true;
            }
        });
    }

    private void registrarToken() {
        FirebaseMessaging.getInstance().subscribeToTopic("test");
        String token = FirebaseInstanceId.getInstance().getToken();
    }

    /**
     * Referencia todas las vistas
     */
    private void iniciarVistas() {
        Typeface face = Typeface.createFromAsset(getAssets(), "fonts/oxygenregular.ttf");
        prefs = getSharedPreferences(Config.NOMBRE_SHARED, Context.MODE_PRIVATE);
        navigationView = findViewById(R.id.navView);
        drawerLayout = findViewById(R.id.drawer_layout);
        View hView = navigationView.getHeaderView(0);
        navHeaderTitle = hView.findViewById(R.id.navHeaderTitle);
        navHeaderSubTitle = hView.findViewById(R.id.navHeaderSubTitle);
        contenidos = findViewById(R.id.contenidos);
        navHeaderTitle.setTypeface(face);
        navHeaderSubTitle.setTypeface(face);
    }

    /**
     * Redirige a la actividad de contenidos locales.
     */
    private void irContenidosLocales() {
        conectado = prefs.getBoolean(Config.LOGIN_SHARED, false);
        if (conectado) {
            startActivity(new Intent(getApplicationContext(), ContenidosGuardados.class));
        } else {
            Toast.makeText(this, "Primero debe ingresar al sistema", Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * Redirige a la actividad de configuraciones de id.
     */
    private void irConfig() {
        conectado = prefs.getBoolean(Config.LOGIN_SHARED, false);
        if (conectado) {
            startActivity(new Intent(MainActivity.this, ConfigBusqueda.class));
        } else {
            Toast.makeText(this, "Primero debe ingresar al sistema", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Metodo que redirige a la actividad de login.
     */
    private void ingresar() {
        conectado = prefs.getBoolean(Config.LOGIN_SHARED, false);
        if (conectado) {
            Toast.makeText(this, "Usuario logueado", Toast.LENGTH_SHORT).show();
        } else {
            startActivity(new Intent(MainActivity.this, SistemaActivity.class));
        }
    }

    /**
     * Configura el nombre del menu lateral de la palicacion.
     */
    private void setNavName() {
        nav_userText = prefs.getString(Config.USUARIO_SHARED, "");
        if (!nav_userText.equals("")) {
            navHeaderSubTitle.setText("Usuario Conectado: " + prefs.getString(Config.USUARIO_SHARED, ""));
        } else {
            navHeaderSubTitle.setText("Usuario no Conectado");
        }
    }

    /**
     * Sustitutye el fragmento actual por otro fragmento.
     *
     * @param fragment fragmento que sera sustituido.
     * @param item     Item que fue presionado en el menu lateral para cambiar de fragmento.
     */
    private void changeFragment(Fragment fragment, MenuItem item) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.content_frame, fragment)
                .commit();
        item.setChecked(true);
        getSupportActionBar().setTitle("");
    }

    /**
     * Se encarga de sleccionar el fragmento por defecto al iniciar la aplicacion, el cual es el fragmento de mapa.
     */
    private void setFragment() {
        Fragment fragment;
        Bundle bundle = getIntent().getExtras();
        fragment = new MapFragment();
        if (bundle != null) {
            fragment.setArguments(bundle);
        }
        changeFragment(fragment, navigationView.getMenu().getItem(0));
    }

    /**
     * Se encarga de configurar una toolbar como barra de opciones de la aplicacion.
     */
    private void setToolbar() {
        toolbar = findViewById(R.id.appbar);
        setSupportActionBar(toolbar);
        //Pone la imagen de menu al appbar
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_action_menu);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    /**
     * Cierra la sesion actual en la aplicacion y borra los datos del usuario actual.
     */
    private void cerrarSesion() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("AVISO");
        builder.setMessage("¿Esta seguro que desea salir?")
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        borrarToken();
                    }
                }).setNegativeButton("Cancelar", null);
        builder.create();
        builder.show();
    }

    /**
     * Borrar token actual
     */
    private void borrarToken() {
        String url = prefs.getString(Config.URL_SHARED, "");
        String id_usuario = prefs.getString(Config.ID_USUARIO_SHARED, "");
        String token = FirebaseInstanceId.getInstance().getToken();
        Call<ElenaExcepciones> call = ApiUtils.obtenerCliente().registerToken(url + "/exp/exp_modulo294.php", "borrar_token", id_usuario, token);

        call.enqueue(new Callback<ElenaExcepciones>() {
            @Override
            public void onResponse(Call<ElenaExcepciones> call, Response<ElenaExcepciones> response) {
                Log.d(TAG, "onResponse: " + response.raw().toString());
                if (response.body().getExe_est_res().equals("true")) {
                    borrarDatosSesion();
                }
            }

            @Override
            public void onFailure(Call<ElenaExcepciones> call, Throwable t) {

                Log.e(TAG, "onFailure: " + t.getLocalizedMessage());
            }
        });

    }

    private void borrarDatosSesion() {
        borrarPreferencias();
        setNavName();

        MapFragment mapFragment = (MapFragment) getSupportFragmentManager().findFragmentById(R.id.content_frame);
        mapFragment.checkFabTools();

        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private void borrarPreferencias() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(Config.USUARIO_SHARED, "");//
        editor.putString(Config.ID_USUARIO_SHARED, "");//
        editor.putString(Config.CLAVE_SHARED, "");//
        editor.putBoolean(Config.LOGIN_SHARED, false);//
        editor.putString(Config.ID_PORTAL, "");//
        editor.putString(Config.ID_TIPO_CONT_SHARED_, "");//
        editor.putString(Config.ID_RELACION_SHARED, "");//
        editor.putBoolean(Config.FIRST_TIME, false);//
        editor.putString(Config.ID_MAPA, "");//
        editor.putString(Config.ID_CONT_MAPA, ""); //

        editor.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                //abrir navigation drawer
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                drawerLayout.openDrawer(GravityCompat.START);
                break;
            case R.id.verconfig:
                verConfig();
                break;
            case R.id.buscarCont:
                cambiarContenidosFragment();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Cambia el fragmento actual por el ContenidosFragment.
     */
    private void cambiarContenidosFragment() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.content_frame, new ContenidosFragment())
                .commit();
    }

    /**
     * Muestra un dialogo de alerta que muestra las configuraciones actuales del usuario en la aplicacion.
     */
    private void verConfig() {

        final View dialogLayout = getLayoutInflater().inflate(R.layout.visor_configuraciones, null);
        int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.90);
        int height = (int) (getResources().getDisplayMetrics().heightPixels * 0.90);


        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

        builder.setView(dialogLayout);
        builder.setTitle("Configuraciones");
        //Vistas
        TextView textView = (TextView) dialogLayout.findViewById(R.id.textView);


        textView.setText("URL: " + prefs.getString(Config.URL_SHARED, "No disponible") + "\n"
                + "Id Portal: " + prefs.getString(Config.ID_PORTAL, "No disponible") + "\n"
                + "Id tipo de contenido: " + prefs.getString(Config.ID_TIPO_CONT_SHARED_, "No disponible") + "\n"
                + "Id Relacion: " + prefs.getString(Config.ID_RELACION_SHARED, "No disponible") + "\n"
                + "Id Mapa: " + prefs.getString(Config.ID_CONT_MAPA, "No disponible"));

        builder.setPositiveButton("OK", null);
        AlertDialog alertDialog = builder.create();
        alertDialog.getWindow().setLayout(600, 700);
        alertDialog.show();
    }
}
