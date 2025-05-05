package com.krikapp.krikzebra.activities;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.krikapp.krikzebra.R;
import com.krikapp.krikzebra.adapter.ItemAdapter;
import com.krikapp.krikzebra.database.AppDatabase;
import com.krikapp.krikzebra.model.Item;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

import com.symbol.emdk.EMDKManager;
import com.symbol.emdk.EMDKManager.EMDKListener;
import com.symbol.emdk.EMDKResults;
import com.symbol.emdk.barcode.BarcodeManager;
import com.symbol.emdk.barcode.ScanDataCollection;
import com.symbol.emdk.barcode.ScanDataCollection.ScanData;
import com.symbol.emdk.barcode.Scanner;
import com.symbol.emdk.barcode.Scanner.DataListener;
import com.symbol.emdk.barcode.Scanner.StatusListener;
import com.symbol.emdk.barcode.Scanner.TriggerType;
import com.symbol.emdk.barcode.ScannerConfig;
import com.symbol.emdk.barcode.ScannerException;
import com.symbol.emdk.barcode.ScannerResults;
import com.symbol.emdk.barcode.StatusData;
import com.symbol.emdk.barcode.StatusData.ScannerStates;

public class ItemActivity extends AppCompatActivity  implements EMDKListener, StatusListener, DataListener {
    @Override
    public void onOpened(EMDKManager emdkManager) {
        // TODO Auto-generated method stub
        // Get a reference to EMDKManager
        this.emdkManager =  emdkManager;

        // Get a  reference to the BarcodeManager feature object
        initBarcodeManager();

        // Initialize the scanner
        initScanner();
    }
    @Override
    public void onClosed() {
        // TODO Auto-generated method stub
        // The EMDK closed unexpectedly. Release all the resources.
        if (emdkManager != null) {
            emdkManager.release();
            emdkManager= null;
        }
        updateStatus("EMDK closed unexpectedly! Please close and restart the application.");
    }
    @Override
    public void onStatus(StatusData statusData) {
        // TODO Auto-generated method stub
        // The status will be returned on multiple cases. Check the state and take the action.
        // Get the current state of scanner in background
        ScannerStates state =  statusData.getState();
        String statusStr = "";
        // Different states of Scanner
        switch (state) {
            case IDLE:
            // Scanner is idle and ready to change configuration and submit read.
            statusStr = statusData.getFriendlyName()+" is   enabled and idle...";
            // Change scanner configuration. This should be done while the scanner is in IDLE state.
            setConfig();
            try {
                // Starts an asynchronous Scan. The method will NOT turn ON the scanner beam,
                //but puts it in a  state in which the scanner can be turned on automatically or by pressing a hardware trigger.
                scanner.read();
            }
            catch (ScannerException e)   {
                updateStatus(e.getMessage());
            }
            break;
            case WAITING:
            // Scanner is waiting for trigger press to scan...
            statusStr = "Scanner is waiting for trigger press...";
            break;
            case SCANNING:
            // Scanning is in progress...
            statusStr = "Scanning...";
            break;
            case DISABLED:
            // Scanner is disabledstatusStr = statusData.getFriendlyName()+" is disabled.";
            break;
            case ERROR:
            // Error has occurred during scanning
            statusStr = "An error has occurred.";
            break;
            default:
            break;
        }
        // Updates TextView with scanner state on UI thread.
      //  updateStatus(statusStr);
    }
    private void updateStatus(final String status) {
        Log.d("status:",status);
       // Toast.makeText(this, "Barcode status is: " + status, Toast.LENGTH_LONG).show();
    }
    private void setConfig() {
        if (scanner != null) {try {
            // Get scanner config
            ScannerConfig config = scanner.getConfig();
            // Enable haptic feedback
            if (config.isParamSupported("config.scanParams.decodeHapticFeedback")) {
                config.scanParams.decodeHapticFeedback = true;
            }
            // Set scanner config
            scanner.setConfig(config);
        } catch (ScannerException e)   {
            updateStatus(e.getMessage());
        }
        }
    }
    @Override
    public void onData(ScanDataCollection scanDataCollection) {
        // TODO Auto-generated method stub
        // The ScanDataCollection object gives scanning result and the collection of ScanData. Check the data and its status.
        String dataStr = "";
        if ((scanDataCollection != null) &&   (scanDataCollection.getResult() == ScannerResults.SUCCESS)) {
            ArrayList<ScanData> scanData =  scanDataCollection.getScanData();
            // Iterate through scanned data and prepare the data.
            for (ScanData data :  scanData) {
                // Get the scanned data
                String barcodeData =  data.getData();
                // Get the type of label being scanned
                ScanDataCollection.LabelType labelType = data.getLabelType();
                // Concatenate barcode data and label type
                dataStr =  barcodeData + "  " +  labelType;
            }
            // Update EditText with scanned data and type of label on UI thread.
            updateData(dataStr);
        }
    }
    // Variables to hold EMDK related objects
    private EMDKManager emdkManager = null;
    private BarcodeManager barcodeManager = null;
    private Scanner scanner = null;
    // Variables to hold handlers of UI controls
    private TextView statusTextView = null;
    private EditText dataView = null;

