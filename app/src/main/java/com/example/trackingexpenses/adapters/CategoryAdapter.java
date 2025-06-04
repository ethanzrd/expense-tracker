package com.example.trackingexpenses.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trackingexpenses.R;

import java.util.ArrayList;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private Context context;
    private List<String> categories;
    private CategoryActionListener listener;
    
    public CategoryAdapter(Context context) {
        this.context = context;
        this.categories = new ArrayList<>();
    }
    
    public void setCategories(List<String> categories) {
        this.categories = categories;
        notifyDataSetChanged();
    }
    
    public void setCategoryActionListener(CategoryActionListener listener) {
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        final String category = categories.get(position);
        holder.categoryNameTextView.setText(category);
        
        // Edit button click
        holder.editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditDialog(category, position);
            }
        });

        // Delete button click
        holder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteConfirmation(category, position);
            }
        });
    }
    
    @Override
    public int getItemCount() {
        return categories.size();
    }
    
    private void showEditDialog(final String currentName, final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("עריכת קטגוריה");
        
        final EditText input = new EditText(context);
        input.setText(currentName);
        builder.setView(input);
        
        builder.setPositiveButton("שמור", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newName = input.getText().toString().trim();
                if (!newName.isEmpty() && listener != null) {
                    listener.onCategoryEdit(currentName, newName, position);
                }
            }
        });
        
        builder.setNegativeButton("ביטול", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        
        builder.show();
    }

    private void showDeleteConfirmation(final String category, final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("מחיקת קטגוריה");
        builder.setMessage("האם אתה בטוח שברצונך למחוק קטגוריה זו?");

        builder.setPositiveButton("כן", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (listener != null) {
                    listener.onCategoryDelete(category, position);
                }
            }
        });

        builder.setNegativeButton("לא", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }
    
    public class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView categoryNameTextView;
        ImageButton editButton;
        ImageButton deleteButton;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryNameTextView = itemView.findViewById(R.id.categoryNameTextView);
            editButton = itemView.findViewById(R.id.editCategoryButton);
            deleteButton = itemView.findViewById(R.id.deleteCategoryButton);
        }
    }
    
    public interface CategoryActionListener {
        void onCategoryEdit(String oldCategory, String newCategory, int position);
        void onCategoryDelete(String category, int position);
    }
}
