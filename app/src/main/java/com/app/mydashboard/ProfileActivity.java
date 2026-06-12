package com.app.mydashboard;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileActivity extends AppCompatActivity {

    private DatabaseHelper db;
    private int userId;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        db = new DatabaseHelper(this);
        userId = getIntent().getIntExtra("user_id", -1);
        String username = getIntent().getStringExtra("username");

        TextView tvUsername = findViewById(R.id.profile_username);
        TextView tvUserId = findViewById(R.id.profile_user_id);
        TextView tvReservedCount = findViewById(R.id.stat_reserved_count);
        TextView tvDownloadedCount = findViewById(R.id.stat_downloaded_count);
        SwitchCompat switchTheme = findViewById(R.id.switch_theme);
        Button btnLogout = findViewById(R.id.btn_logout);

        if (username == null) {
            username = db.getUsernameById(userId);
        }

        if (tvUsername != null) {
            tvUsername.setText(username != null ? username : "Reader");
        }
        if (tvUserId != null) {
            tvUserId.setText("User ID: #" + userId);
        }

        // Fetch local statistics
        int reservedCount = db.getReservedBookCount(userId);
        if (tvReservedCount != null) {
            tvReservedCount.setText(String.valueOf(reservedCount));
        }

        // Fetch Firestore download statistics
        if (tvDownloadedCount != null) {
            FirebaseFirestore.getInstance().collection("download_history")
                    .whereEqualTo("userId", userId)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (queryDocumentSnapshots != null) {
                            tvDownloadedCount.setText(String.valueOf(queryDocumentSnapshots.size()));
                        }
                    })
                    .addOnFailureListener(e -> tvDownloadedCount.setText("0"));
        }

        // Setup theme switch
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        boolean isDarkMode = prefs.getBoolean("dark_mode", false);
        if (switchTheme != null) {
            switchTheme.setChecked(isDarkMode);
            switchTheme.setOnCheckedChangeListener((buttonView, isChecked) -> {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("dark_mode", isChecked);
                editor.apply();

                if (isChecked) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                }
            });
        }

        // Setup logout button
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
                Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        }

        // Setup bottom navigation
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        if (bottomNavigationView != null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_profile);
            bottomNavigationView.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_home) {
                    Intent homeIntent = new Intent(ProfileActivity.this, MainActivity.class);
                    homeIntent.putExtra("user_id", userId);
                    homeIntent.putExtra("username", getIntent().getStringExtra("username"));
                    startActivity(homeIntent);
                    overridePendingTransition(0, 0);
                    return true;
                } else if (itemId == R.id.nav_reserved) {
                    Intent reservedIntent = new Intent(ProfileActivity.this, ReserveActivity.class);
                    reservedIntent.putExtra("user_id", userId);
                    reservedIntent.putExtra("username", getIntent().getStringExtra("username"));
                    startActivity(reservedIntent);
                    overridePendingTransition(0, 0);
                    return true;
                } else if (itemId == R.id.nav_profile) {
                    return true;
                }
                return false;
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (bottomNavigationView != null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_profile);
        }
        // Refresh local stats
        TextView tvReservedCount = findViewById(R.id.stat_reserved_count);
        if (tvReservedCount != null) {
            tvReservedCount.setText(String.valueOf(db.getReservedBookCount(userId)));
        }
    }
}
