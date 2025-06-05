package com.example.trackingexpenses;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class EditEntryActivity extends AppCompatActivity {

    private RadioGroup typeRadioGroup;
    private TextInputEditText amountEditText, dateEditText, noteEditText;
    private Spinner categorySpinner;
    private Button saveButton;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private String currentUserId;
    private String currentType = "expense"; // Default type
    private String entryId;

    private List<String> incomeCategories = new ArrayList<>();
    private List<String> expenseCategories = new ArrayList<>();
    private ArrayAdapter<String> categoryAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_entry);

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
        
        // Get entryId from intent
        entryId = getIntent().getStringExtra("ENTRY_ID");
        if (entryId == null) {
            Toast.makeText(this, "שגיאה: לא נמצאה רשומה לעריכה", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize UI components
        typeRadioGroup = findViewById(R.id.typeRadioGroup);
        amountEditText = findViewById(R.id.amountEditText);
        dateEditText = findViewById(R.id.dateEditText);
        noteEditText = findViewById(R.id.noteEditText);
        categorySpinner = findViewById(R.id.categorySpinner);
        saveButton = findViewById(R.id.saveButton);
        progressBar = findViewById(R.id.progressBar);

        // Change the title to indicate we're editing
        setTitle("עריכת רשומה");
        
        // Setup date picker
        setupDatePicker();

        // Load categories from Firebase
        loadCategories();
        
        // Load entry data
        loadEntryData();

        // Setup radio group listener to switch between income and expense categories
        typeRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.incomeRadioButton) {
                    currentType = "income";
                    updateCategorySpinner(incomeCategories);
                } else {
                    currentType = "expense";
                    updateCategorySpinner(expenseCategories);
                }
            }
        });

        // Setup save button
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveEntry();
            }
        });
    }

    private void setupDatePicker() {
        // Set current date as default
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        dateEditText.setText(dateFormat.format(calendar.getTime()));

        // Show date picker when clicked
        dateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar c = Calendar.getInstance();
                int year = c.get(Calendar.YEAR);
                int month = c.get(Calendar.MONTH);
                int day = c.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(EditEntryActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                Calendar selectedDate = Calendar.getInstance();
                                selectedDate.set(Calendar.YEAR, year);
                                selectedDate.set(Calendar.MONTH, monthOfYear);
                                selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                                dateEditText.setText(dateFormat.format(selectedDate.getTime()));
                            }
                        }, year, month, day);
                datePickerDialog.show();
            }
        });
    }

    private void loadCategories() {
        progressBar.setVisibility(View.VISIBLE);

        mDatabase.child("categories").child(currentUserId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        progressBar.setVisibility(View.GONE);

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

                            // Initialize spinner based on entry type
                            if ("income".equals(currentType)) {
                                updateCategorySpinner(incomeCategories);
                            } else {
                                updateCategorySpinner(expenseCategories);
                            }
                        } else {
                            // Create default categories if none exist
                            createDefaultCategories();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(EditEntryActivity.this, "שגיאה בטעינת קטגוריות", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void createDefaultCategories() {
        Map<String, Object> defaultCategories = new HashMap<>();

        // Default income categories
        List<String> defaultIncome = new ArrayList<>();
        defaultIncome.add("עבודה");
        defaultIncome.add("מתנה");
        defaultIncome.add("אחר");

        // Default expense categories
        List<String> defaultExpense = new ArrayList<>();
        defaultExpense.add("מזון");
        defaultExpense.add("תחבורה");
        defaultExpense.add("בילויים");
        defaultExpense.add("קניות");
        defaultExpense.add("אחר");

        defaultCategories.put("income", defaultIncome);
        defaultCategories.put("expense", defaultExpense);

        mDatabase.child("categories").child(currentUserId).setValue(defaultCategories)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            incomeCategories.addAll(defaultIncome);
                            expenseCategories.addAll(defaultExpense);

                            // Update spinner
                            updateCategorySpinner(incomeCategories);
                        } else {
                            Toast.makeText(EditEntryActivity.this, "שגיאה ביצירת קטגוריות ברירת מחדל", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void updateCategorySpinner(List<String> categories) {
        categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categories);
        categorySpinner.setAdapter(categoryAdapter);
    }

    private void loadEntryData() {
        progressBar.setVisibility(View.VISIBLE);
        
        mDatabase.child("entries").child(currentUserId).child(entryId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        progressBar.setVisibility(View.GONE);
                        
                        if (snapshot.exists()) {
                            // Get entry data
                            String type = snapshot.child("type").getValue(String.class);
                            Double amount = snapshot.child("amount").getValue(Double.class);
                            String category = snapshot.child("category").getValue(String.class);
                            String date = snapshot.child("date").getValue(String.class);
                            String note = snapshot.child("note").getValue(String.class);
                            
                            // Set the entry type
                            currentType = type;
                            if ("income".equals(type)) {
                                ((RadioButton) findViewById(R.id.incomeRadioButton)).setChecked(true);
                            } else {
                                ((RadioButton) findViewById(R.id.expenseRadioButton)).setChecked(true);
                            }
                            
                            // Set the amount
                            if (amount != null) {
                                amountEditText.setText(String.valueOf(amount));
                            }
                            
                            // Set the date
                            if (date != null) {
                                dateEditText.setText(date);
                            }
                            
                            // Set the note
                            if (note != null) {
                                noteEditText.setText(note);
                            }
                            
                            // Category will be set after categories are loaded
                            final String finalCategory = category;
                            if (categoryAdapter != null && finalCategory != null) {
                                // If categories are already loaded
                                int position = ((ArrayAdapter<String>)categorySpinner.getAdapter()).getPosition(finalCategory);
                                if (position >= 0) {
                                    categorySpinner.setSelection(position);
                                }
                            }
                        } else {
                            Toast.makeText(EditEntryActivity.this, "הרשומה לא נמצאה", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
                    
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(EditEntryActivity.this, "שגיאה בטעינת הרשומה", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
    }
    
    private void saveEntry() {
        String amountString = amountEditText.getText().toString().trim();
        String date = dateEditText.getText().toString().trim();
        String note = noteEditText.getText().toString().trim();

        // Validate input
        if (TextUtils.isEmpty(amountString)) {
            amountEditText.setError("נדרש להזין סכום");
            return;
        }

        if (TextUtils.isEmpty(date)) {
            dateEditText.setError("נדרש להזין תאריך");
            return;
        }

        if (categorySpinner.getSelectedItem() == null) {
            Toast.makeText(this, "נדרש לבחור קטגוריה", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountString);
        } catch (NumberFormatException e) {
            amountEditText.setError("הסכום אינו תקין");
            return;
        }

        if (amount <= 0) {
            amountEditText.setError("הסכום חייב להיות גדול מאפס");
            return;
        }

        // Show progress
        progressBar.setVisibility(View.VISIBLE);
        saveButton.setEnabled(false);

        // Create entry data
        String category = categorySpinner.getSelectedItem().toString();
        Map<String, Object> entryData = new HashMap<>();
        entryData.put("type", currentType);
        entryData.put("amount", amount);
        entryData.put("category", category);
        entryData.put("date", date);
        entryData.put("note", note);

        // Update existing entry in Firebase
        mDatabase.child("entries").child(currentUserId).child(entryId).setValue(entryData)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        progressBar.setVisibility(View.GONE);
                        saveButton.setEnabled(true);

                        if (task.isSuccessful()) {
                            Toast.makeText(EditEntryActivity.this, "הרשומה עודכנה בהצלחה", Toast.LENGTH_SHORT).show();
                            finish(); // Return to previous screen
                        } else {
                            Toast.makeText(EditEntryActivity.this, "שגיאה בעדכון הרשומה", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
