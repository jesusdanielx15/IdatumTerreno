package cl.idatum.idatumterreno.utils;

import com.google.maps.android.data.kml.KmlLayer;

/**
 * @author Jesus Cañizales <jcanizales@siigsa.cl>
 * @version 2
 * @package cl.idatum.idatumterreno.utils Proyecto IdatumTerreno
 * @link http://www.siigsa.cl
 * @copyright SIIGSA, Propiedad Intelectual y Derechos Patrimoniales de Software y Base de Datos i-datum. Registro Propiedad Intelectual Nº 211.351 y 211.352 respectivamente, con fecha 22 de noviembre del 2011
 * @since 26/12/2017
 */
public class CubiertaObject {

    private String nombre;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    private String id;
    private Boolean estado = false;
    private KmlLayer kmlLayer;


    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Boolean getEstado() {
        return estado;
    }

    public void setEstado(Boolean estado) {
        this.estado = estado;
    }

    public KmlLayer getKmlLayer() {
        return kmlLayer;
    }

    public void setKmlLayer(KmlLayer kmlLayer) {
        this.kmlLayer = kmlLayer;
    }
}
