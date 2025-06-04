package com.example.trackingexpenses;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
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

import com.example.trackingexpenses.adapters.EntryAdapter;
import com.example.trackingexpenses.models.Entry;
import android.view.Menu;
import android.view.MenuItem;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextView userNameTextView, monthTextView;
    private TextView incomeValueTextView, expenseValueTextView, balanceValueTextView;
    private RecyclerView entriesRecyclerView;
    private FloatingActionButton addEntryButton;
    
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private String currentUserId;
    private String currentMonth;
    private String currentYear;
    
    private ImageButton prevMonthButton;
    private ImageButton nextMonthButton;
    
    private Calendar selectedDate;
    private SimpleDateFormat monthFormat;
    private SimpleDateFormat yearFormat;
    
    private double totalIncome = 0;
    private double totalExpense = 0;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        
        // Add options menu
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        
        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        
        // Initialize views
        userNameTextView = findViewById(R.id.userNameTextView);
        monthTextView = findViewById(R.id.monthTextView);
        incomeValueTextView = findViewById(R.id.incomeValueTextView);
        expenseValueTextView = findViewById(R.id.expenseValueTextView);
        balanceValueTextView = findViewById(R.id.balanceValueTextView);
        entriesRecyclerView = findViewById(R.id.entriesRecyclerView);
        addEntryButton = findViewById(R.id.addEntryButton);
        prevMonthButton = findViewById(R.id.prevMonthButton);
        nextMonthButton = findViewById(R.id.nextMonthButton);
        
        // Setup RecyclerView
        entriesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        // Initialize date formats
        monthFormat = new SimpleDateFormat("MMMM", new Locale("he"));
        yearFormat = new SimpleDateFormat("yyyy", Locale.getDefault());
        
        // Initialize with current month
        selectedDate = Calendar.getInstance();
        updateMonthDisplay();
        
        // Setup month navigation buttons
        prevMonthButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Move to previous month
                selectedDate.add(Calendar.MONTH, -1);
                updateMonthDisplay();
                loadEntriesData();
            }
        });
        
        nextMonthButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Move to next month
                selectedDate.add(Calendar.MONTH, 1);
                updateMonthDisplay();
                loadEntriesData();
            }
        });
        
        // Add button click listener
        addEntryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Navigate to add entry screen
                startActivity(new Intent(MainActivity.this, AddEntryActivity.class));
            }
        });
    }
    
    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            // If user is not logged in, redirect to login activity
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
            return;
        }
        
        currentUserId = currentUser.getUid();
        loadUserData();
        loadEntriesData();
    }
    
    private void loadUserData() {
        mDatabase.child("users").child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String userName = snapshot.child("name").getValue(String.class);
                    boolean isAdmin = false;
                    if (snapshot.child("isAdmin").exists()) {
                        isAdmin = snapshot.child("isAdmin").getValue(Boolean.class);
                    }
                    
                    userNameTextView.setText("שלום, " + userName);
                }
            }
            
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "שגיאה בטעינת פרטי משתמש", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void updateMonthDisplay() {
        currentMonth = monthFormat.format(selectedDate.getTime());
        currentYear = yearFormat.format(selectedDate.getTime());
        monthTextView.setText("סיכום חודשי: " + currentMonth + " " + currentYear);
    }
    
    private void loadEntriesData() {
        // Reset totals
        totalIncome = 0;
        totalExpense = 0;
        
        EntryAdapter entryAdapter = new EntryAdapter(this);
        entriesRecyclerView.setAdapter(entryAdapter);

        // Set click listeners
        entryAdapter.setOnEntryClickListener(new EntryAdapter.OnEntryClickListener() {
            @Override
            public void onEntryClick(Entry entry) {
                // Show options dialog for edit/delete
                final CharSequence[] options = {"עריכה", "מחיקה"};

                androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(MainActivity.this);
                builder.setTitle("בחר פעולה");
                builder.setItems(options, new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(android.content.DialogInterface dialog, int which) {
                        if (which == 0) {
                            // Edit option
                            Intent editIntent = new Intent(MainActivity.this, EditEntryActivity.class);
                            editIntent.putExtra("ENTRY_ID", entry.getEntryId());
                            startActivity(editIntent);
                        } else if (which == 1) {
                            // Delete option
                            deleteEntry(entry.getEntryId());
                        }
                    }
                });
                builder.show();
            }
        });
        
        // Load entries and calculate totals
        
        // Query to get entries for the current user and selected month
        DatabaseReference entriesRef = mDatabase.child("entries").child(currentUserId);
        
        // Format for date filtering
        Calendar startOfMonth = (Calendar) selectedDate.clone();
        startOfMonth.set(Calendar.DAY_OF_MONTH, 1);
        startOfMonth.set(Calendar.HOUR_OF_DAY, 0);
        startOfMonth.set(Calendar.MINUTE, 0);
        startOfMonth.set(Calendar.SECOND, 0);
        
        Calendar endOfMonth = (Calendar) selectedDate.clone();
        endOfMonth.set(Calendar.DAY_OF_MONTH, endOfMonth.getActualMaximum(Calendar.DAY_OF_MONTH));
        endOfMonth.set(Calendar.HOUR_OF_DAY, 23);
        endOfMonth.set(Calendar.MINUTE, 59);
        endOfMonth.set(Calendar.SECOND, 59);
        
        // Date format for Firebase queries
        SimpleDateFormat dbDateFormat = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
        String monthYear = dbDateFormat.format(selectedDate.getTime());
        
        entriesRef.orderByChild("date").startAt(monthYear).endAt(monthYear + "-31").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                totalIncome = 0;
                totalExpense = 0;
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
                    
                    if ("income".equals(type)) {
                        totalIncome += amount;
                    } else if ("expense".equals(type)) {
                        totalExpense += amount;
                    }
                }
                
                // Update adapter with entries
                entryAdapter.setEntries(entryList);
                
                // Update UI
                incomeValueTextView.setText(String.format(Locale.getDefault(), "%.2f ₪", totalIncome));
                expenseValueTextView.setText(String.format(Locale.getDefault(), "%.2f ₪", totalExpense));
                double balance = totalIncome - totalExpense;
                balanceValueTextView.setText(String.format(Locale.getDefault(), "%.2f ₪", balance));
                
                // Set text color for balance based on value
                if (balance < 0) {
                    balanceValueTextView.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                } else {
                    balanceValueTextView.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                }
            }
            
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "שגיאה בטעינת נתונים", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void logout() {
        mAuth.signOut();
        startActivity(new Intent(MainActivity.this, LoginActivity.class));
        finish();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        
        // Show admin panel option only to admin users
        final MenuItem adminMenuItem = menu.findItem(R.id.menu_admin_panel);
        if (adminMenuItem != null) {
            // Check if current user is admin
            DatabaseReference userRef = mDatabase.child("users").child(currentUserId);
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        Boolean isAdmin = snapshot.child("isAdmin").getValue(Boolean.class);
                        adminMenuItem.setVisible(isAdmin != null && isAdmin);
                    } else {
                        adminMenuItem.setVisible(false);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    adminMenuItem.setVisible(false);
                }
            });
        }
        
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.action_categories) {
            // Open category management
            startActivity(new Intent(MainActivity.this, CategoryManagementActivity.class));
            return true;
        } else if (id == R.id.action_monthly_summary) {
            // Open monthly summary
            startActivity(new Intent(MainActivity.this, MonthlySummaryActivity.class));
            return true;
        } else if (id == R.id.menu_logout) {
            logout();
            return true;
        } else if (id == R.id.menu_testing) {
            // Open testing activity
            startActivity(new Intent(MainActivity.this, TestingActivity.class));
            return true;
        } else if (id == R.id.menu_admin_panel) {
            // Open admin panel
            startActivity(new Intent(MainActivity.this, AdminActivity.class));
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }
    
    // Delete entry method
    private void deleteEntry(String entryId) {
        if (entryId == null || entryId.isEmpty()) {
            Toast.makeText(this, "מזהה רשומה לא תקין", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Confirm deletion
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("מחיקת רשומה")
                .setMessage("האם אתה בטוח שברצונך למחוק רשומה זו?")
                .setPositiveButton("כן", new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(android.content.DialogInterface dialog, int which) {
                        // Delete from Firebase
                        mDatabase.child("entries").child(currentUserId).child(entryId).removeValue()
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(MainActivity.this, "הרשומה נמחקה בהצלחה", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(MainActivity.this, "שגיאה במחיקת הרשומה", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    }
                })
                .setNegativeButton("ביטול", null)
                .show();
    }
}