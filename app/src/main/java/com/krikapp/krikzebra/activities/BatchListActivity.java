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
import com.krikapp.krikzebra.model.Item;

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
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
                    exportSelectedBatchesToExcel(selectedBatches);
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
    public static List<Long> convertObjectIdList(List<InventoryBatch> objectList) {
        List<Long> idList = new ArrayList<>();
        for (InventoryBatch obj : objectList) {
            idList.add((long) obj.id);
        }
        return idList;
    }
    private void exportSelectedBatchesToExcel(List<InventoryBatch> selectedBatches) {
        new Thread(() -> {
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Inventory Data");

            // Tạo tiêu đề cột
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Mã Đơn Hàng");
            headerRow.createCell(1).setCellValue("Số Lượng");

            int rowNum = 1;
            try {
                //for (InventoryBatch batch : selectedBatches) {
                List<Long> ids = convertObjectIdList(selectedBatches);
                    List<ExportItem> items = db.inventoryDao().getItemsByBatchIds(ids/*batch.id*/);
                    for (ExportItem item : items) {
                        Row row = sheet.createRow(rowNum++);
                        row.createCell(0).setCellValue(item.orderCode);
                        row.createCell(1).setCellValue(item.sumQuantity);
                    }
                //}
                Date currentDate = new Date();

                // Define the desired date format
                SimpleDateFormat dateFormat = new SimpleDateFormat("_yyyyMMdd_HHmmss");

                // Convert the Date object to a String
                String formattedDate = dateFormat.format(currentDate);
                // Lưu file vào bộ nhớ
                File file = new File(getExternalFilesDir(null), "exported_file"+formattedDate+".xlsx");
                FileOutputStream fileOut = new FileOutputStream(file);
                workbook.write(fileOut);
                fileOut.close();

                // Chia sẻ file
                Uri fileUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", file);
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(Intent.createChooser(shareIntent, "Chia sẻ file"));

            } catch (IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Lỗi xuất file: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                List<InventoryBatch> selectedBatches = adapter.getSelectedBatches();
                exportSelectedBatchesToExcel(selectedBatches);
            } else {
                Toast.makeText(this, "Cần quyền truy cập bộ nhớ để xuất dữ liệu.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}