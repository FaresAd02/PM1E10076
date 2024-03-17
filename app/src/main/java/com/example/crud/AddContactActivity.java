package com.example.crud;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.ByteArrayOutputStream;

public class AddContactActivity extends AppCompatActivity implements LocationListener {
    private DatabaseManager dbManager;
    private int contactId = -1;
    private byte[] firmaBytes;
    private EditText nameEditText;
    private EditText phoneEditText;
    private EditText latitudEditText;
    private EditText longitudEditText;
    private SignatureView signatureView;
    private Button clearSignatureButton;
    private LocationManager locationManager;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);

        dbManager = new DatabaseManager(this);

        nameEditText = findViewById(R.id.nombre_edit_text);
        phoneEditText = findViewById(R.id.telefono_edit_text);
        latitudEditText = findViewById(R.id.latitud_edit_text);
        longitudEditText = findViewById(R.id.longitud_edit_text);
        Button saveButton = findViewById(R.id.save_button);
        signatureView = findViewById(R.id.signature_view);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        Intent intent = getIntent();
        if (intent.hasExtra("CONTACT_ID")) {
            contactId = intent.getIntExtra("CONTACT_ID", -1);
            String nombre = intent.getStringExtra("NOMBRE");
            String telefono = intent.getStringExtra("TELEFONO");
            double latitud = intent.getDoubleExtra("LATITUD", 0);
            double longitud = intent.getDoubleExtra("LONGITUD", 0);
            byte[] firma = intent.getByteArrayExtra("FIRMA");

            nameEditText.setText(nombre);
            phoneEditText.setText(telefono);
            latitudEditText.setText(String.valueOf(latitud));
            longitudEditText.setText(String.valueOf(longitud));

            if (firma != null && firma.length > 0) {
                Bitmap firmaBitmap = BitmapFactory.decodeByteArray(firma, 0, firma.length);
                signatureView.setSignatureBitmap(firmaBitmap);
            }
        }

        saveButton.setOnClickListener(view -> {
            if (validateInput()) {
                new SaveSignatureTask().execute(signatureView.getSignatureBitmap());
            }
        });

        Button buttonGetLatitud = findViewById(R.id.button_get_latitud);
        buttonGetLatitud.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            } else {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
            }
        });

        clearSignatureButton = findViewById(R.id.clear_signature_button);
        clearSignatureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signatureView.clear();
            }
        });
    }


    private boolean validateInput() {
        if (nameEditText.getText().toString().trim().isEmpty() ||
                phoneEditText.getText().toString().trim().isEmpty() ||
                latitudEditText.getText().toString().trim().isEmpty() ||
                longitudEditText.getText().toString().trim().isEmpty()) {
            mostrarAlertaCamposVacios();
            return false;
        } else if (!signatureView.hasSignature()) {
            Toast.makeText(this, "Por favor, firme antes de guardar.", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private class SaveSignatureTask extends AsyncTask<Bitmap, Void, Boolean> {

        protected Boolean doInBackground(Bitmap... bitmaps) {
            Bitmap signatureBitmap = bitmaps[0];
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            signatureBitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
            firmaBytes = bos.toByteArray();
            return true;
        }

        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if (result) {
                Toast.makeText(AddContactActivity.this, "Firma capturada exitosamente.", Toast.LENGTH_SHORT).show();
                guardarInformacionContacto(firmaBytes);
            } else {
                Toast.makeText(AddContactActivity.this, "Error al capturar la firma.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void guardarInformacionContacto(byte[] firmaBytes) {
        String nombre = nameEditText.getText().toString().trim();
        String telefono = phoneEditText.getText().toString().trim();
        double latitud = Double.parseDouble(latitudEditText.getText().toString().trim());
        double longitud = Double.parseDouble(longitudEditText.getText().toString().trim());

        if (contactId != -1) {
            Contacto contacto = new Contacto(contactId, nombre, telefono, latitud, longitud, firmaBytes);
            dbManager.actualizarContacto(contacto);
        } else {
            Contacto nuevoContacto = new Contacto(nombre, telefono, latitud, longitud, firmaBytes);
            dbManager.aniadirContacto(nuevoContacto);
        }
        setResult(RESULT_OK);
        finish();
    }

    private void mostrarAlertaCamposVacios() {
        new AlertDialog.Builder(this)
                .setTitle("Campos requeridos")
                .setMessage("Todos los campos son obligatorios. Por favor, complete la información antes de guardar.")
                .setPositiveButton(android.R.string.ok, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }


    @Override
    public void onLocationChanged(Location location) {
        if (latitudEditText != null && longitudEditText != null) {
            latitudEditText.setText(String.valueOf(location.getLatitude()));
            longitudEditText.setText(String.valueOf(location.getLongitude()));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocationUpdates();
            } else {
                Toast.makeText(this, "Permission denied to access your location.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void getLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (locationManager != null) {
            locationManager.removeUpdates(this);
        }
    }
}
