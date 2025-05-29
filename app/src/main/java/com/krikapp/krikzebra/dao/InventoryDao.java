package com.krikapp.krikzebra.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.krikapp.krikzebra.model.ExportItem;
import com.krikapp.krikzebra.model.InventoryBatch;
import com.krikapp.krikzebra.model.Item;
import com.krikapp.krikzebra.model.ItemWithBatch;

import java.util.List;

@Dao
public interface InventoryDao {
    @Insert
    long insertBatch(InventoryBatch batch);

    @Delete
    void deleteBatch(InventoryBatch batch);

    @Insert
    long insertItem(Item item);

    @Delete
    void deleteItem(Item item);

    @Update
    void updateItem(Item item);

    @Query("SELECT * FROM InventoryBatch ORDER BY createdDate DESC")
    List<InventoryBatch> getAllBatches();

    @Query("SELECT * FROM Item WHERE batchId = :batchId ORDER BY dateTime DESC")
    List<Item> getItemsByBatchId(int batchId);
    @Query("SELECT orderCode, SUM(quantity) as sumQuantity FROM Item WHERE batchId IN (:batchId) GROUP BY orderCode")
    List<ExportItem> getItemsByBatchIds(List<Long> batchId);
    @Query("SELECT * FROM Item ORDER BY dateTime DESC")
    List<Item> getaLLItems();

    @Query("SELECT * FROM Item WHERE id = :itemId and batchId= :batchId")
    Item getItemById(int itemId, int batchId);

    @Query("SELECT  items.orderCode, SUM(quantity) as sumQuantity, b.name AS batchName, max(items.dateTime) AS timeMax " +
            "FROM Item AS items " +
            "JOIN InventoryBatch b ON items.batchId = b.id " +
            "WHERE items.batchId IN (:batchId) " +
            "Group by items.orderCode,batchName " +
            "ORDER BY timeMax DESC")
    List<ExportItem> getItemsWithBatch(List<Long> batchId);
}
