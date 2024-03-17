package com.example.crud;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ListView;

import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;

import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity {
    private ListView listViewContacts;
    private Button buttonAddContact;
    private DatabaseManager dbManager;
    private CursorAdapter adapter;
    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbManager = new DatabaseManager(this);
        listViewContacts = findViewById(R.id.list_view_contacts);
        buttonAddContact = findViewById(R.id.button_add_contact);
        searchView = findViewById(R.id.search_view_contactos);
        configurarSearchView();

        Cursor cursor = dbManager.getAllContacts();
        String[] from = new String[] { "nombre" };
        int[] to = new int[] { android.R.id.text1 };

        adapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_1, cursor, from, to, 0);
        listViewContacts.setAdapter(adapter);

        listViewContacts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, ContactDetailActivity.class);
                intent.putExtra("CONTACT_ID", id);
                startActivity(intent);
            }
        });

        buttonAddContact.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddContactActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Cursor newCursor = dbManager.getAllContacts();
        adapter.changeCursor(newCursor);
    }

    private void configurarSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                hacerBusqueda(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                hacerBusqueda(newText);
                return true;
            }
        });
    }

    private void hacerBusqueda(String query) {
        Cursor oldCursor = adapter.getCursor();
        if (oldCursor != null && !oldCursor.isClosed()) {
            oldCursor.close();
        }

        Cursor newCursor = dbManager.buscarContactos(query);
        adapter.changeCursor(newCursor);
    }

}
