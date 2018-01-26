package cl.idatum.idatumterreno.adapters;

import android.animation.Animator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import cl.idatum.idatumterreno.R;
import cl.idatum.idatumterreno.fragments.ContenidosFragment;
import cl.idatum.idatumterreno.models.Contenidos;
import cl.idatum.idatumterreno.response.Contenido;
import io.realm.RealmResults;


/**
 * Adaptador para renderizar los distintos contenidos del sistema
 *
 * @author Jesus Cañizales <jcanizales@siigsa.cl>
 * @version 2
 * @package cl.idatum.idatumterreno Proyecto IdatumTerreno
 * @link http://www.siigsa.cl
 * @copyright SIIGSA, Propiedad Intelectual y Derechos Patrimoniales de Software y Base de Datos i-datum. Registro Propiedad Intelectual Nº 211.351 y 211.352 respectivamente, con fecha 22 de noviembre del 2011
 * @since 25/10/2017
 */
public class RestAdapter extends RecyclerView.Adapter<RestAdapter.RestViewHolder> {

    private final ContenidosFragment contenidosFragment;
    ArrayList<Contenido> list;
    Context context;
    RealmResults<Contenidos> realmResults;
    OnRecyclerViewItemClickListener listener;
    Typeface face;

    /**
     * Constructor de la clase.
     *
     * @param list1                  Lista de contenidos encontrados en el sistema.
     * @param context                Contexto del fragmento que realiza la busqueda de contenidos.
     * @param contenidosFragment     Instancia del fragmento de contenidos.
     * @param contenidosRealmResults Lista con los resultados de la base de datos, lista de objetos.
     */
    public RestAdapter(ArrayList<Contenido> list1, Context context, ContenidosFragment contenidosFragment, RealmResults<Contenidos> contenidosRealmResults) {

        this.list = list1;
        this.context = context;
        this.contenidosFragment = contenidosFragment;
        this.realmResults = contenidosRealmResults;
        face = Typeface.createFromAsset(context.getAssets(), "fonts/oxygenregular.ttf");
    }

    /**
     * Interface que se encarga de generar un clic en la lista de contenidos.
     */
    public interface OnRecyclerViewItemClickListener {
        void onRecyclerViewItemClicked(int position, String id, String nombre, View view);
    }

    /**
     * Metodo que genera un listener para hacer clic.
     *
     * @param listener El manejador de clic para este adaptador, es llamado cuando se presiona un item.
     */
    public void setOnItemClickListener(OnRecyclerViewItemClickListener listener) {
        this.listener = listener;
    }

    /**
     * Es llamado al crearse cada viewHolder (manejador de vistas).
     * Suficientes viewholders seran creados para llenar la pantalla de items.
     *
     * @param parent   Es el grupo que contiene a todos los viewHolders adentro.
     * @param viewType Es el tipo de item, en este caso solo hay uno y no se utiliza este parametro.
     *                 Se puede usar para generar un layout diferente para cada item que sea diferente.
     * @return Un nuevo adaptador que sostiene la vista de cada item.
     */
    @Override
    public RestViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.contenido_lista, parent, false);
        return new RestViewHolder(v);
    }

    /**
     * Es llamado por el RecyclerView para mostrar los datos en una posicion especifica.
     * En este metodo se actualizan los contenidos del sujetador para mostrar las caracteristicas
     * de cada ficha de contenido del sistema para cada posicion en especifico.
     *
     * @param holder   El sostenedor de cada vista.
     * @param position Posicion del item que se esta llenando con la informacion.
     */
    @Override
    public void onBindViewHolder(final RestViewHolder holder, final int position) {
        holder.textView.setTypeface(face);
        Contenido contenido = list.get(position);
        String detail = "";

        detail += contenido.getId() + " - ";
        detail += contenido.getNombre();

        holder.textView.setText(detail);

        marcarGeorefenciados(holder, contenido);
        marcarGuardados(holder, contenido);

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onRecyclerViewItemClicked(position, list.get(position).getId(), list.get(position).getNombre(), view);
            }
        });
    }

    /**
     * Se encarga de marcar todos los items que ya han sido georeferenciados anteriormente.
     *
     * @param holder    El sostenedor de la vista.
     * @param contenido El objeto actual con que se esta trabajando.
     */
    private void marcarGeorefenciados(RestViewHolder holder, Contenido contenido) {
        if (contenido.getRelaciones().getRel().getRegistros() != null) {
            //holder.cardView.setBackgroundColor(Color.parseColor("#e2e2e2"));
            holder.imageView_geo.setImageResource(R.mipmap.ic_geo_full);
        } else {
            holder.imageView_geo.setImageResource(R.mipmap.ic_geo_empty);
            //holder.cardView.setBackgroundColor(Color.parseColor("#FFFFFF"));
            //holder.textView.setTypeface(null, Typeface.BOLD);
        }
    }

    /**
     * Se encarga de marcar todos los items que ya han sido guardados anteriormente.
     *
     * @param holder    El sostenedor de la vista.
     * @param contenido El objeto actual con que se esta trabajando.
     */
    private void marcarGuardados(RestViewHolder holder, Contenido contenido) {
        for (int i = 0; i < realmResults.size(); i++) {
            Contenidos contenidos = realmResults.get(i);
            // Log.e("RestAdapter", list.get(position).getId() + "=" + (contenidos.getId_contenido()));
            if (contenido.getId().equals(contenidos.getId_contenido())) {
                holder.imageView_save.setImageResource(R.mipmap.ic_launcher_archive_full);
                //holder.textView.setTextColor(ContextCompat.getColor(context, R.color.coloridatum));
                break;
                //Log.e("RestAdapter", "posicion : "+ position +" " + list.get(position).getId() + "=" + (contenidos.getId_contenido()));
            } else {
                holder.imageView_save.setImageResource(R.mipmap.ic_launcher_archive_empty);
                //holder.textView.setTextColor(ContextCompat.getColor(context, R.color.negro));

            }
        }
    }

    @Override
    public void onViewAttachedToWindow(RestViewHolder holder) {
        animateCircularReveal(holder.itemView);
        super.onViewAttachedToWindow(holder);
    }

    private void animateCircularReveal(View itemView) {
        int centerX = 0;
        int centerY = 0;
        int startRadious = 0;
        int endRadious = Math.max(itemView.getWidth(), itemView.getHeight());
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            Animator animator = ViewAnimationUtils.createCircularReveal(itemView, centerX, centerY, startRadious, endRadious);
            animator.start();
        }
    }

    @Override
    public void onViewDetachedFromWindow(RestViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        holder.itemView.clearAnimation();
    }

    /**
     * Obtiene la longitud de la lista a mostrar.
     *
     * @return Retorna el numero de items o vistas disponibles.
     */
    @Override
    public int getItemCount() {
        return list.size();
    }

    /**
     * Clase que se encarga de encontrar las vistas de los items.
     */
    public class RestViewHolder extends RecyclerView.ViewHolder {
        public TextView textView;
        public CardView cardView;
        public ImageView imageView_save;
        public ImageView imageView_geo;

        /**
         * Constructor de la clase que encuentra las vistas de la clase.
         *
         * @param itemView La vista que servira para encontrar el item.
         */
        public RestViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.text_id);
            cardView = itemView.findViewById(R.id.cardView);
            imageView_save = itemView.findViewById(R.id.imageView_save);
            imageView_geo = itemView.findViewById(R.id.imageView_geo);
        }
    }
}
