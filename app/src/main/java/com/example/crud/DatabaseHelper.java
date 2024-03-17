    package com.example.crud;

    import android.content.ContentValues;
    import android.content.Context;
    import android.database.Cursor;
    import android.database.sqlite.SQLiteDatabase;
    import android.database.sqlite.SQLiteOpenHelper;

    public class DatabaseHelper extends SQLiteOpenHelper {
        private static final String DATABASE_NAME = "contactos2.db";
        private static final int DATABASE_VERSION = 1;
        private static final String TABLE_NAME = "contactos";
        private static final String COLUMN_ID = "_id";
        private static final String COLUMN_NOMBRE = "nombre";
        private static final String COLUMN_TELEFONO = "telefono";
        private static final String COLUMN_LATITUD = "latitud";
        private static final String COLUMN_LONGITUD = "longitud";
        private static final String COLUMN_FIRMA = "firma";

        private static final String TABLE_CREATE =
                "CREATE TABLE " + TABLE_NAME + " (" +
                        COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COLUMN_NOMBRE + " TEXT, " +
                        COLUMN_TELEFONO + " TEXT, " +
                        COLUMN_LATITUD + " REAL, " +
                        COLUMN_LONGITUD + " REAL, " +
                        COLUMN_FIRMA + " BLOB" +
                        ")";

        public DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(TABLE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(db);
        }

        public int actualizarContacto(Contacto contacto) {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(COLUMN_NOMBRE, contacto.getNombre());
            values.put(COLUMN_TELEFONO, contacto.getTelefono());
            values.put(COLUMN_LATITUD, contacto.getLatitud());
            values.put(COLUMN_LONGITUD, contacto.getLongitud());
            values.put(COLUMN_FIRMA, contacto.getFirma());

            return db.update(TABLE_NAME, values, COLUMN_ID + " = ?",
                    new String[]{String.valueOf(contacto.getId())});
        }

        public void eliminarContacto(Contacto contacto) {
            SQLiteDatabase db = this.getWritableDatabase();
            db.delete(TABLE_NAME, COLUMN_ID + " = ?",
                    new String[]{String.valueOf(contacto.getId())});
            db.close();
        }

        public Contacto getContacto(int id) {
            SQLiteDatabase db = this.getReadableDatabase();
            Contacto contacto = null;
            Cursor cursor = db.query(TABLE_NAME, new String[]{COLUMN_ID, COLUMN_NOMBRE,
                            COLUMN_TELEFONO, COLUMN_LATITUD,COLUMN_LONGITUD,COLUMN_FIRMA}, COLUMN_ID + "=?",
                    new String[]{String.valueOf(id)}, null, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                contacto = new Contacto(
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOMBRE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TELEFONO)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_LATITUD)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_LONGITUD)),
                        cursor.getBlob(cursor.getColumnIndexOrThrow(COLUMN_FIRMA)));
                cursor.close();
            }

            return contacto;
        }

        public Cursor getAllContactos() {
            SQLiteDatabase db = this.getReadableDatabase();
            return db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
        }

        public void aniadirContacto(Contacto contacto) {
            SQLiteDatabase db = this.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(COLUMN_NOMBRE, contacto.getNombre());
            values.put(COLUMN_TELEFONO, contacto.getTelefono());
            values.put(COLUMN_LATITUD, contacto.getLatitud());
            values.put(COLUMN_LONGITUD, contacto.getLongitud());
            values.put(COLUMN_FIRMA, contacto.getFirma());

            db.insert(TABLE_NAME, null, values);
            db.close();
        }

        public Cursor buscarContactos(String query) {
            SQLiteDatabase db = this.getReadableDatabase();
            String sql = "SELECT * FROM " + TABLE_NAME + " WHERE " +
                    COLUMN_NOMBRE + " LIKE ? OR " +
                    COLUMN_TELEFONO + " LIKE ? OR " +
                    "CAST(" + COLUMN_LATITUD + " AS TEXT) LIKE ? OR " +
                    "CAST(" + COLUMN_LONGITUD + " AS TEXT) LIKE ?";
            String[] selectionArgs = new String[]{"%" + query + "%", "%" + query + "%", "%" + query + "%", "%" + query + "%"};
            return db.rawQuery(sql, selectionArgs);
        }

        public long guardarFirma(int contactId, byte[] firma) {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(COLUMN_FIRMA, firma);
            int affectedRows = db.update(TABLE_NAME, values, COLUMN_ID + "=?", new String[]{String.valueOf(contactId)});
            db.close();
            return affectedRows;
        }
    }
