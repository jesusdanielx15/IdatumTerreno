package cl.idatum.idatumterreno.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.List;

import cl.idatum.idatumterreno.R;

/**
 * @author Jesus Cañizales <jcanizales@siigsa.cl>
 * @version 2
 * @package cl.idatum.idatumterreno Proyecto IdatumTerreno
 * @link http://www.siigsa.cl
 * @copyright SIIGSA, Propiedad Intelectual y Derechos Patrimoniales de Software y Base de Datos i-datum. Registro Propiedad Intelectual Nº 211.351 y 211.352 respectivamente, con fecha 22 de noviembre del 2011
 * @since 25/10/2017
 */

public class SpinnerAdapter extends BaseAdapter {

    private List<String> data;
    private Activity activity;
    private LayoutInflater inflater;


    public SpinnerAdapter(List<String> data, Activity activity) {
        this.data = data;
        this.activity = activity;
        this.inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
            view1 = inflater.inflate(R.layout.spinner_item, null);
        }
        Typeface face = Typeface.createFromAsset(activity.getAssets(), "fonts/oxygenregular.ttf");
        TextView textView = (TextView) view1.findViewById(R.id.textViewSpinner);
        textView.setTypeface(face);
        textView.setText(data.get(i));
        return view1;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        View view = super.getDropDownView(position, convertView, parent);

        LinearLayout linearLayout = (LinearLayout) view;
        TextView textView = (TextView) linearLayout.findViewById(R.id.textViewSpinner);
        textView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        textView.setGravity(Gravity.CENTER);
        return view;
    }
}
