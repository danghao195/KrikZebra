package com.krikapp.krikzebra.task;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.krikapp.krikzebra.database.AppDatabase;
import com.krikapp.krikzebra.model.ExportItem;
import com.krikapp.krikzebra.model.InventoryBatch;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CreateSendExcelFileTask extends AsyncTask<Void, Void, Void> {
    private ProgressDialog progressDialog;
    private List<InventoryBatch> inventoryCheckList; // Danh sách các đợt kiểm kho
    private Context context;
    private AppDatabase db;

    public CreateSendExcelFileTask(Context context, List<InventoryBatch> inventoryCheckList,AppDatabase db) {
        this.context = context;
        this.inventoryCheckList = inventoryCheckList;
        this.db = db;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Đang tạo file...");
        progressDialog.setCancelable(false);
        progressDialog.show();
    }
    @Override
    protected Void doInBackground(Void... voids) {
        createExcelFile(); // Gọi hàm tạo file Excel
        return null;
    }

    private void createExcelFile() {
        try {
            String fileName = getCurrentFileName();
            FileInputStream fis = new FileInputStream(copyFileFromAssets(fileName));
            Workbook workbook = new XSSFWorkbook(fis);
            // Lấy sheet thứ 2
            Sheet sheet = workbook.getSheetAt(1);
            int rowNum = 1;
            List<Long> ids = convertObjectIdList(inventoryCheckList);
            List<ExportItem> items = db.inventoryDao().getItemsByBatchIds(ids/*batch.id*/);
            for (ExportItem item : items) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(2).setCellValue(item.orderCode);
                row.createCell(3).setCellValue(item.sumQuantity);
            }
            File file = new File(context.getExternalFilesDir(null), fileName);
            FileOutputStream fileOut = new FileOutputStream(file);
            workbook.write(fileOut);
            fileOut.close();

            // Chia sẻ file
            Uri fileUri = FileProvider.getUriForFile(this.context, context.getPackageName() + ".fileprovider", file);
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            context.startActivity(Intent.createChooser(shareIntent, "Chia sẻ file"));

        } catch (IOException e) {
            e.printStackTrace();
            //runOnUiThread(() -> Toast.makeText(this, "Lỗi xuất file: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }

    private String getCurrentFileName(){
        Date currentDate = new Date();
        // Define the desired date format
        SimpleDateFormat dateFormat = new SimpleDateFormat("_yyyyMMdd_HHmmss");
        // Convert the Date object to a String
        String formattedDate = dateFormat.format(currentDate);
        // Trả về tên file theo thời gian hiện tại.
        return "exported_file"+formattedDate+".xlsm";
    }
    private File copyFileFromAssets(String fileName) {
        File directory = context.getFilesDir();
        File excelFile = new File(directory, fileName);

        try (InputStream is = context.getAssets().open("Nhanh_Import_Inventory.xlsm");
             FileOutputStream fos = new FileOutputStream(excelFile)) {

            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                fos.write(buffer, 0, length);
            }

            // File đã được sao chép thành công
            System.out.println("File đã được sao chép: " + excelFile.getAbsolutePath());

        } catch (IOException e) {
            e.printStackTrace();
        }
        return  excelFile;
    }
    public static List<Long> convertObjectIdList(List<InventoryBatch> objectList) {
        List<Long> idList = new ArrayList<>();
        for (InventoryBatch obj : objectList) {
            idList.add((long) obj.id);
        }
        return idList;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        // Có thể hiển thị thông báo hoàn tất ở đây
    }
}
