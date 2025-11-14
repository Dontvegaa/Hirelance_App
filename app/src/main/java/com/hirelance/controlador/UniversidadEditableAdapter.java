package com.hirelance.controlador;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hirelance.R;
import com.hirelance.modelo.Universidad;

import java.util.List;

public class UniversidadEditableAdapter extends RecyclerView.Adapter<UniversidadEditableAdapter.ViewHolder> {

    private List<Universidad> listaUniversidades;
    private Context context;
    private OnUniversidadListener listener;

    /**
     * Interfaz para manejar los clics de eliminación
     */
    public interface OnUniversidadListener {
        void onUniversidadEliminarClick(int position);
    }

    public UniversidadEditableAdapter(List<Universidad> listaUniversidades, Context context, OnUniversidadListener listener) {
        this.listaUniversidades = listaUniversidades;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_universidad_editable, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Universidad universidad = listaUniversidades.get(position);

        // 1. Setear el nombre
        holder.textNombreUniversidad.setText(universidad.getNombre());

        // 2. Decodificar y setear el logo Base64
        // (El POJO 'Universidad' debe tener el campo 'logoBase64')
        String logoBase64 = universidad.getLogoBase64();
        if (logoBase64 != null && !logoBase64.isEmpty()) {
            try {
                byte[] decodedString = Base64.decode(logoBase64, Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                holder.imgLogoUniversidad.setImageBitmap(decodedByte);
            } catch (Exception e) {
                holder.imgLogoUniversidad.setImageResource(R.drawable.ic_menu_perfil);
            }
        } else {
            holder.imgLogoUniversidad.setImageResource(R.drawable.ic_menu_perfil);
        }
    }

    @Override
    public int getItemCount() {
        return listaUniversidades.size();
    }

    // ViewHolder
    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgLogoUniversidad;
        TextView textNombreUniversidad;
        ImageView imgEliminarUniversidad;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgLogoUniversidad = itemView.findViewById(R.id.imgLogoUniversidad);
            textNombreUniversidad = itemView.findViewById(R.id.textNombreUniversidad);
            imgEliminarUniversidad = itemView.findViewById(R.id.imgEliminarUniversidad);

            // Configurar el listener para el botón "X"
            imgEliminarUniversidad.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onUniversidadEliminarClick(position);
                }
            });
        }
    }
}