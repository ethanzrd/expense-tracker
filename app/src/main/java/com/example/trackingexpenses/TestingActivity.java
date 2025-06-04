package com.example.trackingexpenses;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.trackingexpenses.utils.TestUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class TestingActivity extends AppCompatActivity {

    private TextView statusTextView;
    private Button createAdminButton, createUserButton, clearDataButton, logoutButton;
    
    private FirebaseAuth mAuth;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_testing);
        
        // Apply WindowInsets for safe area display
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        
        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            currentUserId = currentUser.getUid();
        }
        
        // Initialize UI components
        statusTextView = findViewById(R.id.statusTextView);
        createAdminButton = findViewById(R.id.createAdminButton);
        createUserButton = findViewById(R.id.createUserButton);
        clearDataButton = findViewById(R.id.clearDataButton);
        logoutButton = findViewById(R.id.logoutButton);
        
        // Setup listeners
        createAdminButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TestUtils.createTestAdminUser(TestingActivity.this);
                updateStatus("יוצר משתמש מנהל...");
            }
        });
        
        createUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TestUtils.createTestRegularUser(TestingActivity.this);
                updateStatus("יוצר משתמש רגיל...");
            }
        });
        
        clearDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentUserId != null) {
                    TestUtils.clearTestData(TestingActivity.this, currentUserId);
                    updateStatus("נתונים נמחקו למשתמש: " + currentUserId);
                }
            }
        });
        
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                finish();
            }
        });
        
        // Check current user status
        checkUserStatus();
    }
    
    private void checkUserStatus() {
        if (currentUserId != null) {
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference()
                    .child("users").child(currentUserId);
            
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String name = snapshot.child("name").getValue(String.class);
                        Boolean isAdmin = snapshot.child("isAdmin").getValue(Boolean.class);
                        
                        String status = "מחובר כ: " + name;
                        status += "\nסוג משתמש: " + (isAdmin != null && isAdmin ? "מנהל" : "רגיל");
                        status += "\nמזהה: " + currentUserId;
                        
                        updateStatus(status);
                    }
                }
                
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    updateStatus("שגיאה בטעינת פרטי משתמש");
                }
            });
        } else {
            updateStatus("לא מחובר - נא להתחבר תחילה");
        }
    }
    
    private void updateStatus(String status) {
        statusTextView.setText(status);
    }
}
