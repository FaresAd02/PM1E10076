package com.example.crud;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.Locale;

public class ContactDetailActivity extends AppCompatActivity {
    private DatabaseManager dbManager;
    private Contacto contactoActual;
    private int contactId;
    private ImageView firmaImageView;
    private static final int UPDATE_CONTACT_REQUEST = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_detail);

        dbManager = new DatabaseManager(this);

        contactId = (int) getIntent().getLongExtra("CONTACT_ID", -1);
        if (contactId == -1) {
            finish();
            return;
        }

        Contacto contacto = dbManager.getContacto((int) contactId);
        contactoActual = dbManager.getContacto(contactId);

        TextView nombreTextView = findViewById(R.id.nombre_text_view);
        TextView telefonoTextView = findViewById(R.id.telefono_text_view);
        TextView latitudTextView = findViewById(R.id.latitud_text_view);
        TextView longitudTextView = findViewById(R.id.longitud_text_view);

        firmaImageView = findViewById(R.id.firma_image_view);

        Button actualizarButton = findViewById(R.id.button_actualizar_contacto);
        Button eliminarButton = findViewById(R.id.button_eliminar_contacto);
        Button compartirButton = findViewById(R.id.button_compartir_contacto);
        Button llamarButton = findViewById(R.id.button_llamar_contacto);


        if (contacto != null) {
            nombreTextView.setText(contactoActual.getNombre());
            telefonoTextView.setText(contactoActual.getTelefono());
            latitudTextView.setText(String.format(Locale.getDefault(), "%.4f", contactoActual.getLatitud()));
            longitudTextView.setText(String.format(Locale.getDefault(), "%.4f", contactoActual.getLongitud()));

            if (contacto.getFirma() != null) {
                Bitmap firmaBitmap = BitmapFactory.decodeByteArray(contacto.getFirma(), 0, contacto.getFirma().length);
                firmaImageView.setImageBitmap(firmaBitmap);
            }

            actualizarButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(ContactDetailActivity.this, AddContactActivity.class);
                    intent.putExtra("CONTACT_ID", contactId);
                    intent.putExtra("NOMBRE", contacto.getNombre());
                    intent.putExtra("TELEFONO", contacto.getTelefono());
                    intent.putExtra("LATITUD", contacto.getLatitud());
                    intent.putExtra("LONGITUD", contacto.getLongitud());
                    intent.putExtra("FIRMA", contacto.getFirma());
                    startActivityForResult(intent, UPDATE_CONTACT_REQUEST);
                }
            });

            eliminarButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new AlertDialog.Builder(ContactDetailActivity.this)
                            .setTitle("Eliminar Contacto")
                            .setMessage("¿Está seguro de que quiere eliminar este contacto?")
                            .setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dbManager.eliminarContacto(contactoActual);
                                    finish();
                                }
                            })
                            .setNegativeButton("No", null)
                            .show();
                }
            });

            compartirButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    compartirContacto();
                }
            });

            llamarButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    llamarContacto();
                }
            });

        } else {
            actualizarButton.setVisibility(View.INVISIBLE);
            eliminarButton.setVisibility(View.INVISIBLE);
        }

        if (contacto.getFirma() != null && contacto.getFirma().length > 0) {
            Bitmap firmaBitmap = BitmapFactory.decodeByteArray(contacto.getFirma(), 0, contacto.getFirma().length);
            firmaImageView.setImageBitmap(firmaBitmap);
        } else {
        }

        Button buttonAbrirMapa = findViewById(R.id.button_abrir_mapa);
        buttonAbrirMapa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                double latitud = Double.parseDouble(latitudTextView.getText().toString());
                double longitud = Double.parseDouble(longitudTextView.getText().toString());
                String geoUri = "http://maps.google.com/maps?q=loc:" + latitud + "," + longitud;
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(geoUri));
                startActivity(intent);
            }
        });
    }

    private void compartirContacto() {
        String numeroConPrefijo = getPrefijoPais(contactoActual + contactoActual.getTelefono());
        String contactInfo = "Contacto: " + contactoActual.getNombre() +
                "\nTeléfono: " + numeroConPrefijo;

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, contactInfo);
        sendIntent.setType("text/plain");

        Intent shareIntent = Intent.createChooser(sendIntent, null);
        startActivity(shareIntent);
    }

    private void llamarContacto() {
        final String numeroConPrefijo = getPrefijoPais(contactoActual + contactoActual.getTelefono());
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, 22);
        } else {
            mostrarDialogoConfirmacionLlamada(numeroConPrefijo);
        }
    }

    private void mostrarDialogoConfirmacionLlamada(final String phoneNumber) {
        new AlertDialog.Builder(ContactDetailActivity.this)
                .setMessage("¿Desea llamar a " + phoneNumber + "?")
                .setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        realizarLlamada(phoneNumber);
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void realizarLlamada(String phoneNumberWithPrefix) {
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:" + phoneNumberWithPrefix));
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            startActivity(intent);
        }
    }

    private String getPrefijoPais(String countryName) {
        int resId = getResources().getIdentifier("prefijo_" + countryName.toLowerCase(), "string", getPackageName());
        return resId != 0 ? getString(resId) : "";
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == UPDATE_CONTACT_REQUEST) {
            if (resultCode == RESULT_OK) {
                contactoActual = dbManager.getContacto(contactId);
                updateUI();
            }
        }
    }

    private void updateUI() {
        TextView nombreTextView = findViewById(R.id.nombre_text_view);
        TextView telefonoTextView = findViewById(R.id.telefono_text_view);
        TextView latitudTextView = findViewById(R.id.latitud_text_view);
        TextView longitudTextView = findViewById(R.id.longitud_text_view);
        if (contactoActual != null) {
            nombreTextView.setText(contactoActual.getNombre());
            telefonoTextView.setText(contactoActual.getTelefono());
            latitudTextView.setText(String.format(Locale.getDefault(), "%.4f", contactoActual.getLatitud()));
            longitudTextView.setText(String.format(Locale.getDefault(), "%.4f", contactoActual.getLongitud()));
            if (contactoActual.getFirma() != null) {
                Bitmap firmaBitmap = BitmapFactory.decodeByteArray(contactoActual.getFirma(), 0, contactoActual.getFirma().length);
                firmaImageView.setImageBitmap(firmaBitmap);
            }
        }
    }

}