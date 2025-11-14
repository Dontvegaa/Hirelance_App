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
import android.widget.Toast;
import android.content.Intent; // ¡NUEVO IMPORT!

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hirelance.R;
import com.hirelance.modelo.PerfilEstudiante;
import com.hirelance.controlador.VerTalentoActivity;

import java.util.List;


public class TalentoAdapter extends RecyclerView.Adapter<TalentoAdapter.ViewHolder> {
    private List<PerfilEstudiante> listaEstudiantes;
    private Context context;
    private String tokenHeader; // ¡NUEVO!

    // ¡CAMBIO! Constructor actualizado
    public TalentoAdapter(List<PerfilEstudiante> listaEstudiantes, Context context, String tokenHeader) {
        this.listaEstudiantes = listaEstudiantes;
        this.context = context;
        this.tokenHeader = tokenHeader; // Guardar el token
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_estudiante_talento, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PerfilEstudiante perfil = listaEstudiantes.get(position);

        // 1. Pintar datos del Perfil y Usuario
        if (perfil.getUsuario() != null) {
            holder.textNombreEstudiante.setText(perfil.getUsuario().getNombre() + " " + perfil.getUsuario().getApellido());
        }
        holder.textCarreraEstudiante.setText(perfil.getCarrera());

        // 2. Decodificar la foto de perfil (Base64)
        String fotoBase64 = perfil.getFotoPerfil();
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

        // 3. Pintar la Habilidad Principal
        if (perfil.getHabilidades() != null && !perfil.getHabilidades().isEmpty()) {
            holder.textHabilidadPrincipal.setText(perfil.getHabilidades().get(0).getTitulo());
            holder.textHabilidadPrincipal.setVisibility(View.VISIBLE);
        } else {
            holder.textHabilidadPrincipal.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return listaEstudiantes.size();
    }

    // Metodo para actualizar la lista
    public void setEstudiantes(List<PerfilEstudiante> nuevosEstudiantes) {
        this.listaEstudiantes.clear();
        this.listaEstudiantes.addAll(nuevosEstudiantes);
        notifyDataSetChanged();
    }

    // ViewHolder
    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgFotoEstudiante;
        TextView textNombreEstudiante, textCarreraEstudiante, textHabilidadPrincipal;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgFotoEstudiante = itemView.findViewById(R.id.imgFotoEstudiante);
            textNombreEstudiante = itemView.findViewById(R.id.textNombreEstudiante);
            textCarreraEstudiante = itemView.findViewById(R.id.textCarreraEstudiante);
            textHabilidadPrincipal = itemView.findViewById(R.id.textHabilidadPrincipal);

            // Listener para ver el perfil completo del estudiante
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    PerfilEstudiante perfilSeleccionado = listaEstudiantes.get(position);

                    int idUsuario = -1;

                    // 1. Intentar obtener el ID directamente del objeto PerfilEstudiante (más seguro)
                    if (perfilSeleccionado.getIdUsuario() != 0) {
                        idUsuario = perfilSeleccionado.getIdUsuario();
                    }

                    // 2. Si no funciona, intentar del objeto Usuario anidado (lo que intentamos antes)
                    else if (perfilSeleccionado.getUsuario() != null) {
                        idUsuario = perfilSeleccionado.getUsuario().getIdUsuario();
                    }

                    if (idUsuario > 0) {
                        Intent intent = new Intent(context, VerTalentoActivity.class);

                        // ¡CAMBIO! Añadir ID (como String) y Token al Intent
                        intent.putExtra("ID_USUARIO_PERFIL", String.valueOf(idUsuario));
                        intent.putExtra("AUTH_TOKEN", tokenHeader); // CORRECTO

                        context.startActivity(intent);
                    } else {
                        Toast.makeText(context, "Error: ID de usuario no disponible.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
}