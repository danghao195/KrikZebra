package com.krikapp.krikzebra.activities;


import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import com.krikapp.krikzebra.R;
import com.krikapp.krikzebra.database.AppDatabase;
import com.krikapp.krikzebra.model.Item;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Executors;

public class EditItemActivity extends AppCompatActivity {
    private EditText editTextOrderCode, editTextQuantity;
    private AppDatabase db;
    private int itemId;

    private int batchId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_item);

        editTextOrderCode = findViewById(R.id.editTextOrderCode);
        editTextQuantity = findViewById(R.id.editTextQuantity);
        Button buttonSaveItem = findViewById(R.id.buttonSaveItem);

        db = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "inventory-db").build();

        itemId = getIntent().getIntExtra("itemId", -1);
        batchId = getIntent().getIntExtra("batchId", -1);
        Executors.newSingleThreadExecutor().execute(() -> {
            Item item = db.inventoryDao().getItemById(itemId, batchId);
            runOnUiThread(() -> {
                editTextOrderCode.setText(item.orderCode);
                editTextQuantity.setText(String.valueOf(item.quantity));
            });
        });

        buttonSaveItem.setOnClickListener(v -> {
            String orderCode = editTextOrderCode.getText().toString();
            int quantity = Integer.parseInt(editTextQuantity.getText().toString());

            Item item = new Item();
            item.id = itemId;
            item.batchId = batchId;
            item.orderCode = orderCode;
            item.quantity = quantity;
            item.dateTime = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(new Date());
            Executors.newSingleThreadExecutor().execute(() -> {
                try {
                    db.inventoryDao().updateItem(item);
                    // Trả về kết quả cho Activity gọi
                    Intent resultIntent;
                    resultIntent = new Intent();
                    resultIntent.putExtra("UPDATED_ITEM", item); // Truyền item đã cập nhật
                    setResult(RESULT_OK, resultIntent);
                    finish();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (db != null) {
                        db.close(); // Đóng kết nối
                    }
                }
            });
        });
    }
}
