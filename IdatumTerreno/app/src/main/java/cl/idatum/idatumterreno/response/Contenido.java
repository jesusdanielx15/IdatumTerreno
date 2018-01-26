package cl.idatum.idatumterreno.response;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
/**
 * Representacion de la estructura XML de idatum. El tag contenido.
 * @author Jesus Cañizales <jcanizales@siigsa.cl>
 * @version 2
 * @package cl.idatum.idatumterreno Proyecto IdatumTerreno
 * @link http://www.siigsa.cl
 * @copyright SIIGSA, Propiedad Intelectual y Derechos Patrimoniales de Software y Base de Datos i-datum. Registro Propiedad Intelectual Nº 211.351 y 211.352 respectivamente, con fecha 22 de noviembre del 2011
 * @since 25/10/2017
 */
@Root(name = "contenido", strict = false)
public class Contenido {

    @Attribute
    private String id;
    //  private int nombre;
    @Element(name = "nombre", required = false)
    private String nombre;
    @Element(name = "descripcion", required = false)
    private String descripcion;

    @Element(name = "relaciones", required = false)
    private Relaciones relaciones;

    @Element
    private Respuesta respuesta;

    public Respuesta getRespuesta() {
        return respuesta;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getId() {
        return id;
    }

    public Relaciones getRelaciones() {
        return relaciones;
    }
}

