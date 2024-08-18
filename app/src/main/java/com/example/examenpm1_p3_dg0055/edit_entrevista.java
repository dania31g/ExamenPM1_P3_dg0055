package com.example.examenpm1_p3_dg0055;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class edit_entrevista extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int PICK_AUDIO_REQUEST = 2;
    private static final int REQUEST_IMAGE_CAPTURE = 3;
    private static final int REQUEST_AUDIO_RECORD = 4;

    private EditText etDescripcion, etPeriodista, etFecha;
    private ImageView ivImagen;
    private TextView tvAudioSeleccionado;
    private Uri imagenUri, audioUri;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;
    private Button btnSeleccionar, btnsubirAudio, btngrabarAudio, btnguardar;
    private String entrevistaId;
    private String fechaOriginal;
    private Entrevista entrevista;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_entrevistas);

        databaseReference = FirebaseDatabase.getInstance().getReference("Entrevistas");
        storageReference = FirebaseStorage.getInstance().getReference("Entrevistas");

        etDescripcion = findViewById(R.id.etDescripcion);
        etPeriodista = findViewById(R.id.etPeriodista);
        etFecha = findViewById(R.id.etFecha);
        ivImagen = findViewById(R.id.ivImagen);
        btnSeleccionar = findViewById(R.id.btnSeleccionar);
        btnsubirAudio = findViewById(R.id.btnsubirAudio);
        btngrabarAudio = findViewById(R.id.btngrabarAudio);
        btnguardar= findViewById(R.id.btnguardar);

        entrevistaId = getIntent().getStringExtra("EntrevistaId");

        if (entrevistaId != null && !entrevistaId.isEmpty()) {
            cargarEntrevista(entrevistaId);
        } else {
            Toast.makeText(this, "ID de entrevista no válido", Toast.LENGTH_SHORT).show();
            finish();
        }

        btnSeleccionar.setOnClickListener(v -> mostrarDialogoSeleccionImagen());
        btnsubirAudio.setOnClickListener(v -> seleccionarAudio());
        btngrabarAudio.setOnClickListener(v -> checkAudioPermission());
        btnguardar                                                                                                                                                                                                                          .setOnClickListener(v -> guardarEntrevista());

        etFecha.setEnabled(false);
    }

    private void cargarEntrevista(String id) {
        databaseReference.child(id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Entrevista entrevista = snapshot.getValue(Entrevista.class);
                    if (entrevista != null) {
                        etDescripcion.setText(entrevista.getDescripcion());
                        etPeriodista.setText(entrevista.getPeriodista());
                        etFecha.setText(entrevista.getFecha());
                        fechaOriginal = entrevista.getFecha();

                        Glide.with(edit_entrevista.this)
                                .load(entrevista.getImagenUrl())
                                .into(ivImagen);
                        imagenUri = Uri.parse(entrevista.getImagenUrl());
                        audioUri = Uri.parse(entrevista.getAudioUrl());
                    }
                } else {
                    Toast.makeText(edit_entrevista.this, "Entrevista no encontrada", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(edit_entrevista.this, "Error al cargar los datos", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void mostrarDialogoSeleccionImagen() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Seleccionar Imagen");
        builder.setItems(new CharSequence[]{"Desde galería", "Abrir Cámara"},
                (dialog, which) -> {
                    if (which == 0) {
                        seleccionarImagen();
                    } else if (which == 1) {
                        checkCameraPermission();
                    }
                });
        builder.show();
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_IMAGE_CAPTURE);
        } else {
            capturarImagen();
        }
    }

    private void checkAudioPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_AUDIO_RECORD);
        } else {
            iniciarGrabacionAudio();
        }
    }

    private void capturarImagen() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private void iniciarGrabacionAudio() {
        Intent intent = new Intent(this, grabar_entrevista.class);
        startActivityForResult(intent, REQUEST_AUDIO_RECORD);
    }

    private void seleccionarImagen() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Seleccionar Imagen"), PICK_IMAGE_REQUEST);
    }

    private void seleccionarAudio() {
        Intent intent = new Intent();
        intent.setType("audio/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Seleccionar Audio"), PICK_AUDIO_REQUEST);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_AUDIO_RECORD && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            iniciarGrabacionAudio();
        } else {
            Toast.makeText(this, "Permiso de grabación de audio denegado", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            switch (requestCode) {
                case REQUEST_IMAGE_CAPTURE:
                    Bundle extras = data.getExtras();
                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    ivImagen.setImageBitmap(imageBitmap);
                    imagenUri = getImageUri(this, imageBitmap);
                    break;

                case PICK_IMAGE_REQUEST:
                    imagenUri = data.getData();
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imagenUri);
                        ivImagen.setImageBitmap(bitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;

                case PICK_AUDIO_REQUEST:
                    audioUri = data.getData();
                    break;

                case REQUEST_AUDIO_RECORD:
                    String filePath = data.getStringExtra("audioFilePath");
                    if (filePath != null) {
                        audioUri = Uri.fromFile(new File(filePath));
                    } else {
                        Toast.makeText(this, "Error al grabar audio", Toast.LENGTH_SHORT).show();
                    }
                    break;

                default:
                    break;
            }
        }
    }
    private Uri getImageUri(Context context, Bitmap image) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), image, "EntrevistaImagen", null);
        return Uri.parse(path);
    }

    private void guardarEntrevista() {
        final String descripcion = etDescripcion.getText().toString().trim();
        final String periodista = etPeriodista.getText().toString().trim();
        final String fecha = etFecha.getText().toString().trim();
        final String fechaModificacion = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        if (TextUtils.isEmpty(descripcion) || TextUtils.isEmpty(periodista) || imagenUri == null || audioUri == null) {
            Toast.makeText(this, "Seleccione una imagen y un audio", Toast.LENGTH_SHORT).show();
            return;
        }

        StorageReference imagenRef = storageReference.child(entrevistaId + "/imagen.jpg");
        imagenRef.putFile(imagenUri).addOnSuccessListener(taskSnapshot -> {
            imagenRef.getDownloadUrl().addOnSuccessListener(imagenUrl -> {
                // Subir audio a Firebase Storage
                StorageReference audioRef = storageReference.child(entrevistaId + "/audio.mp3");
                audioRef.putFile(audioUri).addOnSuccessListener(taskSnapshot1 -> {
                    audioRef.getDownloadUrl().addOnSuccessListener(audioUrl -> {
                        Entrevista entrevista = new Entrevista(entrevistaId, descripcion, periodista, fecha, imagenUrl.toString(), audioUrl.toString());
                        databaseReference.child(entrevistaId).setValue(entrevista)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(edit_entrevista.this, "Entrevista actualizada con éxito", Toast.LENGTH_SHORT).show();
                                        finish();
                                    } else {
                                        Toast.makeText(edit_entrevista.this, "Error al actualizar", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    });
                });
            });
        }).addOnFailureListener(e -> Toast.makeText(this, "Error al subir imagen", Toast.LENGTH_SHORT).show());
    }
}
