package com.example.trackingexpenses.utils;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Utility class for testing app functionality
 */
public class TestUtils {

    /**
     * Creates a test admin user if it doesn't exist
     */
    public static void createTestAdminUser(final Context context) {
        final FirebaseAuth auth = FirebaseAuth.getInstance();
        final DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        
        // Test admin credentials
        final String email = "admin@test.com";
        final String password = "admin123";
        final String name = "מנהל בדיקות";
        
        // Check if admin exists
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Admin exists
                            Toast.makeText(context, "התחברות למשתמש מנהל", Toast.LENGTH_SHORT).show();
                        } else {
                            // Create admin user
                            auth.createUserWithEmailAndPassword(email, password)
                                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<AuthResult> task) {
                                            if (task.isSuccessful()) {
                                                // Save admin data
                                                String userId = auth.getCurrentUser().getUid();
                                                
                                                Map<String, Object> userData = new HashMap<>();
                                                userData.put("name", name);
                                                userData.put("email", email);
                                                userData.put("isAdmin", true);
                                                
                                                database.child("users").child(userId).setValue(userData)
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    Toast.makeText(context, "נוצר משתמש מנהל לבדיקות", Toast.LENGTH_SHORT).show();
                                                                    
                                                                    // Create default categories
                                                                    createDefaultCategories(userId);
                                                                }
                                                            }
                                                        });
                                            }
                                        }
                                    });
                        }
                    }
                });
    }
    
    /**
     * Creates a test regular user if it doesn't exist
     */
    public static void createTestRegularUser(final Context context) {
        final FirebaseAuth auth = FirebaseAuth.getInstance();
        final DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        
        // Test user credentials
        final String email = "user@test.com";
        final String password = "user123";
        final String name = "משתמש בדיקות";
        
        // Check if user exists
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // User exists
                            Toast.makeText(context, "התחברות למשתמש רגיל", Toast.LENGTH_SHORT).show();
                        } else {
                            // Create regular user
                            auth.createUserWithEmailAndPassword(email, password)
                                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<AuthResult> task) {
                                            if (task.isSuccessful()) {
                                                // Save user data
                                                String userId = auth.getCurrentUser().getUid();
                                                
                                                Map<String, Object> userData = new HashMap<>();
                                                userData.put("name", name);
                                                userData.put("email", email);
                                                userData.put("isAdmin", false);
                                                
                                                database.child("users").child(userId).setValue(userData)
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    Toast.makeText(context, "נוצר משתמש רגיל לבדיקות", Toast.LENGTH_SHORT).show();
                                                                    
                                                                    // Create default categories
                                                                    createDefaultCategories(userId);
                                                                    
                                                                    // Create sample entries
                                                                    createSampleEntries(userId);
                                                                }
                                                            }
                                                        });
                                            }
                                        }
                                    });
                        }
                    }
                });
    }
    
    /**
     * Creates default categories for a user
     */
    private static void createDefaultCategories(String userId) {
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        
        // Create default categories
        Map<String, Object> categories = new HashMap<>();
        
        // Income categories
        categories.put("income", new String[]{"עבודה", "מתנה", "אחר"});
        
        // Expense categories
        categories.put("expense", new String[]{"מזון", "תחבורה", "בילויים", "קניות", "אחר"});
        
        // Save categories
        database.child("categories").child(userId).setValue(categories);
    }
    
    /**
     * Creates sample entries for a user
     */
    private static void createSampleEntries(String userId) {
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        DatabaseReference entriesRef = database.child("entries").child(userId);
        
        // Current date format
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String currentDate = dateFormat.format(new Date());
        
        // Create sample income entry
        Map<String, Object> incomeEntry = new HashMap<>();
        incomeEntry.put("type", "income");
        incomeEntry.put("amount", 5000.0);
        incomeEntry.put("category", "עבודה");
        incomeEntry.put("date", currentDate);
        incomeEntry.put("note", "משכורת");
        
        // Create sample expense entries
        Map<String, Object> expenseEntry1 = new HashMap<>();
        expenseEntry1.put("type", "expense");
        expenseEntry1.put("amount", 200.0);
        expenseEntry1.put("category", "מזון");
        expenseEntry1.put("date", currentDate);
        expenseEntry1.put("note", "קניות בסופר");
        
        Map<String, Object> expenseEntry2 = new HashMap<>();
        expenseEntry2.put("type", "expense");
        expenseEntry2.put("amount", 100.0);
        expenseEntry2.put("category", "תחבורה");
        expenseEntry2.put("date", currentDate);
        expenseEntry2.put("note", "דלק");
        
        // Save entries
        entriesRef.push().setValue(incomeEntry);
        entriesRef.push().setValue(expenseEntry1);
        entriesRef.push().setValue(expenseEntry2);
    }
    
    /**
     * Clears all test data for the current user
     */
    public static void clearTestData(final Context context, String userId) {
        if (userId != null) {
            DatabaseReference database = FirebaseDatabase.getInstance().getReference();
            
            // Remove entries
            database.child("entries").child(userId).removeValue();
            
            // Remove categories
            database.child("categories").child(userId).removeValue();
            
            Toast.makeText(context, "נתוני בדיקה נמחקו", Toast.LENGTH_SHORT).show();
        }
    }
}
