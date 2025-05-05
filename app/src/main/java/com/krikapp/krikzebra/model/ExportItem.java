package com.krikapp.krikzebra.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity
public class ExportItem implements Serializable {
    public String orderCode;
    public int sumQuantity;
}
