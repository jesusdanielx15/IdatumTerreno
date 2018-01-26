package cl.idatum.idatumterreno.elena;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
/**
 *
 * @package cl.idatum.clases
 * Proyect: Plataforma de Software Web i-datum
 * Archivo: ElenaExcepciones.java
 *
 * @link http://www.siigsa.cl
 * @copyright SIIGSA, Propiedad Intelectual y Derechos Patrimoniales de Software y Base de Datos i-datum. Registro Propiedad Intelectual Nº 211.351 y 211.352 respectivamente, con fecha 22 de noviembre del 2011
 * @author Ricardo Ramirez F. <rramirez@siigsa.cl>
 * @since 02-06-2016
 * @version 2.0.0
 * @Ficha 19900
 */
public class ElenaExcepciones {


    private static final long serialVersionUID = 1L;


    private String exe_est_res = Constante.GC_BULEANO_V;
    private List<Object> exe_res = new ArrayList<Object>();
    private String exe_cod = "";
    private String exe_men = "";
    private JSONObject exe_arc = new JSONObject().put("nom", "").put("met", "").put("lin", "");
    private Object exe_obj = null;
    private JSONObject exe_arg = new JSONObject().put("obj_id", "").put("atr_id", "");
    private JSONObject exe_tip_exc = new JSONObject().put("log", 1).put("tec", 2).put("ses", 3);
    private String exe_deb = Constante.GC_BULEANO_F;
    private String exe_reg_sis = Constante.GC_BULEANO_F;
    private Integer exe_can_res = 0;
    private String exe_ele_jav = Constante.GC_BULEANO_V;
    private JSONObject exe_aut_usu = new JSONObject().put("est_aut", Constante.GC_BULEANO_F).put("usu_id", "");
    private String exe_pag_est = Constante.GC_BULEANO_F;
    private Integer exe_pag_act = 1;
    private Integer exe_pag_can_res = 0;

    public ElenaExcepciones() throws JSONException {

    }

    public ElenaExcepciones(final ElenaExcepciones eleExe) throws JSONException {
        if (eleExe != null) {

            this.exe_est_res = eleExe.getExe_est_res();
            this.exe_res = eleExe.getExe_res();
            this.exe_men = eleExe.getExe_men();
            this.exe_obj = eleExe.getExe_obj();
            this.exe_arg = eleExe.getExe_arg();
            this.exe_arc = eleExe.getExe_arc();
            this.exe_aut_usu = eleExe.getExe_aut_usu();
            this.exe_deb = eleExe.getExe_deb();

            if (!eleExe.getExe_reg_sis().equals(null)) {
                this.exe_reg_sis = eleExe.getExe_reg_sis();
            }

            if (!eleExe.getExe_cod().equals(null)) {
                this.exe_cod = eleExe.getExe_tip_exc().getString("log").toString();
            } else {
                this.exe_cod = eleExe.getExe_cod();
            }

            if (this.exe_res.size() > 0) {
                this.exe_can_res = eleExe.getExe_res().size();
            } else {
                this.exe_can_res = eleExe.getExe_can_res();
            }

        }

    }

    /**
     * @param EE
     * @param base64
     * @return
     * @author Ricardo Ramirez F. <rramirez@siigsa.cl>
     * @version 2.0.0
     * @since 03-08-2016
     */
    @SuppressWarnings("static-access")
    public Object jsonEncode(ElenaExcepciones EE, String base64) {
        Funciones Fun = new Funciones();
        /* Estado de la respuesta */

		/* Mensaje */
        if (Fun.convertirBuleano(base64) == Constante.GC_BULEANO_V) {
            if (EE.getExe_men() != null && EE.getExe_men() != "") {
                EE.setExe_men(Fun.Base64_encode(EE.getExe_men().toString()));
            }
        }

		/* Objeto heredado */
        EE.setExe_obj(null);

        if (Fun.convertirBuleano(base64) == Constante.GC_BULEANO_V) {
            String json = new Gson().toJson((Object) EE);
            //JSONObject jsonObject = new JSONObject((Object)EE);

            //String json = jsonObject.toString();
            return Fun.Base64_encode(json);
        } else {
            return new Gson().toJson(EE);
        }

    }

