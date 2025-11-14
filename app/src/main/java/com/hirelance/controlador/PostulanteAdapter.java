package com.hirelance.controlador;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.hirelance.R;
import com.hirelance.modelo.PerfilEstudiante;
import com.hirelance.modelo.Postulacion;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class PostulanteAdapter extends RecyclerView.Adapter<PostulanteAdapter.ViewHolder> {

    private List<Postulacion> listaPostulaciones;
    private Context context;

    public PostulanteAdapter(List<Postulacion> listaPostulaciones, Context context) {
        this.listaPostulaciones = listaPostulaciones;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_postulante, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Postulacion postulacion = listaPostulaciones.get(position);
        PerfilEstudiante estudiante = postulacion.getEstudiante(); // Obtenemos el perfil anidado

        if (estudiante != null) {
            // 1. Pintar datos del estudiante
            if (estudiante.getUsuario() != null) {
                String nombreCompleto = estudiante.getUsuario().getNombre() + " " + estudiante.getUsuario().getApellido();
                holder.textNombreEstudiante.setText(nombreCompleto);
            }
            holder.textCarreraEstudiante.setText(estudiante.getCarrera());

            // 2. Decodificar la foto de perfil (Base64)
            String fotoBase64 = estudiante.getFotoPerfil();
            if (fotoBase64 != null && !fotoBase64.isEmpty()) {
                try {
                    byte[] decodedString = Base64.decode(fotoBase64, Base64.DEFAULT);
                    Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    holder.imgFotoEstudiante.setImageBitmap(decodedByte);
                } catch (Exception e) {
                    holder.imgFotoEstudiante.setImageResource(R.drawable.ic_menu_perfil);
                }
            } else {
                holder.imgFotoEstudiante.setImageResource(R.drawable.ic_menu_perfil);
            }
        }

        // 3. Pintar datos de la postulación
        Locale locale = new Locale("es", "SV");
        NumberFormat format = NumberFormat.getCurrencyInstance(locale);
        holder.textMontoOfertado.setText(format.format(postulacion.getMontoOfertado()));

        // 4. Lógica de color para el Estado
        String estado = postulacion.getEstado();
        holder.textEstadoPostulacion.setText(estado);
        int colorResId;
        if ("aceptada".equalsIgnoreCase(estado)) {
            colorResId = R.color.hl_acento_verde;
        } else if ("rechazada".equalsIgnoreCase(estado)) {
            colorResId = R.color.hl_acento_rojo;
        } else {
            colorResId = R.color.hl_acento_naranja; // Pendiente
        }
        holder.textEstadoPostulacion.setBackgroundColor(ContextCompat.getColor(context, colorResId));
    }

    @Override
    public int getItemCount() {
        return listaPostulaciones.size();
    }

    // Método para actualizar la lista desde la Activity
    public void setPostulaciones(List<Postulacion> nuevasPostulaciones) {
        this.listaPostulaciones.clear();
        this.listaPostulaciones.addAll(nuevasPostulaciones);
        notifyDataSetChanged();
    }

    // ViewHolder
    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgFotoEstudiante;
        TextView textNombreEstudiante, textCarreraEstudiante, textMontoOfertado, textEstadoPostulacion;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgFotoEstudiante = itemView.findViewById(R.id.imgFotoEstudiante);
            textNombreEstudiante = itemView.findViewById(R.id.textNombreEstudiante);
            textCarreraEstudiante = itemView.findViewById(R.id.textCarreraEstudiante);
            textMontoOfertado = itemView.findViewById(R.id.textMontoOfertado);
            textEstadoPostulacion = itemView.findViewById(R.id.textEstadoPostulacion);

            // (Opcional) Listener para ver el perfil completo del estudiante
            itemView.setOnClickListener(v -> {
                // Obtenemos la postulación
                Postulacion postulacionClicada = listaPostulaciones.get(getAdapterPosition());

                // Creamos el Intent para la nueva Activity
                Intent intent = new Intent(context, DetallePostulanteActivity.class);

                // Pasamos el ID de la POSTULACIÓN
                intent.putExtra("ID_POSTULACION", postulacionClicada.getIdPostulacion());
                context.startActivity(intent);
            });
        }
    }
}