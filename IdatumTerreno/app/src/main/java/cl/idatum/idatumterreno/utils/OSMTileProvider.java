package cl.idatum.idatumterreno.utils;

import com.google.android.gms.maps.model.UrlTileProvider;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Genera la Cubierta de OpenStretMap para ser integrada al visor de google.
 *
 * @author Jesus Cañizales <jcanizales@siigsa.cl>
 * @version 2
 * @package cl.idatum.idatumterreno.utils Proyecto IdatumTerreno
 * @link http://www.siigsa.cl
 * @copyright SIIGSA, Propiedad Intelectual y Derechos Patrimoniales de Software y Base de Datos i-datum. Registro Propiedad Intelectual Nº 211.351 y 211.352 respectivamente, con fecha 22 de noviembre del 2011
 * @since 19/12/2017
 */
public class OSMTileProvider extends UrlTileProvider {

    private String baseUrl;

    public OSMTileProvider(int i, int i1, String url) {
        super(i, i1);
        this.baseUrl = url;
    }

    @Override
    public URL getTileUrl(int x, int y, int zoom) {
        try {
            return new URL(baseUrl.replace("{z}", "" + zoom).replace("{x}", "" + x).replace("{y}", "" + y));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
