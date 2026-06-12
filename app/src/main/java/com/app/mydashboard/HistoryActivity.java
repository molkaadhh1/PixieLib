package com.app.mydashboard;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class HistoryActivity extends AppCompatActivity {

    private static final String TAG = "HistoryActivity";

    private int userId;
    private FirebaseFirestore firestore;
    private ListView listView;
    private EditText searchInput;
    private TextView btnBack;
    private TextView btnClearAll;
    private View emptyStateView;

    private ArrayList<Map<String, Object>> fullHistoryList = new ArrayList<>();
    private ArrayList<Map<String, Object>> filteredHistoryList = new ArrayList<>();
    private HistoryAdapter adapter;
    private String currentSearchQuery = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Load theme settings before setting view
        android.content.SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        boolean isDarkMode = prefs.getBoolean("dark_mode", false);
        if (isDarkMode) {
            androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        userId = getIntent().getIntExtra("user_id", -1);
        if (userId == -1) {
            Toast.makeText(this, "Error: User ID not found.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        firestore = FirebaseFirestore.getInstance();
        listView = findViewById(R.id.history_list_view);
        searchInput = findViewById(R.id.history_search_input);
        btnBack = findViewById(R.id.btn_back);
        btnClearAll = findViewById(R.id.btn_clear_all);
        emptyStateView = findViewById(R.id.empty_state_view);

        // Back action
        btnBack.setOnClickListener(v -> finish());

        // Clear All action
        btnClearAll.setOnClickListener(v -> confirmClearAll());

        // Search text watcher
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchQuery = s.toString();
                applyFilters();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Load history logs from firestore
        loadHistoryLogs();
    }

    private void loadHistoryLogs() {
        Log.d(TAG, "loadHistoryLogs: Fetching records for userId = " + userId);
        firestore.collection("download_history")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    fullHistoryList.clear();
                    if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                        for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                            Map<String, Object> data = doc.getData();
                            if (data != null) {
                                Map<String, Object> item = new HashMap<>(data);
                                item.put("docId", doc.getId()); // Store document ID for deletion
                                fullHistoryList.add(item);
                            }
                        }
                        // Sort chronologically (newest first)
                        fullHistoryList.sort((o1, o2) -> {
                            String t1 = (String) o1.get("timestamp");
                            String t2 = (String) o2.get("timestamp");
                            if (t1 != null && t2 != null) {
                                return t2.compareTo(t1); // Reverse chronological
                            }
                            return 0;
                        });
                    }
                    Log.d(TAG, "loadHistoryLogs: Loaded " + fullHistoryList.size() + " items");
                    applyFilters();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "loadHistoryLogs: Error loading history", e);
                    Toast.makeText(HistoryActivity.this, "Failed to load download history.", Toast.LENGTH_SHORT).show();
                    applyFilters();
                });
    }

    private void applyFilters() {
        filteredHistoryList.clear();
        for (Map<String, Object> item : fullHistoryList) {
            String bookTitle = (String) item.get("bookTitle");
            boolean matchesSearch = bookTitle == null || bookTitle.toLowerCase().contains(currentSearchQuery.toLowerCase());
            if (matchesSearch) {
                filteredHistoryList.add(item);
            }
        }

        // Show/Hide Empty State
        if (filteredHistoryList.isEmpty()) {
            listView.setVisibility(View.GONE);
            emptyStateView.setVisibility(View.VISIBLE);
        } else {
            listView.setVisibility(View.VISIBLE);
            emptyStateView.setVisibility(View.GONE);
        }

        // Setup/Update Adapter
        if (adapter == null) {
            adapter = new HistoryAdapter(this, filteredHistoryList, (documentId, position) -> deleteHistoryItem(documentId, position));
            listView.setAdapter(adapter);
        } else {
            adapter.notifyDataSetChanged();
        }
    }

    private void deleteHistoryItem(String documentId, int listPosition) {
        Log.d(TAG, "deleteHistoryItem: Deleting document = " + documentId);
        firestore.collection("download_history")
                .document(documentId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(HistoryActivity.this, "Entry deleted from history", Toast.LENGTH_SHORT).show();
                    // Find and remove from full list
                    for (int i = 0; i < fullHistoryList.size(); i++) {
                        if (documentId.equals(fullHistoryList.get(i).get("docId"))) {
                            fullHistoryList.remove(i);
                            break;
                        }
                    }
                    applyFilters();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "deleteHistoryItem: Failed to delete", e);
                    Toast.makeText(HistoryActivity.this, "Failed to delete entry.", Toast.LENGTH_SHORT).show();
                });
    }

    private void confirmClearAll() {
        if (fullHistoryList.isEmpty()) {
            Toast.makeText(this, "No download history to clear.", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Clear History")
                .setMessage("Are you sure you want to delete all entries from your download history? This action cannot be undone.")
                .setPositiveButton("Clear All", (dialog, which) -> clearAllHistory())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void clearAllHistory() {
        Log.d(TAG, "clearAllHistory: Purging all history documents for userId = " + userId);
        Toast.makeText(this, "Clearing history...", Toast.LENGTH_SHORT).show();

        // Query documents to delete them
        firestore.collection("download_history")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                        int total = queryDocumentSnapshots.size();
                        final int[] deletedCount = {0};

                        for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                            firestore.collection("download_history")
                                    .document(doc.getId())
                                    .delete()
                                    .addOnSuccessListener(aVoid -> {
                                        deletedCount[0]++;
                                        if (deletedCount[0] == total) {
                                            Toast.makeText(HistoryActivity.this, "Download history cleared successfully", Toast.LENGTH_SHORT).show();
                                            fullHistoryList.clear();
                                            applyFilters();
                                        }
                                    });
                        }
                    } else {
                        fullHistoryList.clear();
                        applyFilters();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "clearAllHistory: Failed to get documents for clear", e);
                    Toast.makeText(HistoryActivity.this, "Failed to clear history.", Toast.LENGTH_SHORT).show();
                });
    }
}
