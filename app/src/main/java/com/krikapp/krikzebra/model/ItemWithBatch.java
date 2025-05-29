package com.krikapp.krikzebra.model;

public class ItemWithBatch {
    public int id;
    public String batchName; // Foreign key
    public String orderCode;
    public int quantity;
    public String dateTime;

    public ItemWithBatch() {
    }

    public ItemWithBatch(int id, String batchName, String orderCode, int quantity, String dateTime) {
        this.id = id;
        this.batchName = batchName;
        this.orderCode = orderCode;
        this.quantity = quantity;
        this.dateTime = dateTime;
    }
}
