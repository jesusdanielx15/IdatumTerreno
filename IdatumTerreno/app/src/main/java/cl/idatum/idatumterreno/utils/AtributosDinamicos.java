package cl.idatum.idatumterreno.utils;

import java.util.List;

/**
 * Clase para configurar un objeto para representar los Atributos Dinamicos de la estructura del PVC.
 *
 * @author Jesus Cañizales <jcanizales@siigsa.cl>
 * @version 2
 * @package cl.idatum.idatumterreno.utils Proyecto IdatumTerreno
 * @link http://www.siigsa.cl
 * @copyright SIIGSA, Propiedad Intelectual y Derechos Patrimoniales de Software y Base de Datos i-datum. Registro Propiedad Intelectual Nº 211.351 y 211.352 respectivamente, con fecha 22 de noviembre del 2011
 * @since 10/11/2017
 */
public class AtributosDinamicos {

    private int id;
    private int tipo;
    private String valor;
    private String fecha;
    private Boolean multiple = false;
    private List<AtributosConceptualMultiple> list;




    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTipo() {
        return tipo;
    }

    public void setTipo(int tipo) {
        this.tipo = tipo;
    }

    public String getValor() {
        return valor;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public Boolean getMultiple() {
        return multiple;
    }

    public void setMultiple(Boolean multiple) {
        this.multiple = multiple;
    }

    public List<AtributosConceptualMultiple> getList() {
        return list;
    }

    public void setList(List<AtributosConceptualMultiple> list) {
        this.list = list;
    }

    public static class AtributosConceptualMultiple {
        private String atributoValor;

        public String getAtributoValor() {
            return atributoValor;
        }

        public void setAtributoValor(String atributoValor) {
            this.atributoValor = atributoValor;
        }
    }
}
