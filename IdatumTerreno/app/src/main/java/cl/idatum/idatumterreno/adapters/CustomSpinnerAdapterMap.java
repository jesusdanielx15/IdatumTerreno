package cl.idatum.idatumterreno.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.TileOverlayOptions;

import java.util.List;

import cl.idatum.idatumterreno.R;
import cl.idatum.idatumterreno.utils.OSMTileProvider;

/**
 * @author Jesus Cañizales <jcanizales@siigsa.cl>
 * @version 2
 * @package cl.idatum.idatumterreno.adapters Proyecto IdatumTerreno
 * @link http://www.siigsa.cl
 * @copyright SIIGSA, Propiedad Intelectual y Derechos Patrimoniales de Software y Base de Datos i-datum. Registro Propiedad Intelectual Nº 211.351 y 211.352 respectivamente, con fecha 22 de noviembre del 2011
 * @since 26/12/2017
 */
public class CustomSpinnerAdapterMap extends BaseAdapter {

    private List<String> data;
    private Activity activity;
    private LayoutInflater inflater;


    public CustomSpinnerAdapterMap(List<String> data, Activity activity) {
        this.data = data;
        this.activity = activity;
        this.inflater = (LayoutInflater) this.activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int i) {
        return i;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View view1 = view;
        if (view == null) {
            view1 = inflater.inflate(R.layout.custom_spinner_item, null);
        }
        //Typeface face = Typeface.createFromAsset(activity.getAssets(), "fonts/oxygenregular.ttf");
        TextView textView = view1.findViewById(R.id.textView);
        ImageView imageView = view1.findViewById(R.id.imageView);
        // textView.setTypeface(face);
        textView.setText(data.get(i));

        switch (data.get(i)) {
            case "Google Calles":
                imageView.setImageResource(R.drawable.predeterminado1opt);

                break;
            case "Google Hibrido":
                imageView.setImageResource(R.drawable.hibrido1opt);

                break;
            case "Google Satelite":
                imageView.setImageResource(R.drawable.satelite1opt);

                break;
            case "Google Terreno":
                imageView.setImageResource(R.drawable.relieve1opt);

                break;
            case "OSM Mapnik MERCATOR":
                imageView.setImageResource(R.drawable.osm_ic_ic_map_ortho);
                break;

        }


        return view1;
    }
}
