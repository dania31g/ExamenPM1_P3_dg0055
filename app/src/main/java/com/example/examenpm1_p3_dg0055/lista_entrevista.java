package com.example.examenpm1_p3_dg0055;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class lista_entrevista extends AppCompatActivity {
    private ListView listViewEntrevistas;
    private List<Entrevista> listaEntrevistas;
    private EntrevistaAdapter entrevistaAdapter;
    private DatabaseReference databaseEntrevistas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lista_entrevistas);

        listViewEntrevistas = findViewById(R.id.listViewEntrevistas);
        listaEntrevistas = new ArrayList<>();
        databaseEntrevistas = FirebaseDatabase.getInstance().getReference("Entrevistas");

        entrevistaAdapter = new EntrevistaAdapter(this, listaEntrevistas);
        listViewEntrevistas.setAdapter(entrevistaAdapter);

        databaseEntrevistas.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                listaEntrevistas.clear();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Entrevista entrevista = postSnapshot.getValue(Entrevista.class);
                    listaEntrevistas.add(entrevista);
                }
                entrevistaAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Manejo de errores
            }
        });

        listViewEntrevistas.setOnItemClickListener((adapterView, view, position, id) -> {
            Entrevista entrevistaSeleccionada = listaEntrevistas.get(position);
            Opciones(entrevistaSeleccionada);
        });
    }

    private void Opciones(Entrevista entrevista) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Opciones para " + entrevista.getDescripcion());

        String[] opciones = {"Editar", "Eliminar", "Escuchar"};
        builder.setItems(opciones, (dialog, which) -> {
            switch (which) {
                case 0:
                    if (entrevista != null) {
                        Intent modificarIntent = new Intent(lista_entrevista.this, edit_entrevista.class);
                        modificarIntent.putExtra("EntrevistaId", entrevista.getIdOrden());
                        startActivity(modificarIntent);
                    } else {
                        Toast.makeText(lista_entrevista.this, "Entrevista no válida", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case 1:
                    eliminarEntrevista(entrevista);
                    break;
                case 2:
                    Intent escucharIntent = new Intent(lista_entrevista.this, escuchar_entrevista.class);
                    escucharIntent.putExtra("Entrevista", entrevista);
                    startActivity(escucharIntent);
                    break;
            }
        });
        builder.show();
    }

    private void eliminarEntrevista(Entrevista entrevista) {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Entrevistas").child(String.valueOf(entrevista.getIdOrden()));
        dbRef.removeValue().addOnSuccessListener(aVoid -> {
            Toast.makeText(lista_entrevista.this, "Entrevista eliminada", Toast.LENGTH_SHORT).show();
            listaEntrevistas.remove(entrevista);
            entrevistaAdapter.notifyDataSetChanged();
        }).addOnFailureListener(e -> {
            Toast.makeText(lista_entrevista.this, "No se pudo eliminar la entrevista", Toast.LENGTH_SHORT).show();
        });

        StorageReference imagenRef = FirebaseStorage.getInstance().getReferenceFromUrl(entrevista.getImagenUrl());
        StorageReference audioRef = FirebaseStorage.getInstance().getReferenceFromUrl(entrevista.getAudioUrl());

        imagenRef.delete().addOnSuccessListener(aVoid -> {
        }).addOnFailureListener(e -> {
            Toast.makeText(lista_entrevista.this, "No se pudo eliminar la imagen", Toast.LENGTH_SHORT).show();
        });

        audioRef.delete().addOnSuccessListener(aVoid -> {
        }).addOnFailureListener(e -> {
            Toast.makeText(lista_entrevista.this, "No se pudo eliminar el audio", Toast.LENGTH_SHORT).show();
        });
    }
}
