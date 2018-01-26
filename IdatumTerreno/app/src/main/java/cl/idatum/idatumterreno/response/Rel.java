package cl.idatum.idatumterreno.response;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Representacion de la estructura XML de idatum. El tag rel.
 *
 * @author Jesus Cañizales <jcanizales@siigsa.cl>
 * @version 2
 * @package cl.idatum.idatumterreno Proyecto IdatumTerreno
 * @link http://www.siigsa.cl
 * @copyright SIIGSA, Propiedad Intelectual y Derechos Patrimoniales de Software y Base de Datos i-datum. Registro Propiedad Intelectual Nº 211.351 y 211.352 respectivamente, con fecha 22 de noviembre del 2011
 * @since 25/10/2017
 */
@Root(name = "rel", strict = false)
public class Rel {

    @Attribute(name = "id", required = false)
    private String id2;

    @Element(required = false)
    private Registros registros;


    public String getId2() {
        return id2;
    }

    public Registros getRegistros() {
        return registros;
    }


}
