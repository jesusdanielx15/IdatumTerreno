package cl.idatum.idatumterreno.response;


/**
 * Representa el objeto para leer la respuesta de login del servidor.
 * @author Jesus Cañizales <jcanizales@siigsa.cl>
 * @version 2
 * @package cl.idatum.idatumterreno Proyecto IdatumTerreno
 * @link http://www.siigsa.cl
 * @copyright SIIGSA, Propiedad Intelectual y Derechos Patrimoniales de Software y Base de Datos i-datum. Registro Propiedad Intelectual Nº 211.351 y 211.352 respectivamente, con fecha 22 de noviembre del 2011
 * @since 25/10/2017
 */
public class LoginResponse {

    private String exc_cod;

    private String exc_res;

    public String getExc_cod() {
        return exc_cod;
    }

    public void setExc_cod(String exc_cod) {
        this.exc_cod = exc_cod;
    }

    public String getExc_res() {
        return exc_res;
    }

    public void setExc_res(String exc_res) {
        this.exc_res = exc_res;
    }


}
