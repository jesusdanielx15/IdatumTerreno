package cl.idatum.idatumterreno.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;

import cl.idatum.idatumterreno.R;
import cl.idatum.idatumterreno.api.ApiUtils;
import cl.idatum.idatumterreno.elena.ElenaExcepciones;
import cl.idatum.idatumterreno.elena.Funciones;
import cl.idatum.idatumterreno.response.LoginResponse;
import cl.idatum.idatumterreno.utils.Config;
import okhttp3.ResponseBody;
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
public class SistemaActivity extends AppCompatActivity {

    private EditText urlSistema;

    private TextInputEditText usuario, clave;
    private Button go;
    private ImageView urlCheck;
    private SharedPreferences prefs;
    private ProgressBar progressBar;
    private String TAG = "SistemaActivity";

    /**
     * Metodo principal de la clase
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sistema);

        Toolbar toolbar = findViewById(R.id.appbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Typeface face = Typeface.createFromAsset(getAssets(), "fonts/oxygenregular.ttf");


        prefs = getSharedPreferences(Config.NOMBRE_SHARED, Context.MODE_PRIVATE);

        urlSistema = findViewById(R.id.urlSistema);
        usuario = findViewById(R.id.usuario);
        clave = findViewById(R.id.clave);
        urlCheck = findViewById(R.id.urlCheck);
        progressBar = findViewById(R.id.progressBar);
        go = findViewById(R.id.boton);


        setTipoDeLetra(face);

        urlSistema.setText(prefs.getString(Config.URL_SHARED, ""));

        urlSistema.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                verificarUrl(b);
            }
        });

        go.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                String usuario64 = Funciones.Base64_encode(usuario.getText().toString());
                String clave64 = Funciones.Base64_encode(clave.getText().toString());
                final String urlText = urlSistema.getText().toString();
                Call<LoginResponse> createUserCall = ApiUtils.obtenerCliente().verificarUsuario("validar_usuario",
                        urlText + "/exp/login.php",
                        usuario64,
                        clave64);
                createUserCall.enqueue(new Callback<LoginResponse>() {
                    @Override
                    public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {

                        if ("true".equals(response.body().getExc_res())) {
                            //


                            if (FirebaseInstanceId.getInstance().getToken() != null) {
                                sendTokenToServer(FirebaseInstanceId.getInstance().getToken(),
                                        usuario.getText().toString(),
                                        clave.getText().toString(),
                                        response.body().getExc_cod(),
                                        urlText);
                            }

                        } else {
                            progressBar.setVisibility(View.INVISIBLE);
                            Toast.makeText(SistemaActivity.this, "Usuario y clave no coinciden", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<LoginResponse> call, Throwable t) {
                        //respuesta.setText(t.getLocalizedMessage());
                        progressBar.setVisibility(View.INVISIBLE);
                        Toast.makeText(SistemaActivity.this, "Error " + t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    private void sendTokenToServer(String token, final String usuario, final String clave, final String exc_cod, final String urlText) {

        Call<ElenaExcepciones> call = ApiUtils.obtenerCliente().registerToken(urlText + "/exp/exp_modulo294.php", "registrar_token", exc_cod, token);

        call.enqueue(new Callback<ElenaExcepciones>() {
            @Override
            public void onResponse(Call<ElenaExcepciones> call, Response<ElenaExcepciones> response) {
                Log.d(TAG, "onResponse: " + response.raw().toString());
                progressBar.setVisibility(View.INVISIBLE);
                if (response.body().getExe_est_res().equals("true")) {
                    Toast.makeText(SistemaActivity.this, "Bienvenido a " + urlText, Toast.LENGTH_SHORT).show();
                    saveOnPreferences(usuario, clave, exc_cod, urlText);
                    goToMain();
                } else {
                    Toast.makeText(SistemaActivity.this, "Error, intente de nuevo", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ElenaExcepciones> call, Throwable t) {
                progressBar.setVisibility(View.INVISIBLE);
                Toast.makeText(SistemaActivity.this, "Error", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Fallo respuesta token " + t.getLocalizedMessage());
            }
        });
    }


    private void verificarUrl(boolean b) {
        if (b != true) {
            Call<ResponseBody> call = ApiUtils.obtenerCliente().verificarUrl(urlSistema.getText().toString());

            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {

                        urlCheck.setImageResource(R.drawable.ic_action_check);
                        urlCheck.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    urlCheck.setImageResource(R.drawable.ic_action_error);
                    Log.e("SistemaActivity.class", "onFailure: " + t.getLocalizedMessage());
                    urlCheck.setVisibility(View.VISIBLE);
                }
            });
        } else {
            urlCheck.setVisibility(View.INVISIBLE);
        }
    }

    private void setTipoDeLetra(Typeface face) {
        urlSistema.setTypeface(face);
        usuario.setTypeface(face);
        clave.setTypeface(face);
        go.setTypeface(face);
    }


    private void saveOnPreferences(String usuario, String clave, String exc_cod, String urlText) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(Config.LOGIN_SHARED, true);
        editor.putBoolean(Config.FIRST_TIME, true);
        editor.putString(Config.USUARIO_SHARED, usuario);
        editor.putString(Config.CLAVE_SHARED, clave);
        editor.putString(Config.URL_SHARED, urlText);
        editor.putString(Config.ID_USUARIO_SHARED, exc_cod);
        editor.commit();
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


}
