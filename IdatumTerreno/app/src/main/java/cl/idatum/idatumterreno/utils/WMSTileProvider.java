package cl.idatum.idatumterreno.utils;

import android.util.Log;

import org.osmdroid.tileprovider.MapTile;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;

import java.util.Locale;

/**
 * Genera la cubierta WMS para ser integrada al visor de OSMdroid.
 *
 * @author Jesus Cañizales <jcanizales@siigsa.cl>
 * @version 2
 * @package cl.idatum.sgi.idatumapp.utils Proyecto IDatumApp
 * @link http://www.siigsa.cl
 * @copyright SIIGSA, Propiedad Intelectual y Derechos Patrimoniales de Software y Base de Datos i-datum. Registro Propiedad Intelectual Nº 211.351 y 211.352 respectivamente, con fecha 22 de noviembre del 2011
 * @since 28/11/2017
 */

public class WMSTileProvider extends OnlineTileSourceBase {


    // Web Mercator n/w corner of the map.
    private static final double[] TILE_ORIGIN = {-20037508.34789244, 20037508.34789244};
    //array indexes for that data
    private static final int ORIG_X = 0;
    private static final int ORIG_Y = 1; // "

    // Size of square world map in meters, using WebMerc projection.
    private static final double MAP_SIZE = 20037508.34789244 * 2;

    // array indexes for array to hold bounding boxes.
    protected static final int MINX = 0;
    protected static final int MAXX = 1;
    protected static final int MINY = 2;
    protected static final int MAXY = 3;
    private String layer = "c7445,c7447,ctmp_0,ctmp_1,ctmp_2,ctmp_buffer,ctmp";
    //private String layer="c7711,ctmp_0,ctmp_1,ctmp_2,ctmp_buffer,ctmp";

    /**
     * Constructor
     *
     * @param aName    a human-friendly name for this tile source
     * @param aBaseUrl the base url(s) of the tile server used when constructing the url to download the tiles http://sedac.ciesin.columbia.edu/geoserver/wms
     */
    public WMSTileProvider(String aName, String[] aBaseUrl, String layername) {
        super(aName, 0, 22, 256, "png", aBaseUrl);
    }

    //&sess=IV_2075253"
    final String WMS_FORMAT_STRING =
            "http://%s" +
                    "?service=WMS" +
                    "&oid=97535" +
                    "&odvid=" +
                    "&sess=IV_2128365" +
                    "&tmp=/tmp/ms_tmp" +
                    "&STYLES=" +
                    "&SRS=EPSG:900913" +
                    "&version=1.1.1" +
                    "&request=GetMap" +
                    "&layers=%s" +
                    "&bbox=%f,%f,%f,%f" +
                    "&width=256" +
                    "&height=256" +
                    "&format=image/png" +
                    "&transparent=true";


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

    @Override
    public String getTileURLString(MapTile aTile) {
        Log.d("WMSDEMO", "entro");
        double[] bbox = getBoundingBox(aTile.getX(), aTile.getY(), aTile.getZoomLevel());
        String s = String.format(Locale.US, WMS_FORMAT_STRING, getBaseUrl(), layer, bbox[MINX],
                bbox[MINY], bbox[MAXX], bbox[MAXY]);
        Log.d("WMSDEMO", s);
        return s;
    }
}