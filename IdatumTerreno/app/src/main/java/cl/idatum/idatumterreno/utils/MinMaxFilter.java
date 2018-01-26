package cl.idatum.idatumterreno.utils;

import android.text.InputFilter;
import android.text.Spanned;

/**
 * @author Jesus Cañizales <jcanizales@siigsa.cl>
 * @version 2
 * @package cl.idatum.idatumterreno.utils Proyecto IdatumTerreno
 * @link http://www.siigsa.cl
 * @copyright SIIGSA, Propiedad Intelectual y Derechos Patrimoniales de Software y Base de Datos i-datum. Registro Propiedad Intelectual Nº 211.351 y 211.352 respectivamente, con fecha 22 de noviembre del 2011
 * @since 9/11/2017
 */
public class MinMaxFilter implements InputFilter {

    private int mIntMin, mIntMax;

    public MinMaxFilter(int minValue, int maxValue) {
        this.mIntMin = minValue;
        this.mIntMax = maxValue;
    }

    public MinMaxFilter(String minValue, String maxValue) {
        this.mIntMin = Integer.parseInt(minValue);
        this.mIntMax = Integer.parseInt(maxValue);
    }

    /**
     * Se ejecuta automaticamente y se encarga de generar unfiltro para que el valor del editText tenga un valor maximo y un valor minimo.
     *
     * @param source
     * @param start
     * @param end
     * @param dest
     * @param dstart
     * @param dend
     * @return
     */
    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        try {
            int input = Integer.parseInt(dest.toString() + source.toString());
            if (isInRange(mIntMin, mIntMax, input))
                return null;
        } catch (NumberFormatException nfe) {
            nfe.printStackTrace();
        }

        return "";
    }

    /**
     * Metodo para evaluar si el valor insertado en el campo
     * de texto se encuentra en el rango establecido entre a y b.
     *
     * @param a es el valor minimo que debe tener el campo de texto.
     * @param b es el valor maximo que debe tener el campo de texto.
     * @param c es el valor del campo de texto.
     * @return
     */
    private boolean isInRange(int a, int b, int c) {
        return b > a ? c >= a && c <= b : c >= b && c <= a;
    }
}