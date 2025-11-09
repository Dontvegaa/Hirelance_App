package com.hirelance.controlador;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hirelance.R;
import com.hirelance.modelo.Proyecto;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/**
 * Adaptador para el RecyclerView que muestra la lista de proyectos.
 * Esta es la versión ACTUALIZADA que recibe la lista en el constructor
 * y maneja los clics para ir al detalle.
 */
public class ProyectoAdapter extends RecyclerView.Adapter<ProyectoAdapter.ProyectoViewHolder> {

    private List<Proyecto> listaProyectos; // <--- Lista de datos
    private Context context;
    private NumberFormat formatadorMoneda;

    // --- ¡ESTE ES EL CONSTRUCTOR CORRECTO! ---
    // Ahora coincide con lo que MainActivity está llamando: new ProyectoAdapter(listaDeProyectos, this)
    public ProyectoAdapter(List<Proyecto> listaProyectos, Context context) {
        this.listaProyectos = listaProyectos;
        this.context = context;
        // Inicializamos el formateador de moneda aquí para eficiencia
        this.formatadorMoneda = NumberFormat.getCurrencyInstance(new Locale("es", "SV"));
    }

    @NonNull
    @Override
    public ProyectoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflamos el layout de la fila (item_proyecto.xml)
        View view = LayoutInflater.from(context).inflate(R.layout.item_proyecto, parent, false);
        return new ProyectoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProyectoViewHolder holder, int position) {
        // Obtenemos el proyecto de la posición actual
        Proyecto proyecto = listaProyectos.get(position);
        // Llamamos al método "bind" para pintar los datos
        holder.bind(proyecto);
    }

    @Override
    public int getItemCount() {
        // El tamaño de la lista determina cuántos items mostrar
        return listaProyectos.size();
    }


    // ======================================================
    // === EL VIEWHOLDER (El cerebro de cada fila) ===
    // ======================================================
    public class ProyectoViewHolder extends RecyclerView.ViewHolder {

        // Vistas de la tarjeta (item_proyecto.xml)
        TextView textTitulo, textPresupuesto, textCategoria, textContratista;

        public ProyectoViewHolder(@NonNull View itemView) {
            super(itemView);
            // Vinculamos las vistas
            textTitulo = itemView.findViewById(R.id.textTituloProyecto);
            textPresupuesto = itemView.findViewById(R.id.textPresupuesto);
            textCategoria = itemView.findViewById(R.id.textCategoria);
            textContratista = itemView.findViewById(R.id.textContratista);

            // --- ¡AQUÍ MANEJAMOS EL CLIC! (Actualización de Parte 7) ---
            itemView.setOnClickListener(v -> {
                int posicion = getAdapterPosition();
                if (posicion != RecyclerView.NO_POSITION) {
                    // Obtener el proyecto al que se le dio clic
                    Proyecto proyectoClicado = listaProyectos.get(posicion);

                    // Crear el Intent para abrir DetalleProyectoActivity
                    Intent intent = new Intent(context, DetalleProyectoActivity.class);

                    // Pasar el ID del proyecto a la nueva actividad
                    intent.putExtra(DetalleProyectoActivity.ID_PROYECTO, proyectoClicado.getIdProyecto());

                    // Iniciar la nueva actividad
                    context.startActivity(intent);
                }
            });
        }

        /**
         * Pinta los datos del objeto Proyecto en las vistas de la fila.
         */
        public void bind(Proyecto proyecto) {
            textTitulo.setText(proyecto.getTitulo());

            // Formatear presupuesto
            textPresupuesto.setText(formatadorMoneda.format(proyecto.getPresupuesto()));

            // Comprobar si la categoría no es nula
            if (proyecto.getCategoria() != null) {
                textCategoria.setText(proyecto.getCategoria().getNombre());
                textCategoria.setVisibility(View.VISIBLE);
            } else {
                textCategoria.setVisibility(View.GONE); // Ocultar si no hay categoría
            }

            // Comprobar si el contratista no es nulo
            if (proyecto.getContratista() != null) {
                String nombreCompleto = proyecto.getContratista().getNombre() + " " + proyecto.getContratista().getApellido();
                textContratista.setText(nombreCompleto);
            } else {
                textContratista.setText("Contratista Anónimo");
            }
        }
    }
}