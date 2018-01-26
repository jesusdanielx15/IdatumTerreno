package cl.idatum.idatumterreno.api;


import android.database.Observable;

import cl.idatum.idatumterreno.elena.ElenaExcepciones;
import cl.idatum.idatumterreno.response.Idatum;
import cl.idatum.idatumterreno.response.LoginResponse;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

/**
 * @author Jesus Cañizales <jcanizales@siigsa.cl>
 * @version 2
 * @package cl.idatum.idatumterreno Proyecto IdatumTerreno
 * @link http://www.siigsa.cl
 * @copyright SIIGSA, Propiedad Intelectual y Derechos Patrimoniales de Software y Base de Datos i-datum. Registro Propiedad Intelectual Nº 211.351 y 211.352 respectivamente, con fecha 22 de noviembre del 2011
 * @since 25/10/2017
 */
public interface RetrofitApi {

    @POST
    @FormUrlEncoded
    Call<ElenaExcepciones> registerToken(@Url String url,
                                     @Field("modaccion") String registrarToken,
                                     @Field("id_usuario") String id_usuario,
                                     @Field("token") String token);

    /**
     * Verifica que URL insertada en la actividad de login exista.
     *
     * @param url Es la URL a verificar.
     * @return
     */
    @GET
    Call<ResponseBody> verificarUrl(@Url String url);

    /**
     * Verificar usuario existente en la base de datos del sistema.
     *
     * @param validar_usuario Modaccion usado para identificar este proceso.
     * @param url             Es la url del sistema a conectarse.
     * @param usuario64       Usuario codificado en base64.
     * @param clave64         Clave codificado en base64.
     * @return
     */
    @POST
    @FormUrlEncoded
    Call<LoginResponse> verificarUsuario(@Field("modaccion") String validar_usuario,
                                         @Url String url,
                                         @Field("log_use") String usuario64,
                                         @Field("log_pas") String clave64);


    /**
     * Se encarga de buscar todos los contenidos relacionados a una palabra de busqueda o id.
     *
     * @param url       Es la url del sistema a conectarse.
     * @param modaccion Modaccion usado para identificar este proceso.
     * @param xml       Es la estructura xml que se encarga de hacer la consulta
     * @return
     */
    @POST
    @FormUrlEncoded
    Call<Idatum> obtenerContenido(@Url String url,
                                  @Field("modaccion") String modaccion,
                                  @Field("xml") String xml);

    /**
     * Obtiene los diferentes datos de una ficha de contenido del sistema necesarios para
     * georeferenciar esta ficha en caso de que se haya presionado el boton en la ficha.
     *
     * @param url       Es la url del sistema a conectarse.
     * @param registrar Modaccion usado para identificar este proceso.
     * @param id        Id de usuario usado para buscar la tarea.
     * @return
     */
    @POST
    @FormUrlEncoded
    Call<ElenaExcepciones> buscarTarea(@Url String url,
                                       @Field("modaccion") String registrar,
                                       @Field("id_usuario") String id);

    /**
     * Terminar la georeferenciacion haciendo que se refresque la ficha donde fue presionado del boton para georeferenciar.
     *
     * @param url       Es la url del sistema a conectarse.
     * @param registrar Modaccion usado para identificar este proceso.
     * @param elena     Objeto con toda la informacion a enviar.
     * @return
     */
    @POST
    @FormUrlEncoded
    Call<ElenaExcepciones> finalizarTarea(@Url String url,
                                          @Field("modaccion") String registrar,
                                          @Field("proceso") String elena);

    /**
     * Hace llamadas al WebService y recibe respuestas en formato XML que luego sera deserealizado.
     *
     * @param url       Es la url del sistema a conectarse.
     * @param modaccion Modaccion usado para identificar este proceso.
     * @param xml       Es la estructura xml que se encarga de hacer la peticion.
     * @return
     */
    @POST
    @FormUrlEncoded
    Call<Idatum> xmlWebService(@Url String url,
                               @Field("modaccion") String modaccion,
                               @Field("xml") String xml);

    /**
     * Obtiene todos los id de portal de cada sistema al que se le indica mediante la URL.
     *
     * @param url       Es la url del sistema a conectarse.
     * @param modaccion Modaccion usado para identificar este proceso.
     * @return
     */
    @POST
    @FormUrlEncoded
    Call<ElenaExcepciones> obtenerIdPortal(@Url String url,
                                           @Field("modaccion") String modaccion);

    /**
     * Obtiene todos los id de tipo de contenido de cada sistema al que se le indica mediante la URL.
     *
     * @param url       Es la url del sistema a conectarse.
     * @param modaccion Modaccion usado para identificar este proceso.
     * @return
     */
    @POST
    @FormUrlEncoded
    Call<ElenaExcepciones> obtenerIdTipoCont(@Url String url,
                                             @Field("modaccion") String modaccion);

    /**
     * Obtiene todos los id de relacion (dado un tipo de contenido) de cada sistema al que se le indica mediante la URL.
     *
     * @param url          Es la url del sistema a conectarse.
     * @param modaccion    Modaccion usado para identificar este proceso.
     * @param id_tipo_cont Id de tipo de contenido necesario para buscar todos sus id de relacion correspondiente.
     * @param idUsuario    Id de usuario usado para obtener los Id de relacion.
     * @return
     */
    @POST
    @FormUrlEncoded
    Call<ElenaExcepciones> obtenerIdRelacion(@Url String url,
                                             @Field("modaccion") String modaccion,
                                             @Field("id_tipo_cont") String id_tipo_cont,
                                             @Field("id_usuario") String idUsuario);

    /**
     * Obtiene la informacion de un mapa por su id de mapa, obtiene un objeto json
     * conteniendo info de sus cubiertas.
     *
     * @param url       Es la url del sistema a conectarse.
     * @param modaccion Modaccion usado para identificar este proceso.
     * @param obj_id    Es el id del mapa del cual se obtendra la info.
     * @return
     */
    @POST
    @FormUrlEncoded
    Call<ElenaExcepciones> obtenerInfoMap(@Url String url,
                                          @Field("modaccion") String modaccion,
                                          @Field("id_mapa") String obj_id);


    /**
     * Obtiene la estructura del pvc dado un tipo de contenido.
     *
     * @param url       Es la url del sistema a conectarse.
     * @param modaccion Modaccion usado para identificar este proceso.
     * @param obj_id    Es el id de la ficha que esta siendo creada con la estructura del pvc.
     * @return
     */
    @POST
    @FormUrlEncoded
    Call<ElenaExcepciones> obtenerIdPvc(@Url String url,
                                        @Field("modaccion") String modaccion,
                                        @Field("objId") String obj_id);


    /**
     * Obtiene los datos correspondientes al atributo conceptual multiple o simple,
     * dependiento del caso.
     *
     * @param url Es la url del sistema a conectarse.
     * @param modaccion Modaccion usado para identificar este proceso.
     * @param plaVisConEstAtrRelTip Es el tipo de dato, puede ser Dato Basico, Atr o Estado.
     * @param atributoTipo Es el tipo de atributo.
     * @param atributoId Es el id del atributo.
     * @return
     */
    @POST
    @FormUrlEncoded
    Call<ElenaExcepciones> obtenerDatosAtributos(@Url String url,
                                                 @Field("modaccion") String modaccion,
                                                 @Field("tipo") String plaVisConEstAtrRelTip,
                                                 @Field("tipAtr") int atributoTipo,
                                                 @Field("atrId") int atributoId);

    @GET
    @Streaming
    Call<ResponseBody> downloadFileWithDynamicUrlSync(@Url String fileUrl);


}