    private RecyclerView recyclerView;
    private ItemAdapter adapter;
    private AppDatabase db;
    private List<Item> itemList;
    private int batchId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_list);

        recyclerView = findViewById(R.id.recyclerViewItems);
        Button buttonAddItem = findViewById(R.id.buttonAddItem);
        db = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "inventory-db").build();

        batchId = getIntent().getIntExtra("batchId", -1);
        loadItemList();

        buttonAddItem.setOnClickListener(v -> {
            Intent intent = new Intent(ItemActivity.this, AddItemActivity.class);
            intent.putExtra("batchId", batchId);
            //startActivity(intent);
            startActivityForResult(intent, CommonInstance.NEW_ITEM_REQUEST);
        });

        // Requests the EMDKManager object. This is an asynchronous call and should be called from the main thread.
        // The callback also will receive in the main thread without blocking it until the EMDK resources are ready.
        EMDKResults results = EMDKManager.getEMDKManager(getApplicationContext(), this);

        // Check the return status of getEMDKManager() and update the status TextView accordingly.
        if (results.statusCode!=   EMDKResults.STATUS_CODE.SUCCESS) {
            updateStatus("EMDKManager object request failed!");
            return;
        } else {
            updateStatus("EMDKManager object initialization is   in   progress.......");
        }
    }
    private void initBarcodeManager() {
        // Get the feature object such as BarcodeManager object for accessing the feature.
        barcodeManager =  (BarcodeManager)emdkManager.getInstance(EMDKManager.FEATURE_TYPE.BARCODE);
        // Add external scanner connection listener.
        if (barcodeManager == null) {
            Toast.makeText(this, "Barcode scanning is not supported.", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void initScanner() {
        if (scanner == null) {
            // Get default scanner defined on the device
            scanner = barcodeManager.getDevice(BarcodeManager.DeviceIdentifier.DEFAULT);
            if(scanner != null) {
                // Implement the DataListener interface and pass the pointer of this object to get the data callbacks.
                scanner.addDataListener(this);

                // Implement the StatusListener interface and pass the pointer of this object to get the status callbacks.
                scanner.addStatusListener(this);

                // Hard trigger. When this mode is set, the user has to manually
                // press the trigger on the device after issuing the read call.
                // NOTE: For devices without a hard trigger, use TriggerType.SOFT_ALWAYS.
                scanner.triggerType =  TriggerType.HARD;

                try{
                    // Enable the scanner
                    // NOTE: After calling enable(), wait for IDLE status before calling other scanner APIs
                    // such as setConfig() or read().
                    scanner.enable();

                } catch (ScannerException e) {
                    updateStatus(e.getMessage());
                    deInitScanner();
                }
            } else {
                updateStatus("Failed to   initialize the scanner device.");
            }
        }
    }
    private void deInitScanner() {
        if (scanner != null) {
            try {
                // Release the scanner
                scanner.release();
            } catch (Exception e)   {
                updateStatus(e.getMessage());
            }
            scanner = null;
        }
    }

    private void updateData(final String result) {
        Log.d("status:",result);
        String valueOfBarCode= (result.split("  "))[0];
        addOrUpdateItem(valueOfBarCode);
        //Toast.makeText(this, "Barcode is: " + result, Toast.LENGTH_LONG).show();
    }
    public void addOrUpdateItem(String itemCode) {
        // Giả sử bạn có một danh sách item
        for (Item item : itemList ) {
            if (item.orderCode.equals(itemCode)) {
                // Nếu mã mặt hàng đã có, tăng số lượng lên 1
                item.quantity = item.quantity + 1;
                item.dateTime = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(new Date());
                //update vào database
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        // Thêm item vào database

                        db.inventoryDao().updateItem(item);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    adapter.updateItem(item); // Cập nhật danh sách trong adapter
                                    recyclerView.scrollToPosition(0); // Cuộn lên đầu danh sách

                                }
                            });

                    }
                }).start();
                //adapter.notifyDataSetChanged(); // Cập nhật giao diện
                return;
            }
        }

        // Nếu mã mặt hàng chưa có, thêm mới
        Item newItem = new Item(); // Tạo item mới với số lượng 1
        newItem.orderCode = itemCode;
        newItem.batchId = batchId;
        newItem.quantity = 1;
        newItem.dateTime = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(new Date());
        new Thread(new Runnable() {
            @Override
            public void run() {
                // Thêm item vào database

                long insertedtemId = db.inventoryDao().insertItem(newItem);
                if (insertedtemId >= 0) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            newItem.id = (int) insertedtemId;
                            adapter.addItem(newItem);
//                            itemList.add(newItem);
//                            adapter.notifyItemInserted(itemList.size() - 1); // Thêm item vào cuối danh sách
                            recyclerView.scrollToPosition(0); // Cuộn lên đầu danh sách

                        }
                    });
                }
            }
        }).start();
    }
    private void loadItemList() {
        new Thread(() -> {

            itemList = db.inventoryDao().getItemsByBatchId(batchId);
            runOnUiThread(() -> {
                adapter = new ItemAdapter(itemList, ItemActivity.this);
                recyclerView.setAdapter(adapter);
                recyclerView.setLayoutManager(new LinearLayoutManager(this));
            });
        }).start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CommonInstance.EDIT_ITEM_REQUEST &&
                resultCode == RESULT_OK) {
            Item updatedItem = (Item) data.getSerializableExtra("UPDATED_ITEM"); // Nhận item đã cập nhật
            adapter.updateItem(updatedItem); // Cập nhật danh sách trong adapter
            recyclerView.scrollToPosition(0); // Cuộn lên đầu danh sách
        }
        if (requestCode == CommonInstance.NEW_ITEM_REQUEST && resultCode == RESULT_OK) {
            Item newItem = (Item) data.getSerializableExtra("NEW_ITEM");
            adapter.addItem(newItem); // Thêm item mới vào adapter
            recyclerView.scrollToPosition(0); // Cuộn lên đầu danh sách
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Release all the EMDK resources
        if (emdkManager != null) {
            emdkManager.release();
            emdkManager= null;
        }
    }


}
