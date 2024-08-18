package com.example.examenpm1_p3_dg0055;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;

import java.util.List;

public class EntrevistaAdapter extends ArrayAdapter<Entrevista> {
    private Activity context;
    private List<Entrevista> listaEntrevistas;

    public EntrevistaAdapter(Activity context, List<Entrevista> listaEntrevistas) {
        super(context, R.layout.item_entrevista, listaEntrevistas);
        this.context = context;
        this.listaEntrevistas = listaEntrevistas;
    }
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View listViewItem = inflater.inflate(R.layout.item_entrevista, null, true);

        TextView tvDescripcion = listViewItem.findViewById(R.id.tvDescripcion);
        TextView tvFecha = listViewItem.findViewById(R.id.tvFecha);
        TextView tvPeriodista = listViewItem.findViewById(R.id.tvPeriodista);
        ImageView ivEntrevista = listViewItem.findViewById(R.id.ivEntrevista);

        Entrevista entrevista = listaEntrevistas.get(position);
        tvDescripcion.setText(entrevista.getDescripcion());
        tvPeriodista.setText(entrevista.getPeriodista());
        tvFecha.setText(entrevista.getFecha());

        Glide.with(context).load(entrevista.getImagenUrl()).into(ivEntrevista);

        return listViewItem;
    }
}