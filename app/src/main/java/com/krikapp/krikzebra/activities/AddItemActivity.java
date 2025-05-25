package com.krikapp.krikzebra.activities;



import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import com.krikapp.krikzebra.R;
import com.krikapp.krikzebra.database.AppDatabase;
import com.krikapp.krikzebra.model.Item;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AddItemActivity extends AppCompatActivity {
    private EditText editTextOrderCode, editTextQuantity;

    private TextView textViewError;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);

        editTextOrderCode = findViewById(R.id.editTextOrderCode);
        editTextQuantity = findViewById(R.id.editTextQuantity);
        textViewError = findViewById(R.id.textViewError);
        Button buttonSaveItem = findViewById(R.id.buttonSaveItem);

        db = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "inventory-db").build();

        int batchId = getIntent().getIntExtra("batchId", -1);

        buttonSaveItem.setOnClickListener(v -> {
            String orderCode = editTextOrderCode.getText().toString();
            String quantityStr = editTextQuantity.getText().toString().trim();
            String currentDateTime = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(new Date());

            // Validate input
            if (orderCode.isEmpty()) {
                textViewError.setText("Mã sản phẩm không được để trống.");
                textViewError.setVisibility(View.VISIBLE);
                return;
            }

            if (quantityStr.isEmpty()) {
                textViewError.setText("Số lượng không được để trống.");
                textViewError.setVisibility(View.VISIBLE);
                return;
            }
            int quantity = Integer.parseInt(quantityStr);
            Item newItem = new Item();
            newItem.batchId = batchId; // Gán batchId
            newItem.orderCode = orderCode;
            newItem.quantity = quantity;
            newItem.dateTime = currentDateTime;

            new Thread(() -> {
               long insertedtemId = db.inventoryDao().insertItem(newItem);
                runOnUiThread(() -> {
                    // Trả về item mới
                    Intent resultIntent = new Intent();
                    newItem.id = (int) insertedtemId;
                    resultIntent.putExtra("NEW_ITEM", newItem);
                    setResult(RESULT_OK, resultIntent);
                    if (db != null) {
                        db.close(); // Đóng kết nối
                    }
                    finish();});
            }).start();
        });
    }
}
