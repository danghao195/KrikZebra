package com.krikapp.krikzebra.adapter;


import static android.app.Activity.RESULT_OK;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.krikapp.krikzebra.R;
import com.krikapp.krikzebra.activities.CommonInstance;
import com.krikapp.krikzebra.activities.EditItemActivity;
import com.krikapp.krikzebra.model.Item;

import java.util.List;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> {
    private List<Item> itemList;
    private Context context;

    public ItemAdapter(List<Item> itemList, Context context) {
        this.itemList = itemList;
        this.context = context;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_item, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        Item item = itemList.get(position);
        holder.orderCode.setText(item.orderCode);
        holder.quantity.setText(String.valueOf(item.quantity));
        holder.dateTime.setText(item.dateTime);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, EditItemActivity.class);
            intent.putExtra("itemId", item.id);
            intent.putExtra("batchId", item.batchId);
            ((Activity)context).startActivityForResult(intent, CommonInstance.EDIT_ITEM_REQUEST);
        });
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView orderCode, quantity, dateTime;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            orderCode = itemView.findViewById(R.id.textOrderCode);
            quantity = itemView.findViewById(R.id.textQuantity);
            dateTime = itemView.findViewById(R.id.textDateTime);
        }
    }
    public void updateItem(Item updatedItem) {
        int position =-1;
        for(Item item : itemList){
            if(item.id ==updatedItem.id){
                position = itemList.indexOf(item);
                itemList.remove(position);
                notifyItemRemoved(position);
                break;
            }
        }
            itemList.add(0, updatedItem); // Nếu không tìm thấy, thêm mới ở đầu
            notifyItemInserted(0); // Thông báo cho adapter

    }

    public void addItem(Item newItem) {
        itemList.add(0, newItem); // Thêm item mới ở đầu danh sách
        notifyItemInserted(0); // Thông báo cho adapter
    }
}
