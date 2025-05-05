package com.krikapp.krikzebra.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.krikapp.krikzebra.dao.InventoryDao;
import com.krikapp.krikzebra.model.InventoryBatch;
import com.krikapp.krikzebra.model.Item;

@Database(entities = {InventoryBatch.class, Item.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract InventoryDao inventoryDao();
}
