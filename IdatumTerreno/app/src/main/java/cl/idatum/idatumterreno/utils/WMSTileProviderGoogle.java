package cl.idatum.idatumterreno.utils;

import android.util.Log;

import com.google.android.gms.maps.model.UrlTileProvider;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Locale;

/**
 * Genera la cubierta WMS para ser integrada al visor de google.
 *
 * @author Jesus Cañizales <jcanizales@siigsa.cl>
 * @version 2
 * @package cl.idatum.idatumterreno.utils Proyecto IdatumTerreno
 * @link http://www.siigsa.cl
 * @copyright SIIGSA, Propiedad Intelectual y Derechos Patrimoniales de Software y Base de Datos i-datum. Registro Propiedad Intelectual Nº 211.351 y 211.352 respectivamente, con fecha 22 de noviembre del 2011
 * @since 12/12/2017
 */
public class WMSTileProviderGoogle extends UrlTileProvider {

    private static final double[] TILE_ORIGIN = {-20037508.34789244, 20037508.34789244};
    //array indexes for that data
    private static final int ORIG_X = 0;
    private static final int ORIG_Y = 1; // "

    // Size of square world map in meters, using WebMerc projection.
    private static final double MAP_SIZE = 20037508.34789244 * 2;

    // cql filters
    private String cqlString = "";
    // array indexes for array to hold bounding boxes.
    protected static final int MINX = 0;
    protected static final int MAXX = 1;
    protected static final int MINY = 2;
    protected static final int MAXY = 3;

    private String layers;
    private String id_mapa;
    private String urlSist;
    private String sess = "MB_" + (int) (Math.random() * 10000000) + 1;

    final String WMS_FORMAT_STRING =
            "%s/exp/mapserv.php" +
                    "?service=WMS" +
                    "&oid=%s" + //97535
                    "&odvid=" +
                    "&sess=%s" +
                    "&verify=true" +
                    "&tmp=/tmp/ms_tmp" +
                    "&STYLES=" +
                    "&SRS=EPSG:900913" +
                    "&version=1.1.1" +
                    "&request=GetMap" +
                    "&layers=%s" +   //c7445,c7447,ctmp_0,ctmp_1,ctmp_2,ctmp_buffer,ctmp
                    "&bbox=%f,%f,%f,%f" +
                    "&width=256" +
                    "&height=256" +
                    "&format=image/png" +
                    "&transparent=true";


    public WMSTileProviderGoogle(int x, int y, String id_mapa, String layers, String url) {
        super(x, y);
        this.layers = layers;
        this.id_mapa = id_mapa;
        this.urlSist = url;
    }

    @Override
    public URL getTileUrl(int x, int y, int zoom) {
        double[] bbox = getBoundingBox(x, y, zoom);
        String s = String.format(Locale.US, WMS_FORMAT_STRING, urlSist, id_mapa, sess, layers, bbox[MINX],
                bbox[MINY], bbox[MAXX], bbox[MAXY]);
        Log.d("WMSTileProviderGoogle", s);
        URL url = null;
        try {
            url = new URL(s);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            throw new AssertionError(e);

        }
        return url;
    }

    protected String getCql() {
        return URLEncoder.encode(cqlString);
    }

    public void setCql(String c) {
        cqlString = c;
    }

    // Return a web Mercator bounding box given tile x/y indexes and a zoom
    // level.
    protected double[] getBoundingBox(int x, int y, int zoom) {
        double tileSize = MAP_SIZE / Math.pow(2, zoom);
        double minx = TILE_ORIGIN[ORIG_X] + x * tileSize;
        double maxx = TILE_ORIGIN[ORIG_X] + (x + 1) * tileSize;
        double miny = TILE_ORIGIN[ORIG_Y] - (y + 1) * tileSize;
        double maxy = TILE_ORIGIN[ORIG_Y] - y * tileSize;

        double[] bbox = new double[4];
        bbox[MINX] = minx;
        bbox[MINY] = miny;
        bbox[MAXX] = maxx;
        bbox[MAXY] = maxy;

        return bbox;
    }


}
