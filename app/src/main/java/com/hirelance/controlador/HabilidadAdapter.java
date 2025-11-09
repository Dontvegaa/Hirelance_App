package com.hirelance.controlador;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hirelance.R;
import com.hirelance.modelo.Habilidad;

import java.util.List;

public class HabilidadAdapter extends RecyclerView.Adapter<HabilidadAdapter.HabilidadViewHolder> {

    private List<Habilidad> habilidadList;

    public HabilidadAdapter(List<Habilidad> habilidadList) {
        this.habilidadList = habilidadList;
    }

    @NonNull
    @Override
    public HabilidadViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_habilidad, parent, false);
        return new HabilidadViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HabilidadViewHolder holder, int position) {
        Habilidad habilidad = habilidadList.get(position);
        holder.textHabilidad.setText(habilidad.getTitulo());
    }

    @Override
    public int getItemCount() {
        return habilidadList.size();
    }

    public static class HabilidadViewHolder extends RecyclerView.ViewHolder {
        TextView textHabilidad;

        public HabilidadViewHolder(@NonNull View itemView) {
            super(itemView);
            textHabilidad = itemView.findViewById(R.id.textHabilidad);
        }
    }
}