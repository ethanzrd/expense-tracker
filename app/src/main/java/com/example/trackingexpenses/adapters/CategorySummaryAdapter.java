package com.example.trackingexpenses.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trackingexpenses.R;
import com.example.trackingexpenses.models.CategorySummary;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CategorySummaryAdapter extends RecyclerView.Adapter<CategorySummaryAdapter.CategorySummaryViewHolder> {

    private Context context;
    private List<CategorySummary> categorySummaries;
    private boolean isIncome;
    
    public CategorySummaryAdapter(Context context, boolean isIncome) {
        this.context = context;
        this.categorySummaries = new ArrayList<>();
        this.isIncome = isIncome;
    }
    
    public void setCategorySummaries(List<CategorySummary> categorySummaries) {
        this.categorySummaries = categorySummaries;
        notifyDataSetChanged();
    }
    
    @NonNull
    @Override
    public CategorySummaryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_category_summary, parent, false);
        return new CategorySummaryViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull CategorySummaryViewHolder holder, int position) {
        CategorySummary summary = categorySummaries.get(position);
        
        holder.categoryNameTextView.setText(summary.getCategoryName());
        holder.categoryAmountTextView.setText(String.format(Locale.getDefault(), "%.2f ₪", summary.getAmount()));
        holder.categoryPercentageTextView.setText(String.format(Locale.getDefault(), "%.1f%%", summary.getPercentage()));
        holder.categoryProgressBar.setProgress((int) summary.getPercentage());
        
        // Set color based on income/expense
        int color = isIncome ? 
                context.getResources().getColor(android.R.color.holo_green_dark) : 
                context.getResources().getColor(android.R.color.holo_red_dark);
        
        holder.categoryAmountTextView.setTextColor(color);
        holder.categoryProgressBar.setProgressTintList(android.content.res.ColorStateList.valueOf(color));
    }
    
    @Override
    public int getItemCount() {
        return categorySummaries.size();
    }
    
    public class CategorySummaryViewHolder extends RecyclerView.ViewHolder {
        TextView categoryNameTextView;
        TextView categoryAmountTextView;
        TextView categoryPercentageTextView;
        ProgressBar categoryProgressBar;
        
        public CategorySummaryViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryNameTextView = itemView.findViewById(R.id.categoryNameTextView);
            categoryAmountTextView = itemView.findViewById(R.id.categoryAmountTextView);
            categoryPercentageTextView = itemView.findViewById(R.id.categoryPercentageTextView);
            categoryProgressBar = itemView.findViewById(R.id.categoryProgressBar);
        }
    }
}
