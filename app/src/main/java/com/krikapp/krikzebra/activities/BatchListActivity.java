package com.krikapp.krikzebra.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.krikapp.krikzebra.R;
import com.krikapp.krikzebra.adapter.BatchAdapter;
import com.krikapp.krikzebra.database.AppDatabase;
import com.krikapp.krikzebra.model.ExportItem;
import com.krikapp.krikzebra.model.InventoryBatch;
import com.krikapp.krikzebra.task.CreateSendExcelFileTask;

import java.io.FileInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import android.content.Intent;
import android.net.Uri;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.util.ArrayList;

public class BatchListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private BatchAdapter adapter;
    private AppDatabase db;
    private Button buttonDeleteSelected;
    private Button buttonExportSelected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_batch_list);

        recyclerView = findViewById(R.id.recyclerViewBatches);
        buttonDeleteSelected = findViewById(R.id.buttonDeleteSelected);
        buttonExportSelected = findViewById(R.id.buttonExportSelected);

        db = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "inventory-db").build();

        loadBatchList();

        buttonDeleteSelected.setOnClickListener(v -> {
            List<InventoryBatch> selectedBatches = adapter.getSelectedBatches();
            if (!selectedBatches.isEmpty()) {
                new Thread(() -> {
                    for (InventoryBatch batch : selectedBatches) {
                        db.inventoryDao().deleteBatch(batch);
                    }
                    runOnUiThread(this::loadBatchList);
                }).start();
            } else {
                Toast.makeText(this, "Vui lòng chọn ít nhất một đợt để xóa.", Toast.LENGTH_SHORT).show();
            }
        });

        buttonExportSelected.setOnClickListener(v -> {
            List<InventoryBatch> selectedBatches = adapter.getSelectedBatches();
            if (!selectedBatches.isEmpty()) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                } else {
                    new CreateSendExcelFileTask(BatchListActivity.this,selectedBatches,db).execute();
                    //exportSelectedBatchesToExcel(selectedBatches);
                }
            } else {
                Toast.makeText(this, "Vui lòng chọn ít nhất một đợt để xuất.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadBatchList() {
        new Thread(() -> {
            List<InventoryBatch> batchList = db.inventoryDao().getAllBatches();
            runOnUiThread(() -> {
                adapter = new BatchAdapter(batchList, BatchListActivity.this);
                recyclerView.setAdapter(adapter);
                recyclerView.setLayoutManager(new LinearLayoutManager(this));

                updateButtonVisibility();
            });
        }).start();
    }

    public void updateButtonVisibility() {
        List<InventoryBatch> selectedBatches = adapter.getSelectedBatches();
        if (selectedBatches.isEmpty()) {
            buttonDeleteSelected.setVisibility(View.GONE);
            buttonExportSelected.setVisibility(View.GONE);
        } else {
            buttonDeleteSelected.setVisibility(View.VISIBLE);
            buttonExportSelected.setVisibility(View.VISIBLE);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                List<InventoryBatch> selectedBatches = adapter.getSelectedBatches();
                new CreateSendExcelFileTask(BatchListActivity.this,selectedBatches,db).execute();
            } else {
                Toast.makeText(this, "Cần quyền truy cập bộ nhớ để xuất dữ liệu.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}