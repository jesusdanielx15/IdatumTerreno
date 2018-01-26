package cl.idatum.idatumterreno.ServicioNotificationPush;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import cl.idatum.idatumterreno.api.ApiUtils;
import cl.idatum.idatumterreno.elena.ElenaExcepciones;
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
 * @since 22/1/2018
 */
public class FirebaseInstanceIDService extends FirebaseInstanceIdService {

    private static final String TAG = "FirebaseToken";

    @Override
    public void onTokenRefresh() {
        String token = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "onTokenRefresh: " + token);


        SharedPreferences prefs = getSharedPreferences(Config.NOMBRE_SHARED, Context.MODE_PRIVATE);
        String url = prefs.getString(Config.URL_SHARED, "") + "/exp/exp_modulo294.php";
        String id_usuario = prefs.getString(Config.ID_USUARIO_SHARED, "");
        if (prefs.getBoolean(Config.LOGIN_SHARED, false)) {
            registerToken(token,url,id_usuario);
        }
    }

    private void registerToken(String token, String url, String id_usuario) {
        Call<ElenaExcepciones> call = ApiUtils.obtenerCliente().registerToken(url, "registrar_token", id_usuario, token);

        call.enqueue(new Callback<ElenaExcepciones>() {
            @Override
            public void onResponse(Call<ElenaExcepciones> call, Response<ElenaExcepciones> response) {
                Log.d(TAG, "onResponse: " + response.raw().toString());
            }

            @Override
            public void onFailure(Call<ElenaExcepciones> call, Throwable t) {

                Log.e(TAG, "onFailure: " + t.getLocalizedMessage());
            }
        });
    }
}
