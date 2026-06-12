package com.app.mydashboard;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AdminActivity extends AppCompatActivity {

    private static final String TAG = "AdminActivity";

    private DatabaseHelper db;
    private ListView lvBooks, lvReservations;
    private TextView chipBooks, chipReservations, tvEmptyReservations;
    private View containerBooks, containerReservations;
    private Button btnAddBook, btnLogout;

    private ArrayList<Book> bookList = new ArrayList<>();
    private ArrayList<Map<String, Object>> reservationList = new ArrayList<>();
    private AdminBookAdapter bookAdapter;
    private AdminReservationAdapter reservationAdapter;

    private String activeTab = "books"; // "books" or "reservations"

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
        setContentView(R.layout.activity_admin);

        db = new DatabaseHelper(this);

        lvBooks = findViewById(R.id.lv_admin_books);
        lvReservations = findViewById(R.id.lv_admin_reservations);
        chipBooks = findViewById(R.id.chip_manage_books);
        chipReservations = findViewById(R.id.chip_manage_reservations);
        tvEmptyReservations = findViewById(R.id.tv_admin_reservations_empty);
        containerBooks = findViewById(R.id.container_manage_books);
        containerReservations = findViewById(R.id.container_manage_reservations);
        btnAddBook = findViewById(R.id.btn_admin_add_book);
        btnLogout = findViewById(R.id.btn_admin_logout);

        String username = getIntent().getStringExtra("username");
        TextView tvWelcome = findViewById(R.id.admin_welcome_text);
        if (username != null && tvWelcome != null) {
            tvWelcome.setText("Welcome, Admin " + username);
        }

        // Add Book click
        btnAddBook.setOnClickListener(v -> {
            Intent intent = new Intent(AdminActivity.this, BookFormActivity.class);
            startActivity(intent);
        });

        // Logout click
        btnLogout.setOnClickListener(v -> {
            Intent intent = new Intent(AdminActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        // Tab switches
        chipBooks.setOnClickListener(v -> switchTab("books"));
        chipReservations.setOnClickListener(v -> switchTab("reservations"));

        // Initialize lists
        loadBooks();
        loadReservations();
    }

    private void switchTab(String tab) {
        activeTab = tab;
        if ("books".equalsIgnoreCase(tab)) {
            // Highlight books chip
            chipBooks.setBackgroundResource(R.drawable.chip_background_selected);
            chipBooks.setTextColor(getResources().getColor(R.color.white));
            chipReservations.setBackgroundResource(R.drawable.chip_background_unselected);
            chipReservations.setTextColor(getResources().getColor(R.color.text_secondary));

            // Show books list, hide reservations list
            containerBooks.setVisibility(View.VISIBLE);
            containerReservations.setVisibility(View.GONE);
            btnAddBook.setVisibility(View.VISIBLE);
        } else {
            // Highlight reservations chip
            chipReservations.setBackgroundResource(R.drawable.chip_background_selected);
            chipReservations.setTextColor(getResources().getColor(R.color.white));
            chipBooks.setBackgroundResource(R.drawable.chip_background_unselected);
            chipBooks.setTextColor(getResources().getColor(R.color.text_secondary));

            // Show reservations list, hide books list
            containerReservations.setVisibility(View.VISIBLE);
            containerBooks.setVisibility(View.GONE);
            btnAddBook.setVisibility(View.GONE);

            loadReservations(); // Refresh reservations on tab switch
        }
    }

    private void loadBooks() {
        Cursor cursor = db.getBooks();
        bookList.clear();

        while (cursor.moveToNext()) {
            int id = cursor.getInt(0);
            String title = cursor.getString(1);
            String author = cursor.getString(2);
            String cover = cursor.getString(3);
            String file = cursor.getString(4);
            String category = cursor.getString(5);
            int available = cursor.getInt(6);

            bookList.add(new Book(id, title, author, cover, file, category, available));
        }
        cursor.close();

        if (bookAdapter == null) {
            bookAdapter = new AdminBookAdapter(this, bookList, () -> loadBooks());
            lvBooks.setAdapter(bookAdapter);
        } else {
            bookAdapter.notifyDataSetChanged();
        }
    }

    private void loadReservations() {
        Cursor cursor = db.getAllReservations();
        reservationList.clear();

        while (cursor.moveToNext()) {
            Map<String, Object> res = new HashMap<>();
            res.put("id", cursor.getInt(0));
            res.put("userId", cursor.getInt(1));
            res.put("username", cursor.getString(2));
            res.put("bookId", cursor.getInt(3));
            res.put("bookTitle", cursor.getString(4));
            res.put("status", cursor.getString(5));
            res.put("confirmationDate", cursor.getString(6));

            reservationList.add(res);
        }
        cursor.close();

        if (reservationList.isEmpty()) {
            tvEmptyReservations.setVisibility(View.VISIBLE);
            lvReservations.setVisibility(View.GONE);
        } else {
            tvEmptyReservations.setVisibility(View.GONE);
            lvReservations.setVisibility(View.VISIBLE);
        }

        if (reservationAdapter == null) {
            reservationAdapter = new AdminReservationAdapter(this, reservationList, new AdminReservationAdapter.OnReservationActionListener() {
                @Override
                public void onConfirm(int reservationId, int position) {
                    showDatePicker(reservationId);
                }

                @Override
                public void onCancel(int reservationId, int position) {
                    loadReservations();
                }
            });
            lvReservations.setAdapter(reservationAdapter);
        } else {
            reservationAdapter.notifyDataSetChanged();
        }
    }

    private void showDatePicker(int reservationId) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, yearSelected, monthSelected, daySelected) -> {
            // Months are 0-indexed in DatePicker
            String dateString = String.format(Locale.getDefault(), "%d-%02d-%02d", yearSelected, monthSelected + 1, daySelected);
            confirmReservation(reservationId, dateString);
        }, year, month, day);

        datePickerDialog.setTitle("Choose Pickup/Confirmation Date");
        datePickerDialog.show();
    }

    private void confirmReservation(int reservationId, String confirmationDate) {
        Log.d(TAG, "confirmReservation: Confirming reservation " + reservationId + " with date " + confirmationDate);
        if (db.confirmReservation(reservationId, confirmationDate)) {
            Toast.makeText(this, "Reservation confirmed successfully for " + confirmationDate, Toast.LENGTH_SHORT).show();
            loadReservations();
        } else {
            Toast.makeText(this, "Failed to confirm reservation.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadBooks();
        loadReservations();
    }
}
