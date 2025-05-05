package com.krikapp.krikzebra.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.krikapp.krikzebra.R;
import com.krikapp.krikzebra.activities.BatchListActivity;
import com.krikapp.krikzebra.activities.ItemActivity;
import com.krikapp.krikzebra.model.InventoryBatch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;public class BatchAdapter extends RecyclerView.Adapter<BatchAdapter.BatchViewHolder> {
    private List<InventoryBatch> batchList;
    private Context context;
    private List<Boolean> selectedItems;

    public BatchAdapter(List<InventoryBatch> batchList, Context context) {
        this.batchList = batchList;
        this.context = context;
        this.selectedItems = new ArrayList<>(Collections.nCopies(batchList.size(), false));
    }

    @NonNull
    @Override
    public BatchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_batch, parent, false);
        return new BatchViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BatchViewHolder holder, int position) {
        InventoryBatch batch = batchList.get(position);
        holder.batchName.setText(batch.name);
        holder.creator.setText(batch.creator);
        holder.createdDate.setText(batch.createdDate);
        holder.checkboxSelect.setChecked(selectedItems.get(position));

        holder.checkboxSelect.setOnClickListener(v -> {
            selectedItems.set(position, holder.checkboxSelect.isChecked());
            ((BatchListActivity) context).updateButtonVisibility(); // Gọi phương thức cập nhật
        });

       // holder.itemView.setOnClickListener(v -> {
       //     holder.checkboxSelect.toggle();
       //     selectedItems.set(position, holder.checkboxSelect.isChecked());
       //     ((BatchListActivity) context).updateButtonVisibility(); // Gọi phương thức cập nhật
       // });
        holder.itemView.setOnClickListener(v -> {
            // Chuyển sang màn hình danh sách mặt hàng của đợt kiểm kho
            Intent intent = new Intent(context, ItemActivity.class);
            intent.putExtra("batchId", batch.id); // Truyền ID của đợt kiểm kho
            context.startActivity(intent);
        });

    }

    public List<InventoryBatch> getSelectedBatches() {
        List<InventoryBatch> selectedBatches = new ArrayList<>();
        for (int i = 0; i < selectedItems.size(); i++) {
            if (selectedItems.get(i)) {
                selectedBatches.add(batchList.get(i));
            }
        }
        return selectedBatches;
    }

    @Override
    public int getItemCount() {
        return batchList.size();
    }

    public static class BatchViewHolder extends RecyclerView.ViewHolder {
        TextView batchName, creator, createdDate;
        CheckBox checkboxSelect;

        public BatchViewHolder(@NonNull View itemView) {
            super(itemView);
            batchName = itemView.findViewById(R.id.textBatchName);
            creator = itemView.findViewById(R.id.textCreator);
            createdDate = itemView.findViewById(R.id.textCreatedDate);
            checkboxSelect = itemView.findViewById(R.id.checkboxSelect);
        }
    }
}