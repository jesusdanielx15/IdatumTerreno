package cl.idatum.idatumterreno.models;

import cl.idatum.idatumterreno.app.MyApplication;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

/**
 * @author Jesus Cañizales <jcanizales@siigsa.cl>
 * @version 2
 * @package cl.idatum.idatumterreno.models Proyecto IdatumTerreno
 * @link http://www.siigsa.cl
 * @copyright SIIGSA, Propiedad Intelectual y Derechos Patrimoniales de Software y Base de Datos i-datum. Registro Propiedad Intelectual Nº 211.351 y 211.352 respectivamente, con fecha 22 de noviembre del 2011
 * @since 25/1/2018
 */
public class CambioEstadosObject extends RealmObject {

    @PrimaryKey
    private int id;
    @Required
    private String id_msg;
    @Required
    private String id_usuario;
    @Required
    private String obj_id;

    @Required
    private String nombre;
    @Required
    private String estado_actual;
    @Required
    private String estado_anterior;
    @Required
    private String estado_actual_color;
    @Required
    private String estado_anterior_color;
    @Required
    private String fecha;
    @Required
    private Boolean leido;

    public CambioEstadosObject() {
    }

    public CambioEstadosObject(String id_usuario, String id_msg, String nombre, String obj_id, String estado_actual, String estado_anterior, String estado_actual_color, String estado_anterior_color, String fecha, Boolean leido) {
        this.id = MyApplication.cambioEstadosId.incrementAndGet();
        this.id_msg = id_msg;
        this.id_usuario = id_usuario;
        this.nombre = nombre;
        this.obj_id = obj_id;
        this.estado_actual = estado_actual;
        this.estado_anterior = estado_anterior;
        this.estado_actual_color = estado_actual_color;
        this.estado_anterior_color = estado_anterior_color;
        this.fecha = fecha;
        this.leido = leido;

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEstado_actual() {
        return estado_actual;
    }

    public void setEstado_actual(String estado_actual) {
        this.estado_actual = estado_actual;
    }

    public String getEstado_anterior() {
        return estado_anterior;
    }

    public void setEstado_anterior(String estado_anterior) {
        this.estado_anterior = estado_anterior;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getId_usuario() {
        return id_usuario;
    }

    public void setId_usuario(String id_usuario) {
        this.id_usuario = id_usuario;
    }

    public String getEstado_actual_color() {
        return estado_actual_color;
    }

    public void setEstado_actual_color(String estado_actual_color) {
        this.estado_actual_color = estado_actual_color;
    }

    public String getEstado_anterior_color() {
        return estado_anterior_color;
    }

    public void setEstado_anterior_color(String estado_anterior_color) {
        this.estado_anterior_color = estado_anterior_color;
    }

    public String getObj_id() {
        return obj_id;
    }

    public void setObj_id(String obj_id) {
        this.obj_id = obj_id;
    }

    public String getId_msg() {
        return id_msg;
    }

    public void setId_msg(String id_msg) {
        this.id_msg = id_msg;
    }

    public Boolean getLeido() {
        return leido;
    }

    public void setLeido(Boolean leido) {
        this.leido = leido;
    }

}
