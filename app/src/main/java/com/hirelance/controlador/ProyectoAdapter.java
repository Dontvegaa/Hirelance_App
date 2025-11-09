package com.hirelance.controlador;

import android.content.Context;
import android.content.Intent; // <-- ¡NUEVO IMPORT!
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
// ... (otros imports)
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.hirelance.R;
import com.hirelance.modelo.Proyecto;

import java.util.ArrayList;
import java.util.List;
// ... (otros imports)

public class ProyectoAdapter extends RecyclerView.Adapter<ProyectoAdapter.ProyectoViewHolder> {

    private List<Proyecto> proyectoList;
    private Context context;
    // ... (formatadorMoneda)

    public ProyectoAdapter(Context context) {
        this.context = context;
        this.proyectoList = new ArrayList<>();
    }

    public void setProyectos(List<Proyecto> proyectos) {
        this.proyectoList = proyectos;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProyectoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_proyecto, parent, false);
        // Pasamos la lista al ViewHolder para que pueda obtener el ID al hacer clic
        return new ProyectoViewHolder(view, proyectoList, context); // <-- ¡MODIFICADO!
    }

    @Override
    public void onBindViewHolder(@NonNull ProyectoViewHolder holder, int position) {
        Proyecto proyecto = proyectoList.get(position);
        // ... (lógica de onBindViewHolder existente)
        holder.textTituloProyecto.setText(proyecto.getTitulo());
        String presupuestoFormateado = formatadorMoneda.format(proyecto.getPresupuesto());
        holder.textPresupuesto.setText(presupuestoFormateado);
        if (proyecto.getCategoria() != null) {
            holder.textCategoria.setText(proyecto.getCategoria().getNombre());
            holder.textCategoria.setVisibility(View.VISIBLE);
        } else {
            holder.textCategoria.setVisibility(View.GONE);
        }
        if (proyecto.getContratista() != null) {
            String nombreContratista = proyecto.getContratista().getNombre() + " " + proyecto.getContratista().getApellido();
            holder.textContratista.setText("Publicado por: " + nombreContratista);
            holder.textContratista.setVisibility(View.VISIBLE);
        } else {
            holder.textContratista.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return proyectoList.size();
    }


    /**
     * El ViewHolder: Mantiene las referencias a las vistas de 'item_proyecto.xml'.
     */
    // ¡MODIFICAMOS EL VIEWHOLDER PARA MANEJAR CLICS!
    public static class ProyectoViewHolder extends RecyclerView.ViewHolder {
        TextView textTituloProyecto;
        TextView textPresupuesto;
        TextView textCategoria;
        TextView textContratista;

        public ProyectoViewHolder(@NonNull View itemView, List<Proyecto> lista, Context ctx) { // <-- ¡MODIFICADO!
            super(itemView);
            textTituloProyecto = itemView.findViewById(R.id.textTituloProyecto);
            textPresupuesto = itemView.findViewById(R.id.textPresupuesto);
            textCategoria = itemView.findViewById(R.id.textCategoria);
            textContratista = itemView.findViewById(R.id.textContratista);

            // --- ¡NUEVA LÓGICA DE CLIC! ---
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition(); // Obtenemos la posición del ítem clickeado
                if (position != RecyclerView.NO_POSITION) {
                    // Obtenemos el ID del proyecto de la lista
                    int proyectoId = lista.get(position).getIdProyecto();

                    // Creamos un Intent para abrir la DetalleProyectoActivity
                    Intent intent = new Intent(ctx, DetalleProyectoActivity.class);

                    // Añadimos el ID del proyecto como "extra" para que la nueva
                    // actividad sepa qué proyecto cargar.
                    intent.putExtra(DetalleProyectoActivity.ID_PROYECTO, proyectoId);

                    // Iniciamos la nueva actividad
                    ctx.startActivity(intent);
                }
            });
        }
    }
}