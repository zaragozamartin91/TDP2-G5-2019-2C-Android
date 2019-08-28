package com.g5.tdp2.cashmaps;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import java.util.List;
import java.util.stream.Collectors;

public class SearchActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        Intent intent = getIntent();
        List<String> banks = ((List<?>) intent.getSerializableExtra("banks")).stream().map(Object::toString).collect(Collectors.toList());

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, banks);

        AutoCompleteTextView actv = findViewById(R.id.search_input_bank);
        actv.setThreshold(1);//will start working from first character
        actv.setAdapter(adapter);//setting the adapter data into the AutoCompleteTextView
        actv.setTextColor(Color.RED);

        actv.setOnItemClickListener((parent, view, position, id) -> {
            String item = parent.getItemAtPosition(position).toString();
            Toast.makeText(getApplicationContext(), "Banco seleccionado: " + item, Toast.LENGTH_SHORT).show();
        });

        Button searchBtn = findViewById(R.id.search_btn);
        searchBtn.setOnClickListener(
                view -> Snackbar.make(view, "Esto dispara el mapa de google maps", Snackbar.LENGTH_SHORT).show()
        );
    }
}
