package com.krikapp.krikzebra.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import com.krikapp.krikzebra.database.AppDatabase;

import com.krikapp.krikzebra.R;
import com.krikapp.krikzebra.model.InventoryBatch;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CreateBatchActivity extends AppCompatActivity {
    private EditText editTextBatchName, editTextCreator;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_batch);

        editTextBatchName = findViewById(R.id.editTextBatchName);
        editTextCreator = findViewById(R.id.editTextCreator);
        Button buttonSaveBatch = findViewById(R.id.buttonSaveBatch);

        db = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "inventory-db").build();

        buttonSaveBatch.setOnClickListener(v -> {
            String batchName = editTextBatchName.getText().toString();
            String creator = editTextCreator.getText().toString();
            String createdDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());

            InventoryBatch newBatch = new InventoryBatch();
            newBatch.name = batchName;
            newBatch.creator = creator;
            newBatch.createdDate = createdDate;

            new Thread(() -> {
                db.inventoryDao().insertBatch(newBatch);
                runOnUiThread(() -> finish());
            }).start();
        });
    }
}
