package com.example.trackingexpenses;

import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.trackingexpenses.fragments.UserCategoriesFragment;
import com.example.trackingexpenses.fragments.UserEntriesFragment;
import com.example.trackingexpenses.fragments.UserSummaryFragment;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class UserDetailsActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextView userNameTextView;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    
    private String userId;
    private String userName;
    
    private static final int NUM_PAGES = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_details);
        
        // Apply WindowInsets for safe area display
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        
        // Get user data from intent
        userId = getIntent().getStringExtra("userId");
        userName = getIntent().getStringExtra("userName");
        
        if (userId == null || userName == null) {
            finish();
            return;
        }
        
        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        
        // Setup toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Initialize UI components
        userNameTextView = findViewById(R.id.userNameTextView);
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);
        
        // Set user name
        userNameTextView.setText(userName);
        
        // Setup ViewPager with fragments
        UserDetailsPagerAdapter pagerAdapter = new UserDetailsPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);
        
        // Connect TabLayout with ViewPager
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("סיכום");
                    break;
                case 1:
                    tab.setText("רשומות");
                    break;
                case 2:
                    tab.setText("קטגוריות");
                    break;
            }
        }).attach();
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
    
    // ViewPager adapter for the tabs
    private class UserDetailsPagerAdapter extends FragmentStateAdapter {
        public UserDetailsPagerAdapter(FragmentActivity fa) {
            super(fa);
        }
        
        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0:
                    return UserSummaryFragment.newInstance(userId);
                case 1:
                    return UserEntriesFragment.newInstance(userId);
                case 2:
                    return UserCategoriesFragment.newInstance(userId);
                default:
                    return UserSummaryFragment.newInstance(userId);
            }
        }
        
        @Override
        public int getItemCount() {
            return NUM_PAGES;
        }
    }
}
