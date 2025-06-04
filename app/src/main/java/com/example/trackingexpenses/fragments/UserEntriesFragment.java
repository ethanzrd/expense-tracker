package com.example.trackingexpenses.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trackingexpenses.R;
import com.example.trackingexpenses.adapters.EntryAdapter;
import com.example.trackingexpenses.models.Entry;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class UserEntriesFragment extends Fragment {

    private static final String ARG_USER_ID = "userId";

    private String userId;
    private RecyclerView entriesRecyclerView;
    private ProgressBar progressBar;
    private EntryAdapter entryAdapter;

    public UserEntriesFragment() {
        // Required empty public constructor
    }

    public static UserEntriesFragment newInstance(String userId) {
        UserEntriesFragment fragment = new UserEntriesFragment();
        Bundle args = new Bundle();
        args.putString(ARG_USER_ID, userId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userId = getArguments().getString(ARG_USER_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_user_entries, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialize UI components
        entriesRecyclerView = view.findViewById(R.id.entriesRecyclerView);
        progressBar = view.findViewById(R.id.progressBar);
        
        // Setup RecyclerView
        entriesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        entryAdapter = new EntryAdapter(getContext());

        // Set entry adapter with no edit/delete capabilities (admin view-only)
        entryAdapter.setOnEntryClickListener(new EntryAdapter.OnEntryClickListener() {
            @Override
            public void onEntryClick(Entry entry) {
                // View only - show details in toast
                Toast.makeText(getContext(), entry.getCategory() + ": " + entry.getAmount() + " ₪", Toast.LENGTH_SHORT).show();
            }
        });
        
        entriesRecyclerView.setAdapter(entryAdapter);
        
        // Load entries
        loadUserEntries();
    }
    
    private void loadUserEntries() {
        if (userId == null || getContext() == null) {
            return;
        }
        
        progressBar.setVisibility(View.VISIBLE);
        
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("entries").child(userId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<Entry> entryList = new ArrayList<>();
                        
                        for (DataSnapshot entrySnapshot : snapshot.getChildren()) {
                            String entryId = entrySnapshot.getKey();
                            String type = entrySnapshot.child("type").getValue(String.class);
                            String category = entrySnapshot.child("category").getValue(String.class);
                            String date = entrySnapshot.child("date").getValue(String.class);
                            String note = entrySnapshot.child("note").getValue(String.class);
                            
                            double amount = 0;
                            if (entrySnapshot.child("amount").exists()) {
                                Object amountObj = entrySnapshot.child("amount").getValue();
                                if (amountObj instanceof Long) {
                                    amount = ((Long) amountObj).doubleValue();
                                } else if (amountObj instanceof Double) {
                                    amount = (Double) amountObj;
                                }
                            }
                            
                            Entry entry = new Entry(entryId, type, amount, category, date, note);
                            entryList.add(entry);
                        }
                        
                        // Sort entries by date (newest first)
                        entryList.sort((e1, e2) -> e2.getDate().compareTo(e1.getDate()));
                        
                        // Update adapter
                        entryAdapter.setEntries(entryList);
                        
                        progressBar.setVisibility(View.GONE);
                    }
                    
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        progressBar.setVisibility(View.GONE);
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "שגיאה בטעינת רשומות", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
