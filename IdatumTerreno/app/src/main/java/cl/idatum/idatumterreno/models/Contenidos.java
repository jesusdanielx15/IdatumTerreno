package cl.idatum.idatumterreno.models;

import java.util.Date;

import cl.idatum.idatumterreno.app.MyApplication;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

/**
 * Clase Contenidos que representa la estructura de la tabla de base de datos con sus respectivas columnas.
 *
 * @author Jesus Cañizales <jcanizales@siigsa.cl>
 * @version 2
 * @package cl.idatum.idatumterreno Proyecto IdatumTerreno
 * @link http://www.siigsa.cl
 * @copyright SIIGSA, Propiedad Intelectual y Derechos Patrimoniales de Software y Base de Datos i-datum. Registro Propiedad Intelectual Nº 211.351 y 211.352 respectivamente, con fecha 22 de noviembre del 2011
 * @since 25/10/2017
 */

public class Contenidos extends RealmObject {
    @PrimaryKey
    private int id;
    @Required
    private String id_contenido;
    @Required
    private String id_usuario;
    @Required
    private String nombre_contenido;
    private String tipo_id_contenido;
    private String latitud;

    private String longitud;

    private String id_relacion;
    @Required
    private Date fecha_creacion;

    public Contenidos() {

    }

    /**
     * Constructor de la clase contenidos.
     *
     * @param id_contenido      Id de contenido de la ficha.
     * @param id_usuario        Id de usuario actual.
     * @param nombre_contenido  Nombre de la ficha de contenido.
     * @param tipo_id_contenido Tipo de id de contenido.
     * @param latitud           LAtitud de la coordenada
     * @param longitud          Longitud de la coordenada
     * @param id_relacion       Id de relacion.
     */
    public Contenidos(String id_contenido, String id_usuario, String nombre_contenido, String tipo_id_contenido, String latitud, String longitud, String id_relacion) {
        this.tipo_id_contenido = tipo_id_contenido;
        this.id = MyApplication.ContenidosId.incrementAndGet();
        this.id_contenido = id_contenido;
        this.id_usuario = id_usuario;
        this.nombre_contenido = nombre_contenido;
        this.latitud = latitud;
        this.longitud = longitud;
        this.id_relacion = id_relacion;
        this.fecha_creacion = new Date();
    }

    public int getId() {
        return id;
    }


    public String getId_contenido() {
        return id_contenido;
    }

    public void setId_contenido(String id_contenido) {
        this.id_contenido = id_contenido;
    }

    public String getId_usuario() {
        return id_usuario;
    }

    public void setId_usuario(String id_usuario) {
        this.id_usuario = id_usuario;
    }

    public String getLatitud() {
        return latitud;
    }

    public void setLatitud(String latitud) {
        this.latitud = latitud;
    }

    public String getLongitud() {
        return longitud;
    }

    public void setLongitud(String longitud) {
        this.longitud = longitud;
    }

    public String getId_relacion() {
        return id_relacion;
    }

    public void setId_relacion(String id_relacion) {
        this.id_relacion = id_relacion;
    }

    public Date getFecha_creacion() {
        return fecha_creacion;
    }

    public String getNombre_contenido() {
        return nombre_contenido;
    }

    public void setNombre_contenido(String nombre_contenido) {
        this.nombre_contenido = nombre_contenido;
    }

    public String getTipo_id_contenido() {
        return tipo_id_contenido;
    }

    public void setTipo_id_contenido(String tipo_id_contenido) {
        this.tipo_id_contenido = tipo_id_contenido;
    }
}
