package com.app.mydashboard; // Déclare le package de l'application

// Importe les classes nécessaires pour cette activité

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private DatabaseHelper db;
    private int userId;
    private ListView lvBooks;
    private BottomNavigationView bottomNavigationView;

    private ArrayList<Book> allBooks = new ArrayList<>();
    private ArrayList<Book> filteredBooks = new ArrayList<>();
    private BookAdapter adapter;
    private String currentSearchQuery = "";
    private String currentCategoryFilter = "All";

    private android.widget.EditText etSearch;
    private TextView chipAll, chipDev, chipAi, chipArch, chipDatabases, chipDataScience, chipSecurity, chipCloud, chipMobile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = new DatabaseHelper(this);
        lvBooks = findViewById(R.id.lvBooks);
        TextView tvWelcome = findViewById(R.id.welcome_text);

        String username = getIntent().getStringExtra("username");
        userId = getIntent().getIntExtra("user_id", -1);

        tvWelcome.setText("Welcome Back,\n" + username + "!");

        // Initialize category views
        etSearch = findViewById(R.id.search_input);
        chipAll = findViewById(R.id.chip_all);
        chipDev = findViewById(R.id.chip_dev);
        chipAi = findViewById(R.id.chip_ai);
        chipArch = findViewById(R.id.chip_arch);
        chipDatabases = findViewById(R.id.chip_databases);
        chipDataScience = findViewById(R.id.chip_datascience);
        chipSecurity = findViewById(R.id.chip_security);
        chipCloud = findViewById(R.id.chip_cloud);
        chipMobile = findViewById(R.id.chip_mobile);

        // Bind search field to dynamic filtering
        if (etSearch != null) {
            etSearch.addTextChangedListener(new android.text.TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    currentSearchQuery = s.toString();
                    applyFilters();
                }

                @Override
                public void afterTextChanged(android.text.Editable s) {}
            });
        }

        // Bind chips click events
        if (chipAll != null) chipAll.setOnClickListener(v -> selectCategory("All", chipAll));
        if (chipDev != null) chipDev.setOnClickListener(v -> selectCategory("Development", chipDev));
        if (chipAi != null) chipAi.setOnClickListener(v -> selectCategory("AI & Net", chipAi));
        if (chipArch != null) chipArch.setOnClickListener(v -> selectCategory("Software Architecture", chipArch));
        if (chipDatabases != null) chipDatabases.setOnClickListener(v -> selectCategory("Databases", chipDatabases));
        if (chipDataScience != null) chipDataScience.setOnClickListener(v -> selectCategory("Data Science", chipDataScience));
        if (chipSecurity != null) chipSecurity.setOnClickListener(v -> selectCategory("Security", chipSecurity));
        if (chipCloud != null) chipCloud.setOnClickListener(v -> selectCategory("Cloud", chipCloud));
        if (chipMobile != null) chipMobile.setOnClickListener(v -> selectCategory("Mobile", chipMobile));

        // Load current data
        loadBooks();

        // Configure Bottom Navigation
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        if (bottomNavigationView != null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_home);
            bottomNavigationView.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_home) {
                    return true;
                } else if (itemId == R.id.nav_reserved) {
                    Intent intent = new Intent(MainActivity.this, ReserveActivity.class);
                    intent.putExtra("user_id", userId);
                    intent.putExtra("username", username);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    return true;
                } else if (itemId == R.id.nav_profile) {
                    Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                    intent.putExtra("user_id", userId);
                    intent.putExtra("username", username);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    return true;
                }
                return false;
            });
        }
    }

    private void loadBooks() {
        Cursor cursor = db.getBooks();
        allBooks.clear();

        while (cursor.moveToNext()) {
            int id = cursor.getInt(0);
            String title = cursor.getString(1);
            String author = cursor.getString(2);
            String cover = cursor.getString(3);
            String file = cursor.getString(4);
            String category = cursor.getString(5);
            int available = cursor.getInt(6);

            allBooks.add(new Book(id, title, author, cover, file, category, available));
        }
        cursor.close();

        applyFilters();
    }

    private void applyFilters() {
        filteredBooks.clear();
        for (Book book : allBooks) {
            boolean matchesSearch = book.getTitle().toLowerCase().contains(currentSearchQuery.toLowerCase()) ||
                                    book.getAuthor().toLowerCase().contains(currentSearchQuery.toLowerCase());
            boolean matchesCategory = "All".equalsIgnoreCase(currentCategoryFilter) ||
                                      book.getCategory().equalsIgnoreCase(currentCategoryFilter) ||
                                      (currentCategoryFilter.equalsIgnoreCase("AI & Net") && book.getCategory().equalsIgnoreCase("AI & Networking"));

            if (matchesSearch && matchesCategory) {
                filteredBooks.add(book);
            }
        }

        if (adapter == null) {
            adapter = new BookAdapter(this, filteredBooks, userId, "reserve");
            lvBooks.setAdapter(adapter);
        } else {
            adapter.notifyDataSetChanged();
        }

        updateReservedCount();
    }

    private void updateReservedCount() {
        TextView tvReservedCount = findViewById(R.id.tvReservedCount);
        if (tvReservedCount != null) {
            int count = db.getReservedBookCount(userId);
            tvReservedCount.setText("Reserved Books: " + count);
        }
    }

    private void selectCategory(String category, TextView selectedChip) {
        currentCategoryFilter = category;

        // Reset backgrounds for all chips in a clean loop
        TextView[] chips = {chipAll, chipDev, chipAi, chipArch, chipDatabases, chipDataScience, chipSecurity, chipCloud, chipMobile};
        for (TextView chip : chips) {
            if (chip != null) {
                chip.setBackgroundResource(R.drawable.chip_background_unselected);
                chip.setTextColor(getResources().getColor(R.color.text_secondary));
            }
        }

        // Apply active background
        selectedChip.setBackgroundResource(R.drawable.chip_background_selected);
        selectedChip.setTextColor(getResources().getColor(R.color.white));

        applyFilters();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (bottomNavigationView != null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_home);
        }
        updateReservedCount();
    }
}