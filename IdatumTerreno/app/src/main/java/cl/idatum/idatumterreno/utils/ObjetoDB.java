package cl.idatum.idatumterreno.utils;

/**
 * Clase para configurar un objeto para representar los Datos Basicos de la estructura del PVC.
 *
 * @author Jesus Cañizales <jcanizales@siigsa.cl>
 * @version 2
 * @package cl.idatum.idatumterreno.utils Proyecto IdatumTerreno
 * @link http://www.siigsa.cl
 * @copyright SIIGSA, Propiedad Intelectual y Derechos Patrimoniales de Software y Base de Datos i-datum. Registro Propiedad Intelectual Nº 211.351 y 211.352 respectivamente, con fecha 22 de noviembre del 2011
 * @since 13/11/2017
 */
public class ObjetoDB {
    String objeto_id;
    String objeto_codigo;
    String objeto_nomb;
    String objeto_desc;
    String objeto_obs;


    public String getObjeto_id() {
        return objeto_id;
    }

    public void setObjeto_id(String objeto_id) {
        this.objeto_id = objeto_id;
    }

    public String getObjeto_codigo() {
        return objeto_codigo;
    }

    public void setObjeto_codigo(String objeto_codigo) {
        this.objeto_codigo = objeto_codigo;
    }

    public String getObjeto_nomb() {
        return objeto_nomb;
    }

    public void setObjeto_nomb(String objeto_nomb) {
        this.objeto_nomb = objeto_nomb;
    }

    public String getObjeto_desc() {
        return objeto_desc;
    }

    public void setObjeto_desc(String objeto_desc) {
        this.objeto_desc = objeto_desc;
    }

    public String getObjeto_obs() {
        return objeto_obs;
    }

    public void setObjeto_obs(String objeto_obs) {
        this.objeto_obs = objeto_obs;
    }


}
