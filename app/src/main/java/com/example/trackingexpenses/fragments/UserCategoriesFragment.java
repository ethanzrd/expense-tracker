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
import com.example.trackingexpenses.adapters.CategoryAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class UserCategoriesFragment extends Fragment {

    private static final String ARG_USER_ID = "userId";

    private String userId;
    private TabLayout tabLayout;
    private RecyclerView categoriesRecyclerView;
    private ProgressBar progressBar;
    
    private CategoryAdapter categoryAdapter;
    private String currentType = "income"; // Default to income tab
    private List<String> incomeCategories = new ArrayList<>();
    private List<String> expenseCategories = new ArrayList<>();

    public UserCategoriesFragment() {
        // Required empty public constructor
    }

    public static UserCategoriesFragment newInstance(String userId) {
        UserCategoriesFragment fragment = new UserCategoriesFragment();
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
        return inflater.inflate(R.layout.fragment_user_categories, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialize UI components
        tabLayout = view.findViewById(R.id.tabLayout);
        categoriesRecyclerView = view.findViewById(R.id.categoriesRecyclerView);
        progressBar = view.findViewById(R.id.progressBar);
        
        // Setup RecyclerView
        categoriesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        categoryAdapter = new CategoryAdapter(getContext());
        
        // Set readonly adapter - no edit/delete for admin view
        categoryAdapter.setCategoryActionListener(new CategoryAdapter.CategoryActionListener() {
            @Override
            public void onCategoryEdit(String oldCategory, String newCategory, int position) {
                // Not allowed for admin view
                Toast.makeText(getContext(), "אין אפשרות לערוך כמנהל", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCategoryDelete(String category, int position) {
                // Not allowed for admin view
                Toast.makeText(getContext(), "אין אפשרות למחוק כמנהל", Toast.LENGTH_SHORT).show();
            }
        });
        
        categoriesRecyclerView.setAdapter(categoryAdapter);
        
        // Load categories
        loadUserCategories();
        
        // Setup tab selection listener
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    // Income tab
                    currentType = "income";
                    categoryAdapter.setCategories(incomeCategories);
                } else {
                    // Expense tab
                    currentType = "expense";
                    categoryAdapter.setCategories(expenseCategories);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // Not needed
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // Not needed
            }
        });
    }
    
    private void loadUserCategories() {
        if (userId == null || getContext() == null) {
            return;
        }
        
        progressBar.setVisibility(View.VISIBLE);
        
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("categories").child(userId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        // Clear current lists
                        incomeCategories.clear();
                        expenseCategories.clear();
                        
                        if (snapshot.exists()) {
                            // Load income categories
                            if (snapshot.child("income").exists()) {
                                for (DataSnapshot categorySnapshot : snapshot.child("income").getChildren()) {
                                    String category = categorySnapshot.getValue(String.class);
                                    if (category != null) {
                                        incomeCategories.add(category);
                                    }
                                }
                            }
                            
                            // Load expense categories
                            if (snapshot.child("expense").exists()) {
                                for (DataSnapshot categorySnapshot : snapshot.child("expense").getChildren()) {
                                    String category = categorySnapshot.getValue(String.class);
                                    if (category != null) {
                                        expenseCategories.add(category);
                                    }
                                }
                            }
                            
                            // Update adapter with current type's categories
                            if ("income".equals(currentType)) {
                                categoryAdapter.setCategories(incomeCategories);
                            } else {
                                categoryAdapter.setCategories(expenseCategories);
                            }
                        }
                        
                        progressBar.setVisibility(View.GONE);
                    }
                    
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        progressBar.setVisibility(View.GONE);
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "שגיאה בטעינת קטגוריות", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
