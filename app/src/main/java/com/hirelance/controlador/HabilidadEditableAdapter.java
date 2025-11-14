package com.hirelance.controlador;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hirelance.R;
import com.hirelance.modelo.Habilidad;

import java.util.List;

public class HabilidadEditableAdapter extends RecyclerView.Adapter<HabilidadEditableAdapter.ViewHolder> {

    private List<Habilidad> listaHabilidades;
    private Context context;
    private OnHabilidadListener listener;

    /**
     * Interfaz para manejar los clics de eliminación
     */
    public interface OnHabilidadListener {
        void onHabilidadEliminarClick(int position);
    }

    public HabilidadEditableAdapter(List<Habilidad> listaHabilidades, Context context, OnHabilidadListener listener) {
        this.listaHabilidades = listaHabilidades;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_habilidad_editable, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Habilidad habilidad = listaHabilidades.get(position);
        holder.textHabilidadTitulo.setText(habilidad.getTitulo());
    }

    @Override
    public int getItemCount() {
        return listaHabilidades.size();
    }

    // ViewHolder
    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView textHabilidadTitulo;
        ImageView imgEliminarHabilidad;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textHabilidadTitulo = itemView.findViewById(R.id.textHabilidadTitulo);
            imgEliminarHabilidad = itemView.findViewById(R.id.imgEliminarHabilidad);

            // Configurar el listener para el botón "X"
            imgEliminarHabilidad.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onHabilidadEliminarClick(position);
                }
            });
        }
    }
}