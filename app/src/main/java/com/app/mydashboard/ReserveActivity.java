package com.app.mydashboard;

import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;

public class ReserveActivity extends AppCompatActivity {

    private DatabaseHelper db; // Local SQLite database helper
    private int userId;
    private String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reserve);

        // Initialize the database helper
        db = new DatabaseHelper(this);

        // Bind the ListView
        ListView lvReservedBooks = findViewById(R.id.book_list_view);

        // Get the user ID and username passed via Intent
        userId = getIntent().getIntExtra("user_id", -1);
        password=getIntent().getStringExtra("password");
        String username = getIntent().getStringExtra("username");
        int count = getReservedBooksCount(userId);

        // When done, set the result and finish the activity
       // setResult(RESULT_OK, getIntent().putExtra("reserved_books_count", count));
        //finish();

        // Debugging logs
        if (username != null) {
            Log.d("ReserveActivity", "Received username: " + username);
        } else {
            Log.d("ReserveActivity", "Error: username is null");
        }

        // Validate user ID
        if (userId == -1) {
            Toast.makeText(this, "Error: User not logged in!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Load reserved books
        loadReservedBooks();

        // Set up bottom navigation
        setupBottomNavigation();

        // Set up the "Show History" button
        Button showHistoryButton = findViewById(R.id.viewDownloadHistoryButton);
        showHistoryButton.setOnClickListener(v -> {
            Intent intent = new Intent(ReserveActivity.this, HistoryActivity.class);
            intent.putExtra("user_id", userId);
            startActivity(intent);
        });
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        if (bottomNavigationView != null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_reserved);
            bottomNavigationView.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_home) {
                    Intent homeIntent = new Intent(ReserveActivity.this, MainActivity.class);
                    homeIntent.putExtra("user_id", userId);
                    homeIntent.putExtra("username", getIntent().getStringExtra("username"));
                    startActivity(homeIntent);
                    overridePendingTransition(0, 0);
                    return true;
                } else if (itemId == R.id.nav_reserved) {
                    return true;
                } else if (itemId == R.id.nav_profile) {
                    Intent profileIntent = new Intent(ReserveActivity.this, ProfileActivity.class);
                    profileIntent.putExtra("user_id", userId);
                    profileIntent.putExtra("username", getIntent().getStringExtra("username"));
                    startActivity(profileIntent);
                    overridePendingTransition(0, 0);
                    return true;
                }
                return false;
            });
        }
    }

    private void loadReservedBooks() {
        Cursor cursor = db.getReservedBooks(userId);
        ArrayList<Book> reservedBooks = new ArrayList<>();

        while (cursor.moveToNext()) {
            int id = cursor.getInt(0);
            String title = cursor.getString(1);
            String author = cursor.getString(2);
            String cover = cursor.getString(3);
            String file = cursor.getString(4);
            String category = cursor.getString(5);
            int available = cursor.getInt(6);
            String status = cursor.getString(7);
            String confDate = cursor.getString(8);

            Book book = new Book(id, title, author, cover, file, category, available);
            book.setReservationStatus(status);
            book.setReservationDate(confDate);
            reservedBooks.add(book);
        }

        cursor.close();

        BookAdapter adapter = new BookAdapter(this, reservedBooks, userId, "download");
        ListView lvReservedBooks = findViewById(R.id.book_list_view);
        if (lvReservedBooks != null) {
            lvReservedBooks.setAdapter(adapter);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        if (bottomNavigationView != null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_reserved);
        }
        loadReservedBooks();
    }

    private int getReservedBooksCount(int userId) {
        Cursor cursor = db.getReservedBooks(userId);  // Get reserved books for the user
        int count = cursor.getCount();  // Get the number of rows (reserved books)
        cursor.close();  // Close the cursor
        return count;
    }

}
