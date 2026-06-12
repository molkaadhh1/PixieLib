package com.app.mydashboard;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class HistoryAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<Map<String, Object>> historyList;
    private DatabaseHelper db;
    private HashMap<String, Book> bookCache = new HashMap<>();
    private FirebaseFirestore firestore;
    private OnDeleteListener onDeleteListener;

    public interface OnDeleteListener {
        void onDelete(String documentId, int position);
    }

    public HistoryAdapter(Context context, ArrayList<Map<String, Object>> historyList, OnDeleteListener onDeleteListener) {
        this.context = context;
        this.historyList = historyList;
        this.onDeleteListener = onDeleteListener;
        this.db = new DatabaseHelper(context);
        this.firestore = FirebaseFirestore.getInstance();

        // Build a cache of books to quickly look up cover/author by title
        try {
            Cursor cursor = db.getBooks();
            while (cursor.moveToNext()) {
                int id = cursor.getInt(0);
                String title = cursor.getString(1);
                String author = cursor.getString(2);
                String cover = cursor.getString(3);
                String file = cursor.getString(4);
                String category = cursor.getString(5);
                bookCache.put(title.toLowerCase().trim(), new Book(id, title, author, cover, file, category));
            }
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getCount() {
        return historyList.size();
    }

    @Override
    public Object getItem(int position) {
        return historyList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.history_item, parent, false);
        }

        Map<String, Object> item = historyList.get(position);
        String bookTitle = (String) item.get("bookTitle");
        String timestamp = (String) item.get("timestamp");
        String docId = (String) item.get("docId");

        ImageView bookImage = convertView.findViewById(R.id.history_book_image);
        TextView tvTitle = convertView.findViewById(R.id.history_book_title);
        TextView tvAuthor = convertView.findViewById(R.id.history_book_author);
        TextView tvTime = convertView.findViewById(R.id.history_download_time);
        Button btnRead = convertView.findViewById(R.id.history_read_button);
        ImageButton btnDelete = convertView.findViewById(R.id.history_delete_button);

        // Bind title and time
        tvTitle.setText(bookTitle);
        tvTime.setText("Downloaded: " + (timestamp != null ? timestamp : "Unknown"));

        // Match with cached book info
        Book matchedBook = null;
        if (bookTitle != null) {
            matchedBook = bookCache.get(bookTitle.toLowerCase().trim());
        }

        if (matchedBook != null) {
            tvAuthor.setText(matchedBook.getAuthor());
            @SuppressLint("DiscouragedApi")
            int imageResId = context.getResources().getIdentifier(matchedBook.getCover(), "drawable", context.getPackageName());
            if (imageResId != 0) {
                bookImage.setImageResource(imageResId);
            } else {
                bookImage.setImageResource(R.drawable.place_holder);
            }
        } else {
            tvAuthor.setText("Unknown Author");
            bookImage.setImageResource(R.drawable.place_holder);
        }

        // Setup Read button click
        final Book finalBook = matchedBook;
        btnRead.setOnClickListener(v -> {
            if (finalBook != null) {
                String bookUrl;
                if (Objects.equals(finalBook.getTitle(), "Introduction to Algorithms") || Objects.equals(finalBook.getTitle(), "Inroduction to Algorithms")) {
                    bookUrl = "https://drive.google.com/file/d/1tzL7pAygq-xWK8wt1tRSHhwx8wS7Tt4S/view?usp=sharing";
                } else if (Objects.equals(finalBook.getTitle(), "Design Patterns")) {
                    bookUrl = "https://drive.google.com/file/d/1c37BhPQp4JItl6Qq4bDv-mOc4ts6WfFx/view?usp=sharing";
                } else if (Objects.equals(finalBook.getTitle(), "Computer Networking")) {
                    bookUrl = "https://drive.google.com/file/d/1tmBflgqsEZ3wAO5ktJuPafafqV1Vh2An/view?usp=sharing";
                } else {
                    bookUrl = "";
                }

                Intent intent = new Intent(context, GoogleDriveReaderActivity.class);
                intent.putExtra("bookTitle", finalBook.getTitle());
                intent.putExtra("bookFile", finalBook.getFile());
                intent.putExtra("bookurl", bookUrl);
                intent.putExtra("userid", (int) item.get("userId"));
                context.startActivity(intent);
            } else {
                Toast.makeText(context, "Book details are not available locally.", Toast.LENGTH_SHORT).show();
            }
        });

        // Setup Delete button click
        btnDelete.setOnClickListener(v -> {
            if (docId != null && onDeleteListener != null) {
                onDeleteListener.onDelete(docId, position);
            }
        });

        return convertView;
    }
}
