package com.krikapp.krikzebra.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

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

    private TextView textViewError;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_batch);

        editTextBatchName = findViewById(R.id.editTextBatchName);
        editTextCreator = findViewById(R.id.editTextCreator);
        textViewError = findViewById(R.id.textViewError);
        Button buttonSaveBatch = findViewById(R.id.buttonSaveBatch);

        db = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "inventory-db").build();

        buttonSaveBatch.setOnClickListener(v -> {
            String batchName = editTextBatchName.getText().toString();
            String creator = editTextCreator.getText().toString();
            // Validate input
            if (batchName.isEmpty()) {
                textViewError.setText("Tên đợt kiêm kho không được để trống.");
                textViewError.setVisibility(View.VISIBLE);
                return;
            }

            if (creator.isEmpty()) {
                textViewError.setText("Người tạo không được để trống.");
                textViewError.setVisibility(View.VISIBLE);
                return;
            }
            String createdDate = new SimpleDateFormat("dd/MM/yyyy  HH:mm:ss", Locale.getDefault()).format(new Date());

            InventoryBatch newBatch = new InventoryBatch();
            newBatch.name = batchName;
            newBatch.creator = creator;
            newBatch.createdDate = createdDate;

            new Thread(() -> {
                long batchId = db.inventoryDao().insertBatch(newBatch);
                newBatch.id = (int) batchId;
                runOnUiThread(() -> {
                    finish();
                    Intent intent = new Intent(this, ItemActivity.class);
                    intent.putExtra("batchId", (int)batchId); // Truyền ID của đợt kiểm kho
                    startActivity(intent);
                });
            }).start();
        });
    }
}
