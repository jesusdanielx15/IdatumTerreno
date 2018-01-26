package cl.idatum.idatumterreno.api;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.simplexml.SimpleXmlConverterFactory;

/**
 * @author Jesus Cañizales <jcanizales@siigsa.cl>
 * @version 2
 * @package cl.idatum.idatumterreno Proyecto IdatumTerreno
 * @link http://www.siigsa.cl
 * @copyright SIIGSA, Propiedad Intelectual y Derechos Patrimoniales de Software y Base de Datos i-datum. Registro Propiedad Intelectual Nº 211.351 y 211.352 respectivamente, con fecha 22 de noviembre del 2011
 * @since 25/10/2017
 */
public class ApiClientXML {

    private static Retrofit retrofit = null;

    /**
     * Construye y configura el cliente Retrofit para realizar peticiones HTTP.
     * @param baseUrl Es la url base a conectarse.
     * @return
     */
    public static Retrofit getClient(String baseUrl) {
        if (retrofit == null) {

            final OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .readTimeout(60, TimeUnit.SECONDS)
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .build();

            retrofit=new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .client(new OkHttpClient())
                    .addConverterFactory(SimpleXmlConverterFactory.create())
                    .client(okHttpClient)
                    .build();
        }
        return retrofit;

    }


}