package com.hirelance.controlador;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat; // Importa ContextCompat
import androidx.recyclerview.widget.RecyclerView;

import com.hirelance.R;
import com.hirelance.modelo.Postulacion;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class PostulacionAdapter extends RecyclerView.Adapter<PostulacionAdapter.ViewHolder> {

    private List<Postulacion> listaPostulaciones;
    private Context context;

    // Constructor
    public PostulacionAdapter(List<Postulacion> listaPostulaciones, Context context) {
        this.listaPostulaciones = listaPostulaciones;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflamos el layout del item
        View view = LayoutInflater.from(context).inflate(R.layout.item_postulacion, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Obtenemos la postulación actual
        Postulacion postulacion = listaPostulaciones.get(position);

        // 1. Vinculamos los datos
        // Asumimos que la API anida el objeto Proyecto dentro de Postulacion
        if (postulacion.getProyecto() != null) {
            holder.textTituloProyecto.setText(postulacion.getProyecto().getTitulo());
        }

        // 2. Formateamos el monto
        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("es", "SV")); // Formato $
        holder.textMontoOfertado.setText(format.format(postulacion.getMontoOfertado()));

        // 3. Vinculamos y coloreamos el estado
        String estado = postulacion.getEstado();
        holder.textEstadoPostulacion.setText(estado);

        int colorResId;
        if ("aceptada".equalsIgnoreCase(estado)) {
            colorResId = R.color.hl_acento_verde; // Necesitarás definir este color
        } else if ("rechazada".equalsIgnoreCase(estado)) {
            colorResId = R.color.hl_acento_rojo; // Necesitarás definir este color
        } else {
            // "pendiente"
            colorResId = R.color.hl_acento_naranja; // Usamos el naranja para pendiente
        }

        // Usamos ContextCompat para obtener el color
        holder.textEstadoPostulacion.setBackgroundColor(ContextCompat.getColor(context, colorResId));
    }

    @Override
    public int getItemCount() {
        return listaPostulaciones.size();
    }

    /**
     * ViewHolder para el item
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView textTituloProyecto, textMontoOfertado, textEstadoPostulacion;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Vinculamos las vistas del item_postulacion.xml
            textTituloProyecto = itemView.findViewById(R.id.textTituloProyecto);
            textMontoOfertado = itemView.findViewById(R.id.textMontoOfertado);
            textEstadoPostulacion = itemView.findViewById(R.id.textEstadoPostulacion);
        }
    }

    // Método para actualizar la lista desde la Activity
    public void setPostulaciones(List<Postulacion> nuevasPostulaciones) {
        this.listaPostulaciones.clear();
        this.listaPostulaciones.addAll(nuevasPostulaciones);
        notifyDataSetChanged();
    }
}