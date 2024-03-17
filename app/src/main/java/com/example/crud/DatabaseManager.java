package com.example.crud;

import android.content.ContentValues;
import android.database.Cursor;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;


public class DatabaseManager {
    private DatabaseHelper dbHelper;

    public DatabaseManager(Context context) {
        this.dbHelper = new DatabaseHelper(context);
    }

    public void aniadirContacto(Contacto contacto) {
        dbHelper.aniadirContacto(contacto);
    }

    public int actualizarContacto(Contacto contacto) {
        return dbHelper.actualizarContacto(contacto);
    }

    public void eliminarContacto(Contacto contacto) {
        dbHelper.eliminarContacto(contacto);
    }

    public Contacto getContacto(int id) {
        return dbHelper.getContacto(id);
    }

    public Cursor getAllContacts() {
        return dbHelper.getAllContactos();
    }

    public Cursor buscarContactos(String query) {
        return dbHelper.buscarContactos(query);
    }

    public long guardarFirma(int contactId, byte[] firma) {
        return dbHelper.guardarFirma(contactId, firma);
    }
}