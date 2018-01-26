package cl.idatum.idatumterreno.elena;

import android.util.Base64;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * @author Ricardo Ramirez F. <rramirez@siigsa.cl>
 * @version 2.0.0
 * @since 01-09-2016
 */
public class Funciones {


    /**
     * @author Ricardo Ramirez F. <rramirez@siigsa.cl>
     * @version 2.0.0
     * @since 01-09-2016
     */
    private static final long serialVersionUID = 1L;

    public static String NUMEROS = "0123456789";
    public static String MAYUSCULAS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    public static String MINUSCULAS = "abcdefghijklmnopqrstuvwxyz";


    /**
     * @author Ricardo Ramirez F. <rramirez@siigsa.cl>
     * @version 2.0.0
     * @since 21-10-2016
     */


    public Funciones() {

    }


    public static String convertirBuleano(Object val) {

        Object[] verdaderos = {"1", "true", "t", "on", true};
        Object[] falsos = {"0", "false", "f", "off", "", false};

        String Respuesta = null;

        for (Object res : verdaderos) {
            if (res.equals(val)) {
                Respuesta = Constante.GC_BULEANO_V;
            }
        }
        for (Object res : falsos) {
            if (res.equals(val)) {
                Respuesta = Constante.GC_BULEANO_F;
            }
        }

        return Respuesta;

    }

    public static String Base64_decode(String Cad) throws IOException {

        byte[] Cad_byt_bas64_dec = Base64.decode(Cad, 0);

        String Cad_str = new String(Cad_byt_bas64_dec, "UTF-8");

        return Cad_str;

    }

    public static String Base64_encode(String Cad) {
        byte[] data;
        String res_Geo = "";
        try {
            data = Cad.getBytes("UTF-8");
            res_Geo = Base64.encodeToString(data, 0);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return res_Geo;
    }


}
