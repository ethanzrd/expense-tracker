package com.example.trackingexpenses;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trackingexpenses.adapters.CategorySummaryAdapter;
import com.example.trackingexpenses.models.CategorySummary;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MonthlySummaryActivity extends AppCompatActivity {

    private TextView monthTextView, incomeValueTextView, expenseValueTextView, balanceValueTextView;
    private TabLayout tabLayout;
    private RecyclerView categoryBreakdownRecyclerView;
    private ProgressBar progressBar;
    private View prevMonthButton, nextMonthButton;
    
    private CategorySummaryAdapter incomeSummaryAdapter;
    private CategorySummaryAdapter expenseSummaryAdapter;
    
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private String currentUserId;
    
    private Calendar selectedDate;
    private SimpleDateFormat monthFormat;
    private SimpleDateFormat yearFormat;
    private String currentMonth;
    private String currentYear;
    
    private double totalIncome = 0;
    private double totalExpense = 0;
    
    private Map<String, Double> incomeCategorySums = new HashMap<>();
    private Map<String, Double> expenseCategorySums = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_monthly_summary);
        
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
        monthTextView = findViewById(R.id.monthTextView);
        incomeValueTextView = findViewById(R.id.incomeValueTextView);
        expenseValueTextView = findViewById(R.id.expenseValueTextView);
        balanceValueTextView = findViewById(R.id.balanceValueTextView);
        tabLayout = findViewById(R.id.tabLayout);
        categoryBreakdownRecyclerView = findViewById(R.id.categoryBreakdownRecyclerView);
        progressBar = findViewById(R.id.progressBar);
        prevMonthButton = findViewById(R.id.prevMonthButton);
        nextMonthButton = findViewById(R.id.nextMonthButton);
        
        // Setup RecyclerView
        categoryBreakdownRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        // Initialize adapters
        incomeSummaryAdapter = new CategorySummaryAdapter(this, true);
        expenseSummaryAdapter = new CategorySummaryAdapter(this, false);
        
        // Set default adapter (income tab is default)
        categoryBreakdownRecyclerView.setAdapter(incomeSummaryAdapter);
        
        // Initialize date formats
        monthFormat = new SimpleDateFormat("MMMM", new Locale("he"));
        yearFormat = new SimpleDateFormat("yyyy", Locale.getDefault());
        
        // Initialize with current month
        selectedDate = Calendar.getInstance();
        updateMonthDisplay();
        
        // Load data
        loadMonthlySummary();
        
        // Setup month navigation buttons
        prevMonthButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Move to previous month
                selectedDate.add(Calendar.MONTH, -1);
                updateMonthDisplay();
                loadMonthlySummary();
            }
        });
        
        nextMonthButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Move to next month
                selectedDate.add(Calendar.MONTH, 1);
                updateMonthDisplay();
                loadMonthlySummary();
            }
        });
        
        // Setup tab selection listener
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    // Income tab
                    categoryBreakdownRecyclerView.setAdapter(incomeSummaryAdapter);
                } else {
                    // Expense tab
                    categoryBreakdownRecyclerView.setAdapter(expenseSummaryAdapter);
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
    
    /**
     * Updates the month display text
     */
    private void updateMonthDisplay() {
        currentMonth = monthFormat.format(selectedDate.getTime());
        currentYear = yearFormat.format(selectedDate.getTime());
        monthTextView.setText(currentMonth + " " + currentYear);
    }
    
    /**
     * Loads financial summary for the selected month
     */
    private void loadMonthlySummary() {
        progressBar.setVisibility(View.VISIBLE);
        
        // Reset counters
        totalIncome = 0;
        totalExpense = 0;
        incomeCategorySums.clear();
        expenseCategorySums.clear();
        
        int year = selectedDate.get(Calendar.YEAR);
        int month = selectedDate.get(Calendar.MONTH) + 1; // Calendar months are 0-based
        
        // Format for date filtering
        SimpleDateFormat dbDateFormat = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
        String monthYear = dbDateFormat.format(selectedDate.getTime());
        
        String startDate = monthYear + "-01";
        String endDate = monthYear + "-31";
        
        mDatabase.child("entries").child(currentUserId).orderByChild("date")
                .startAt(startDate).endAt(endDate)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        // Reset counters
                        totalIncome = 0;
                        totalExpense = 0;
                        incomeCategorySums.clear();
                        expenseCategorySums.clear();
                        
                        // Process each entry
                        for (DataSnapshot entrySnapshot : snapshot.getChildren()) {
                            String type = entrySnapshot.child("type").getValue(String.class);
                            String category = entrySnapshot.child("category").getValue(String.class);
                            
                            double amount = 0;
                            if (entrySnapshot.child("amount").exists()) {
                                Object amountObj = entrySnapshot.child("amount").getValue();
                                if (amountObj instanceof Long) {
                                    amount = ((Long) amountObj).doubleValue();
                                } else if (amountObj instanceof Double) {
                                    amount = (Double) amountObj;
                                }
                            }
                            
                            if ("income".equals(type)) {
                                totalIncome += amount;
                                
                                // Add to category sum
                                if (category != null) {
                                    double currentSum = incomeCategorySums.getOrDefault(category, 0.0);
                                    incomeCategorySums.put(category, currentSum + amount);
                                }
                            } else if ("expense".equals(type)) {
                                totalExpense += amount;
                                
                                // Add to category sum
                                if (category != null) {
                                    double currentSum = expenseCategorySums.getOrDefault(category, 0.0);
                                    expenseCategorySums.put(category, currentSum + amount);
                                }
                            }
                        }
                        
                        // Update UI with totals
                        updateSummaryUI();
                        
                        // Create category summary lists
                        updateCategorySummaries();
                        
                        progressBar.setVisibility(View.GONE);
                    }
                    
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(MonthlySummaryActivity.this, "שגיאה בטעינת נתונים", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    
    private void updateSummaryUI() {
        // Update total income, expense, and balance values
        incomeValueTextView.setText(String.format(Locale.getDefault(), "%.2f ₪", totalIncome));
        expenseValueTextView.setText(String.format(Locale.getDefault(), "%.2f ₪", totalExpense));
        
        double balance = totalIncome - totalExpense;
        balanceValueTextView.setText(String.format(Locale.getDefault(), "%.2f ₪", balance));
        
        // Set balance color
        if (balance < 0) {
            balanceValueTextView.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        } else {
            balanceValueTextView.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        }
    }
    
    private void updateCategorySummaries() {
        // Create income category summaries
        List<CategorySummary> incomeSummaries = new ArrayList<>();
        for (Map.Entry<String, Double> entry : incomeCategorySums.entrySet()) {
            String category = entry.getKey();
            double amount = entry.getValue();
            float percentage = totalIncome > 0 ? (float) ((amount / totalIncome) * 100) : 0;
            
            incomeSummaries.add(new CategorySummary(category, amount, percentage));
        }
        
        // Create expense category summaries
        List<CategorySummary> expenseSummaries = new ArrayList<>();
        for (Map.Entry<String, Double> entry : expenseCategorySums.entrySet()) {
            String category = entry.getKey();
            double amount = entry.getValue();
            float percentage = totalExpense > 0 ? (float) ((amount / totalExpense) * 100) : 0;
            
            expenseSummaries.add(new CategorySummary(category, amount, percentage));
        }
        
        // Sort both lists by amount (descending)
        incomeSummaries.sort((a, b) -> Double.compare(b.getAmount(), a.getAmount()));
        expenseSummaries.sort((a, b) -> Double.compare(b.getAmount(), a.getAmount()));
        
        // Update adapters
        incomeSummaryAdapter.setCategorySummaries(incomeSummaries);
        expenseSummaryAdapter.setCategorySummaries(expenseSummaries);
    }
}
