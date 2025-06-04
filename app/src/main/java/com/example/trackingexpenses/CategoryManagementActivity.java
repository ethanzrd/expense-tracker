package com.example.trackingexpenses;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trackingexpenses.adapters.CategoryAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class CategoryManagementActivity extends AppCompatActivity implements CategoryAdapter.CategoryActionListener {

    private TabLayout tabLayout;
    private RecyclerView categoriesRecyclerView;
    private EditText newCategoryEditText;
    private Button addCategoryButton;
    private ProgressBar progressBar;
    
    private CategoryAdapter categoryAdapter;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private String currentUserId;
    
    private String currentType = "income"; // Default to income tab
    private List<String> incomeCategories = new ArrayList<>();
    private List<String> expenseCategories = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_category_management);
        
        // Apply WindowInsets for safe area display
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        
        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        currentUserId = mAuth.getCurrentUser().getUid();
        
        // Initialize UI components
        tabLayout = findViewById(R.id.tabLayout);
        categoriesRecyclerView = findViewById(R.id.categoriesRecyclerView);
        newCategoryEditText = findViewById(R.id.newCategoryEditText);
        addCategoryButton = findViewById(R.id.addCategoryButton);
        progressBar = findViewById(R.id.progressBar);
        
        // Setup RecyclerView
        categoriesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        categoryAdapter = new CategoryAdapter(this);
        categoryAdapter.setCategoryActionListener(this);
        categoriesRecyclerView.setAdapter(categoryAdapter);
        
        // Load categories
        loadCategories();
        
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
        
        // Setup add button
        addCategoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addCategory();
            }
        });
    }
    
    private void loadCategories() {
        progressBar.setVisibility(View.VISIBLE);
        
        mDatabase.child("categories").child(currentUserId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        progressBar.setVisibility(View.GONE);
                        
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
                    }
                    
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(CategoryManagementActivity.this, "שגיאה בטעינת קטגוריות", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    
    private void addCategory() {
        String newCategory = newCategoryEditText.getText().toString().trim();
        
        if (TextUtils.isEmpty(newCategory)) {
            newCategoryEditText.setError("נא להזין שם קטגוריה");
            return;
        }
        
        // Check if category already exists
        List<String> currentCategories = "income".equals(currentType) ? incomeCategories : expenseCategories;
        if (currentCategories.contains(newCategory)) {
            newCategoryEditText.setError("קטגוריה זו כבר קיימת");
            return;
        }
        
        // Add to Firebase
        progressBar.setVisibility(View.VISIBLE);
        addCategoryButton.setEnabled(false);
        
        // Add to current list
        currentCategories.add(newCategory);
        
        // Create a new list to save to Firebase
        mDatabase.child("categories").child(currentUserId).child(currentType).setValue(currentCategories)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        progressBar.setVisibility(View.GONE);
                        addCategoryButton.setEnabled(true);
                        
                        if (task.isSuccessful()) {
                            Toast.makeText(CategoryManagementActivity.this, "קטגוריה נוספה בהצלחה", Toast.LENGTH_SHORT).show();
                            newCategoryEditText.setText(""); // Clear input field
                        } else {
                            Toast.makeText(CategoryManagementActivity.this, "שגיאה בהוספת קטגוריה", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    public void onCategoryEdit(String oldCategory, String newCategory, int position) {
        if (TextUtils.isEmpty(newCategory)) {
            Toast.makeText(this, "שם הקטגוריה לא יכול להיות ריק", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Check if new category name already exists
        List<String> currentCategories = "income".equals(currentType) ? incomeCategories : expenseCategories;
        if (currentCategories.contains(newCategory) && !oldCategory.equals(newCategory)) {
            Toast.makeText(this, "קטגוריה זו כבר קיימת", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Update category in list
        currentCategories.remove(oldCategory);
        currentCategories.add(position, newCategory);
        
        // Update in Firebase
        progressBar.setVisibility(View.VISIBLE);
        
        mDatabase.child("categories").child(currentUserId).child(currentType).setValue(currentCategories)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        progressBar.setVisibility(View.GONE);
                        
                        if (task.isSuccessful()) {
                            Toast.makeText(CategoryManagementActivity.this, "קטגוריה עודכנה בהצלחה", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(CategoryManagementActivity.this, "שגיאה בעדכון הקטגוריה", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    public void onCategoryDelete(String category, int position) {
        // Remove category from list
        List<String> currentCategories = "income".equals(currentType) ? incomeCategories : expenseCategories;
        currentCategories.remove(category);

        // Update in Firebase
        progressBar.setVisibility(View.VISIBLE);

        mDatabase.child("categories").child(currentUserId).child(currentType).setValue(currentCategories)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        progressBar.setVisibility(View.GONE);

                        if (task.isSuccessful()) {
                            Toast.makeText(CategoryManagementActivity.this, "קטגוריה נמחקה בהצלחה", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(CategoryManagementActivity.this, "שגיאה במחיקת הקטגוריה", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
