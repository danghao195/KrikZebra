package com.krikapp.krikzebra.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity
public class Item  implements Serializable {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public int batchId; // Foreign key
    public String orderCode;
    public int quantity;
    public String dateTime; // Ngày giờ
}
