package com.hirelance.controlador;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.hirelance.R;
import com.hirelance.modelo.Proyecto;

import java.util.List;

public class ContratistaProyectoAdapter extends RecyclerView.Adapter<ContratistaProyectoAdapter.ViewHolder> {

    private List<Proyecto> listaProyectos;
    private Context context;

    public ContratistaProyectoAdapter(List<Proyecto> listaProyectos, Context context) {
        this.listaProyectos = listaProyectos;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_proyecto_contratista, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Proyecto proyecto = listaProyectos.get(position);

        // 1. Setear datos
        holder.textTituloProyecto.setText(proyecto.getTitulo());
        holder.textEstadoProyecto.setText(proyecto.getEstado());

        // 2. Lógica de color para el estado (puedes expandir esto)
        int colorResId;
        String estado = proyecto.getEstado();
        if ("publicado".equalsIgnoreCase(estado)) {
            colorResId = R.color.hl_acento_verde; // (Verde)
        } else if ("cancelado".equalsIgnoreCase(estado)) {
            colorResId = R.color.hl_acento_rojo; // (Rojo)
        } else {
            colorResId = R.color.hl_acento_naranja; // (Naranja para 'en_progreso', etc.)
        }
        holder.textEstadoProyecto.setBackgroundColor(ContextCompat.getColor(context, colorResId));
    }

    @Override
    public int getItemCount() {
        return listaProyectos.size();
    }

    // Método para actualizar la lista desde la Activity
    public void setProyectos(List<Proyecto> nuevosProyectos) {
        this.listaProyectos.clear();
        this.listaProyectos.addAll(nuevosProyectos);
        notifyDataSetChanged();
    }

    // ViewHolder
    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView textTituloProyecto, textEstadoProyecto;
        MaterialButton btnVerPostulaciones;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textTituloProyecto = itemView.findViewById(R.id.textTituloProyecto);
            textEstadoProyecto = itemView.findViewById(R.id.textEstadoProyecto);
            btnVerPostulaciones = itemView.findViewById(R.id.btnVerPostulaciones);

            // --- ¡ACTUALIZA ESTE LISTENER! ---
            btnVerPostulaciones.setOnClickListener(v -> {
                // Obtenemos el proyecto
                Proyecto proyectoClicado = listaProyectos.get(getAdapterPosition());

                // Creamos el Intent para la nueva Activity
                Intent intent = new Intent(context, VerPostulacionesActivity.class);

                // Pasamos el ID del proyecto
                intent.putExtra("ID_PROYECTO", proyectoClicado.getIdProyecto());
                context.startActivity(intent);
            });
        }
    }
}