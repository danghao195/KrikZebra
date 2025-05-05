package com.krikapp.krikzebra.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.krikapp.krikzebra.R;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button buttonCreateBatch = findViewById(R.id.buttonCreateBatch);
        Button buttonBatchList = findViewById(R.id.buttonBatchList);

        buttonCreateBatch.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CreateBatchActivity.class);
            startActivity(intent);
        });

        buttonBatchList.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, BatchListActivity.class);
            startActivity(intent);
        });
    }
}