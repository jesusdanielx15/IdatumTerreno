package cl.idatum.idatumterreno.response;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Representacion de la estructura XML de idatum. El tag relaciones.
 *
 * @author Jesus Cañizales <jcanizales@siigsa.cl>
 * @version 2
 * @package cl.idatum.idatumterreno Proyecto IdatumTerreno
 * @link http://www.siigsa.cl
 * @copyright SIIGSA, Propiedad Intelectual y Derechos Patrimoniales de Software y Base de Datos i-datum. Registro Propiedad Intelectual Nº 211.351 y 211.352 respectivamente, con fecha 22 de noviembre del 2011
 * @since 25/10/2017
 */
@Root(name = "relaciones", strict = false)
public class Relaciones {
    @Element(required = false)
    private Rel rel;

    public Rel getRel() {
        return rel;
    }


}