    /**
     * @param //String str
     * @param //String base64
     * @return
     * @throws Exception
     * @author Ricardo Ramirez F. <rramirez@siigsa.cl>
     * @version 2.0.0
     * @since 02-08-2016
     */
    @SuppressWarnings("static-access")
    public ElenaExcepciones jsonDecode(String str, String base64) throws Exception {
        //Funciones Fun = new Funciones(alfa);

        if (Funciones.convertirBuleano(base64) == Constante.GC_BULEANO_V) {
            str = Funciones.Base64_decode(str);
        }

        JSONObject obj = new JSONObject(str);

        ElenaExcepciones EE = new ElenaExcepciones();

        EE.setExe_est_res(Funciones.convertirBuleano(obj.get("exe_est_res")));
        EE.setExe_res(Arrays.asList((Object) obj.get("exe_res")));
        if (!obj.get("exe_cod").equals(null)) {
            EE.setExe_cod(obj.get("exe_cod").toString());
        }
        EE.setExe_men(obj.get("exe_men").toString());
        EE.setExe_arc(obj.getJSONObject("exe_arc"));
        EE.setExe_obj(obj.get("exe_obj"));
        EE.setExe_arg(obj.getJSONObject("exe_arg"));
        EE.setExe_deb(obj.get("exe_deb").toString());
        EE.setExe_aut_usu(obj.getJSONObject("exe_aut_usu"));
        EE.setExe_pag_est(obj.get("exe_pag_est").toString());
        EE.setExe_pag_act((Integer) obj.get("exe_pag_act"));
        EE.setExe_pag_can_res((Integer) obj.get("exe_pag_can_res"));

        return EE;
    }

    /**
     * @param fila
     * @param columna
     * @return Object
     * @author Ricardo Ramirez F. <rramirez@siigsa.cl>
     * @version 3.0.0
     * <p>
     * Obtiene el valor de una columna de una consulta sql nativa
     * @since 02-05-2017
     */
    @SuppressWarnings("rawtypes")
    public Object getValorColumna(Object fila, String columna) {
        Object dato = "";
        try {
            dato = ((HashMap) fila).get(columna);
            if (dato == null) {
                dato = "";
            }
        } catch (Exception ex) {
            //
        }

        return dato;
    }

    public String getExe_est_res() {
        return exe_est_res;
    }

    public void setExe_est_res(String exe_est_res) {
        this.exe_est_res = exe_est_res;
    }

    public List<Object> getExe_res() {
        return exe_res;
    }

    public void setExe_res(List<Object> exe_res) {
        this.exe_res = exe_res;

        this.exe_can_res = exe_res.size();
    }

    public String getExe_cod() {
        return exe_cod;
    }

    public void setExe_cod(String exe_cod) {
        this.exe_cod = exe_cod;

        if (this.getExe_est_res() == Constante.GC_BULEANO_F) {
            //this.ingresarRegistroSistema();
        }
    }

    public String getExe_men() {
        return exe_men;
    }

    public void setExe_men(String exe_men) {
        this.exe_men = exe_men;
    }

    public JSONObject getExe_arc() {
        return exe_arc;
    }

    public void setExe_arc(JSONObject exe_arc) {
        this.exe_arc = exe_arc;
    }

    public Object getExe_obj() {
        return exe_obj;
    }

    public void setExe_obj(Object exe_obj) {
        this.exe_obj = exe_obj;
    }

    public JSONObject getExe_arg() {
        return exe_arg;
    }

    public void setExe_arg(JSONObject exe_arg) {
        this.exe_arg = exe_arg;
    }

    public JSONObject getExe_tip_exc() {
        return exe_tip_exc;
    }

    public void setExe_tip_exc(JSONObject exe_tip_exc) {
        this.exe_tip_exc = exe_tip_exc;
    }

    public String getExe_deb() {
        return exe_deb;
    }

    public void setExe_deb(String exe_deb) {
        this.exe_deb = exe_deb;
    }

    public String getExe_reg_sis() {
        return exe_reg_sis;
    }

    public void setExe_reg_sis(String exe_reg_sis) {
        this.exe_reg_sis = exe_reg_sis;
    }

    public Integer getExe_can_res() {
        return exe_can_res;
    }

    public void setExe_can_res(Integer exe_can_res) {
        this.exe_can_res = exe_can_res;
    }

    public String getExe_ele_jav() {
        return exe_ele_jav;
    }

    public void setExe_ele_jav(String exe_ele_jav) {
        this.exe_ele_jav = exe_ele_jav;
    }

    public JSONObject getExe_aut_usu() {
        return exe_aut_usu;
    }

    public void setExe_aut_usu(JSONObject exe_aut_usu) {
        this.exe_aut_usu = exe_aut_usu;
    }

    public String getExe_pag_est() {
        return exe_pag_est;
    }

    public void setExe_pag_est(String exe_pag_est) {
        this.exe_pag_est = exe_pag_est;
    }

    public Integer getExe_pag_act() {
        return exe_pag_act;
    }

    public void setExe_pag_act(Integer exe_pag_act) {
        this.exe_pag_act = exe_pag_act;
    }

    public Integer getExe_pag_can_res() {
        return exe_pag_can_res;
    }

    public void setExe_pag_can_res(Integer exe_pag_can_res) {
        this.exe_pag_can_res = exe_pag_can_res;
    }


}
