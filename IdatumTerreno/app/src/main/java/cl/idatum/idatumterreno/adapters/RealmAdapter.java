package cl.idatum.idatumterreno.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import cl.idatum.idatumterreno.R;
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
public class RealmAdapter extends RealmBasedRecyclerViewAdapter<Contenidos, RealmAdapter.ViewHolder> implements Filterable {

    OnRealmRecyclerViewItemClickListener listener;
    Realm realm;
    Typeface face;
    SharedPreferences prefs = getContext().getSharedPreferences(Config.NOMBRE_SHARED, Context.MODE_PRIVATE);

    /**
     * Contructor de la clase de adaptador de recyclerView.
     *
     * @param context         Contexto de la actividad "contenidosCuardados".
     * @param realm           Instancia de la base de datos.
     * @param realmResults    Es la lista con los resultados de la base de datos, una lista de objetos.
     * @param automaticUpdate Si esta en verdadero, la lista se actualizara automaticamente al realizarce un cambio
     * @param animateResults  Animacion que se genera al borrarse un item.
     */
    public RealmAdapter(Context context,
                        Realm realm,
                        RealmResults<Contenidos> realmResults,
                        boolean automaticUpdate,
                        boolean animateResults
    ) {
        super(context, realmResults, automaticUpdate, animateResults);
        this.realm = realm;
        face = Typeface.createFromAsset(context.getAssets(), "fonts/oxygenregular.ttf");
    }

    /**
     * Clase que sostiene cada una de las vistas a usarse por el adaptador
     */
    public class ViewHolder extends RealmViewHolder {
        public TextView id_contenidos;
        public TextView coordenadas;
        public TextView id_relacion;
        public TextView fecha_creacion;
        public TextView nombre_contenido;
        public TextView id_tipo_cont;
        public CardView cardView;

        /**
         * Constructor de la clase que sostiene las vistas
         *
         * @param itemView Es la vista que se usara pa encontrar los items.
         */
        public ViewHolder(View itemView) {
            super(itemView);
            this.id_contenidos = itemView.findViewById(R.id.id_contenido);
            this.coordenadas = itemView.findViewById(R.id.coordenadas);

            this.id_relacion = itemView.findViewById(R.id.id_relacion);
            this.fecha_creacion = itemView.findViewById(R.id.fecha_creacion);
            this.nombre_contenido = itemView.findViewById(R.id.nombre_contenido);
            this.cardView = itemView.findViewById(R.id.cardView);
            this.id_tipo_cont = itemView.findViewById(R.id.id_tipo_cont);


        }
    }

    /**
     * Interfaz que se encargara de generar un clickListener.
     */
    public interface OnRealmRecyclerViewItemClickListener {

        public void onRealmRecyclerViewItemClicked(int position, String id_contenido, String id_usuario, String nombre_contenido, String tipo_id_contenido, String latitud, String longitud, String id_relacion, View view);
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
    public RealmAdapter.ViewHolder onCreateRealmViewHolder(ViewGroup viewGroup, int i) {
        View view = inflater.inflate(R.layout.contenido_item_realm, viewGroup, false);
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
    public void onBindRealmViewHolder(final RealmAdapter.ViewHolder viewHolder, final int i) {

        setTypeFace(viewHolder);

        final Contenidos contenidos = realmResults.get(i);
        viewHolder.id_contenidos.setText(contenidos.getId_contenido());
        viewHolder.nombre_contenido.setText(contenidos.getNombre_contenido());
        if (contenidos.getLatitud() != null && contenidos.getLongitud() != null) {
            String lat = contenidos.getLatitud();
            if (lat.length() > 7) {
                lat = lat.substring(0, 7);
            }
            String lon = contenidos.getLongitud();
            if (lon.length() > 7) {
                lon = lon.substring(0, 7);
            }
            viewHolder.coordenadas.setText(lat + ", " + lon);
        } else {
            viewHolder.coordenadas.setText("vacio");
        }

        if (!("").equals(contenidos.getId_relacion()) && contenidos.getId_relacion() != null) {
            viewHolder.id_relacion.setText(contenidos.getId_relacion());
        } else {
            viewHolder.id_relacion.setText("vacio");
        }
        if (!("").equals(contenidos.getTipo_id_contenido()) && contenidos.getTipo_id_contenido() != null) {
            viewHolder.id_tipo_cont.setText(contenidos.getTipo_id_contenido());
        } else {
            viewHolder.id_tipo_cont.setText("vacio");
        }

        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        String fecha = dateFormat.format(contenidos.getFecha_creacion());
        viewHolder.fecha_creacion.setText(fecha);

        viewHolder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onRealmRecyclerViewItemClicked(i,
                        contenidos.getId_contenido(),
                        contenidos.getId_usuario(),
                        contenidos.getNombre_contenido(),
                        contenidos.getTipo_id_contenido(),
                        contenidos.getLatitud(),
                        contenidos.getLongitud(),
                        contenidos.getId_relacion(),
                        v);
            }
        });
    }

    private void setTypeFace(ViewHolder viewHolder) {
        viewHolder.id_contenidos.setTypeface(face);
        viewHolder.coordenadas.setTypeface(face);
        viewHolder.id_relacion.setTypeface(face);
        viewHolder.fecha_creacion.setTypeface(face);
        viewHolder.nombre_contenido.setTypeface(face);
        viewHolder.id_tipo_cont.setTypeface(face);
    }

    @Override
    public Filter getFilter() {
        ContenidosFilter filter = new ContenidosFilter(this);
        return filter;
    }

    /**
     * Clase para generar un filtro en la lista y poder encontrar cualquier contenido.
     */
    private class ContenidosFilter extends Filter {
        private final RealmAdapter adapter;

        private ContenidosFilter(RealmAdapter adapter) {
            super();
            this.adapter = adapter;
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            return new FilterResults();
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            adapter.filterResults(constraint.toString());
        }
    }

    /**
     * Se encarga de filtrar los resultados de la base de datos
     *
     * @param text Texto a buscar en la base de datos.
     */
    private void filterResults(String text) {

        text = text == null ? null : text.toLowerCase().trim();
        if (text == null || "".equals(text)) {

            updateRealmResults(realm.where(Contenidos.class).contains("id_usuario", prefs.getString(Config.ID_USUARIO_SHARED, "")).findAll().sort("id"));
        } else {
            updateRealmResults(realm.where(Contenidos.class).contains("id_usuario", prefs.getString(Config.ID_USUARIO_SHARED, "")).contains("nombre_contenido", text, Case.INSENSITIVE).findAll());
        }

    }

}
