package com.krikapp.krikzebra.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class InventoryBatch {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String name;
    public String creator;
    public String createdDate;
}
