package cl.idatum.idatumterreno.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import cl.idatum.idatumterreno.R;
import cl.idatum.idatumterreno.activities.CambioEstados;
import cl.idatum.idatumterreno.models.CambioEstadosObject;
import cl.idatum.idatumterreno.models.Contenidos;
import cl.idatum.idatumterreno.utils.Config;
import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmBasedRecyclerViewAdapter;
import io.realm.RealmResults;
import io.realm.RealmViewHolder;

/**
 * Esta clase es el adaptador para los items que se encuentran en la base de datos Realm.
 *
 * @author Jesus Cañizales <jcanizales@siigsa.cl>
 * @version 2
 * @package cl.idatum.idatumterreno Proyecto IdatumTerreno
 * @link http://www.siigsa.cl
 * @copyright SIIGSA, Propiedad Intelectual y Derechos Patrimoniales de Software y Base de Datos i-datum. Registro Propiedad Intelectual Nº 211.351 y 211.352 respectivamente, con fecha 22 de noviembre del 2011
 * @since 25/10/2017
 */
public class RealmAdapterCambioEstado extends RealmBasedRecyclerViewAdapter<CambioEstadosObject, RealmAdapterCambioEstado.ViewHolder> {

    OnRealmRecyclerViewItemClickListener listener;
    Realm realm;
    Typeface face;

    /**
     * Contructor de la clase de adaptador de recyclerView.
     *
     * @param context         Contexto de la actividad "CambioEstados".
     * @param realm           Instancia de la base de datos.
     * @param realmResults    Es la lista con los resultados de la base de datos, una lista de objetos.
     * @param automaticUpdate Si esta en verdadero, la lista se actualizara automaticamente al realizarce un cambio
     * @param animateResults  Animacion que se genera al borrarse un item.
     */
    public RealmAdapterCambioEstado(Context context,
                                    Realm realm,
                                    RealmResults<CambioEstadosObject> realmResults,
                                    boolean automaticUpdate,
                                    boolean animateResults) {

        super(context, realmResults, automaticUpdate, animateResults);
        this.realm = realm;
        face = Typeface.createFromAsset(context.getAssets(), "fonts/oxygenregular.ttf");
    }

    /**
     * Clase que sostiene cada una de las vistas a usarse por el adaptador
     */
    public class ViewHolder extends RealmViewHolder {
        public TextView textView;
        public CardView cardView;

        /**
         * Constructor de la clase que sostiene las vistas
         *
         * @param itemView Es la vista que se usara pa encontrar los items.
         */
        public ViewHolder(View itemView) {
            super(itemView);
            this.textView = itemView.findViewById(R.id.textView);
            this.cardView = itemView.findViewById(R.id.cardView);
        }
    }

    /**
     * Interfaz que se encargara de generar un clickListener.
     */
    public interface OnRealmRecyclerViewItemClickListener {

        public void onRealmRecyclerViewItemClicked(int position,
                                                   String id_msg,
                                                   String nombre,
                                                   String obj_id,
                                                   String estado_actual,
                                                   String estado_anterior,
                                                   String estado_actual_color,
                                                   String estado_anterior_color,
                                                   String fecha,
                                                   View view);
    }

    /**
     * Metodo que configura un listener para generar un clic.
     *
     * @param listener
     */
    public void setOnItemClickListener(OnRealmRecyclerViewItemClickListener listener) {
        this.listener = listener;
    }

    /**
     * Es llamado al crearse el adaptador.
     *
     * @param viewGroup
     * @param i
     * @return
     */
    @Override
    public RealmAdapterCambioEstado.ViewHolder onCreateRealmViewHolder(ViewGroup viewGroup, int i) {
        View view = inflater.inflate(R.layout.cambio_estado_item_realm, viewGroup, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    /**
     * Se encarga de hacer el proceso de llenado de datos.
     *
     * @param viewHolder Es el sostenedor de vistas
     * @param i          Indica la posicion en que se encuentra en la lista
     */
    @Override
    public void onBindRealmViewHolder(final RealmAdapterCambioEstado.ViewHolder viewHolder, final int i) {

        Log.d("TAG", "onBindRealmViewHolder: " + i + "-" + viewHolder.getAdapterPosition());

        setTypeFace(viewHolder);

        final CambioEstadosObject cambioEstadosObject = realmResults.get(i);
        if (cambioEstadosObject.getLeido()) {

            Log.e("TAG", "onBindRealmViewHolder: " +  cambioEstadosObject.getId_msg() + "Se marco leido") ;
            viewHolder.textView.setText("Cambio de Etapa - Producto: " + cambioEstadosObject.getNombre());
            viewHolder.textView.setTextColor(Color.GRAY);
            viewHolder.cardView.setBackgroundColor(Color.parseColor("#FFF5F5F5"));
        } else {
            Log.e("TAG", "onBindRealmViewHolder: " +  cambioEstadosObject.getId_msg() + "Se marco nuevo") ;
            viewHolder.textView.setTextColor(Color.BLACK);
            viewHolder.cardView.setBackgroundColor(Color.parseColor("#FFFFFF"));
            viewHolder.textView.setText("Cambio de Etapa - Producto: " + cambioEstadosObject.getNombre());
        }
        viewHolder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onRealmRecyclerViewItemClicked(viewHolder.getAdapterPosition(),
                        cambioEstadosObject.getId_msg(),
                        cambioEstadosObject.getNombre(),
                        cambioEstadosObject.getObj_id(),
                        cambioEstadosObject.getEstado_actual(),
                        cambioEstadosObject.getEstado_anterior(),
                        cambioEstadosObject.getEstado_actual_color(),
                        cambioEstadosObject.getEstado_anterior_color(),
                        cambioEstadosObject.getFecha(),
                        v);
            }
        });
    }



    private void setTypeFace(ViewHolder viewHolder) {
        viewHolder.textView.setTypeface(face);

    }
}
