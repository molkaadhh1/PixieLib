package com.app.mydashboard;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class BookFormActivity extends AppCompatActivity {

    private DatabaseHelper db;
    private int bookId = -1;
    private boolean isEditMode = false;

    private EditText editTitle, editAuthor, editCover, editFile;
    private Spinner spinnerCategory;
    private Switch switchAvailable;
    private TextView tvHeaderTitle;

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
        setContentView(R.layout.activity_book_form);

        db = new DatabaseHelper(this);

        editTitle = findViewById(R.id.edit_book_title);
        editAuthor = findViewById(R.id.edit_book_author);
        editCover = findViewById(R.id.edit_book_cover);
        editFile = findViewById(R.id.edit_book_file);
        spinnerCategory = findViewById(R.id.spinner_book_category);
        switchAvailable = findViewById(R.id.switch_book_available);
        tvHeaderTitle = findViewById(R.id.form_title);
        TextView btnBack = findViewById(R.id.btn_form_back);
        Button btnSave = findViewById(R.id.btn_save_book);

        // Set up Spinner categories
        ArrayList<String> categories = new ArrayList<>();
        categories.add("Development");
        categories.add("AI & Networking");
        categories.add("Software Architecture");
        categories.add("Databases");
        categories.add("Data Science");
        categories.add("Security");
        categories.add("Cloud");
        categories.add("Mobile");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);

        // Check if we are editing an existing book
        bookId = getIntent().getIntExtra("book_id", -1);
        if (bookId != -1) {
            isEditMode = true;
            tvHeaderTitle.setText("Edit Book");
            prefillForm();
        } else {
            isEditMode = false;
            tvHeaderTitle.setText("Add Book");
        }

        btnBack.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> saveBook());
    }

    private void prefillForm() {
        // Find the book by querying db
        android.database.Cursor cursor = db.getBooks();
        while (cursor.moveToNext()) {
            int id = cursor.getInt(0);
            if (id == bookId) {
                editTitle.setText(cursor.getString(1));
                editAuthor.setText(cursor.getString(2));
                editCover.setText(cursor.getString(3));
                editFile.setText(cursor.getString(4));

                String category = cursor.getString(5);
                int available = cursor.getInt(6);

                switchAvailable.setChecked(available == 1);

                // Set category spinner selection
                if ("Development".equalsIgnoreCase(category)) {
                    spinnerCategory.setSelection(0);
                } else if ("AI & Networking".equalsIgnoreCase(category) || "AI & Net".equalsIgnoreCase(category)) {
                    spinnerCategory.setSelection(1);
                } else if ("Software Architecture".equalsIgnoreCase(category)) {
                    spinnerCategory.setSelection(2);
                } else if ("Databases".equalsIgnoreCase(category)) {
                    spinnerCategory.setSelection(3);
                } else if ("Data Science".equalsIgnoreCase(category)) {
                    spinnerCategory.setSelection(4);
                } else if ("Security".equalsIgnoreCase(category)) {
                    spinnerCategory.setSelection(5);
                } else if ("Cloud".equalsIgnoreCase(category)) {
                    spinnerCategory.setSelection(6);
                } else if ("Mobile".equalsIgnoreCase(category)) {
                    spinnerCategory.setSelection(7);
                }
                break;
            }
        }
        cursor.close();
    }

    private void saveBook() {
        String title = editTitle.getText().toString().trim();
        String author = editAuthor.getText().toString().trim();
        String cover = editCover.getText().toString().trim();
        String file = editFile.getText().toString().trim();
        String category = spinnerCategory.getSelectedItem().toString();
        int available = switchAvailable.isChecked() ? 1 : 0;

        if (title.isEmpty() || author.isEmpty() || cover.isEmpty() || file.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean success;
        if (isEditMode) {
            success = db.updateBook(bookId, title, author, cover, file, category, available);
            if (success) {
                Toast.makeText(this, "Book updated successfully!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to update book.", Toast.LENGTH_SHORT).show();
            }
        } else {
            success = db.addBook(title, author, cover, file, category, available);
            if (success) {
                Toast.makeText(this, "Book added successfully!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to add book.", Toast.LENGTH_SHORT).show();
            }
        }

        if (success) {
            setResult(RESULT_OK);
            finish();
        }
    }
}
