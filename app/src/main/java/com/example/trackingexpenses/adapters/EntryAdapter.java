package com.example.trackingexpenses.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trackingexpenses.R;
import com.example.trackingexpenses.models.Entry;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class EntryAdapter extends RecyclerView.Adapter<EntryAdapter.EntryViewHolder> {

    private Context context;
    private List<Entry> entries;
    private OnEntryClickListener listener;

    public EntryAdapter(Context context) {
        this.context = context;
        this.entries = new ArrayList<>();
    }

    public void setEntries(List<Entry> entries) {
        this.entries = entries;
        notifyDataSetChanged();
    }

    public void setOnEntryClickListener(OnEntryClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public EntryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_entry, parent, false);
        return new EntryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EntryViewHolder holder, int position) {
        Entry entry = entries.get(position);
        
        // Set type badge
        if ("income".equals(entry.getType())) {
            holder.typeTextView.setText("הכנסה");
            holder.typeTextView.setBackgroundColor(Color.parseColor("#4CAF50")); // Green
            holder.amountTextView.setTextColor(Color.parseColor("#4CAF50"));
            holder.amountTextView.setText(String.format(Locale.getDefault(), "₪%.2f", entry.getAmount()));
        } else {
            holder.typeTextView.setText("הוצאה");
            holder.typeTextView.setBackgroundColor(Color.parseColor("#F44336")); // Red
            holder.amountTextView.setTextColor(Color.parseColor("#F44336"));
            holder.amountTextView.setText(String.format(Locale.getDefault(), "₪%.2f", entry.getAmount()));
        }
        
        // Set other fields
        holder.categoryTextView.setText(entry.getCategory());
        holder.noteTextView.setText(entry.getNote());
        holder.dateTextView.setText(entry.getDate());

        // Set click listener
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onEntryClick(entry);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return entries.size();
    }

    public class EntryViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView typeTextView, amountTextView, categoryTextView, noteTextView, dateTextView;

        public EntryViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (CardView) itemView;
            typeTextView = itemView.findViewById(R.id.entryTypeTextView);
            amountTextView = itemView.findViewById(R.id.entryAmountTextView);
            categoryTextView = itemView.findViewById(R.id.entryCategoryTextView);
            noteTextView = itemView.findViewById(R.id.entryNoteTextView);
            dateTextView = itemView.findViewById(R.id.entryDateTextView);
        }
    }

    public interface OnEntryClickListener {
        void onEntryClick(Entry entry);
    }
}
