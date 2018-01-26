package cl.idatum.idatumterreno.api;

/**
 * @author Jesus Cañizales <jcanizales@siigsa.cl>
 * @version 2
 * @package cl.idatum.idatumterreno Proyecto IdatumTerreno
 * @link http://www.siigsa.cl
 * @copyright SIIGSA, Propiedad Intelectual y Derechos Patrimoniales de Software y Base de Datos i-datum. Registro Propiedad Intelectual Nº 211.351 y 211.352 respectivamente, con fecha 22 de noviembre del 2011
 * @since 25/10/2017
 */
public class ApiUtils {

    private ApiUtils(){};

    public  static final String BASE_URL= "http://a/";

    /**
     * Obtiene y crea el cliente Retrofit
     * @return Retorna el cliente.
     */
    public static RetrofitApi obtenerCliente() {
        return ApiClient.getClient(BASE_URL).create(RetrofitApi.class);
    }
}
