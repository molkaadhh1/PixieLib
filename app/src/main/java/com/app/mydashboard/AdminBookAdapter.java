package com.app.mydashboard;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class AdminBookAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<Book> books;
    private DatabaseHelper db;
    private OnBookChangedListener listener;

    public interface OnBookChangedListener {
        void onBookChanged();
    }

    public AdminBookAdapter(Context context, ArrayList<Book> books, OnBookChangedListener listener) {
        this.context = context;
        this.books = books;
        this.listener = listener;
        this.db = new DatabaseHelper(context);
    }

    @Override
    public int getCount() {
        return books.size();
    }

    @Override
    public Object getItem(int position) {
        return books.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.book_item, parent, false);
        }

        Book book = books.get(position);

        ImageView bookImage = convertView.findViewById(R.id.book_image);
        TextView bookName = convertView.findViewById(R.id.book_name);
        TextView bookAuthor = convertView.findViewById(R.id.book_author);
        TextView bookCategoryBadge = convertView.findViewById(R.id.book_category_badge);
        Button actionButton = convertView.findViewById(R.id.reserve_button);
        Button askButton = convertView.findViewById(R.id.read_online_button);
        Button cancelReservationButton = convertView.findViewById(R.id.cancel_reservation_button);

        // Prefill generic book info
        bookName.setText(book.getTitle());
        bookAuthor.setText(book.getAuthor());
        if (bookCategoryBadge != null) {
            bookCategoryBadge.setText(book.getCategory());
        }

        @SuppressLint("DiscouragedApi")
        int imageResId = context.getResources().getIdentifier(book.getCover(), "drawable", context.getPackageName());
        if (imageResId != 0) {
            bookImage.setImageResource(imageResId);
        } else {
            bookImage.setImageResource(R.drawable.place_holder);
        }

        // Hide cancel/reserve buttons from standard layout
        if (cancelReservationButton != null) {
            cancelReservationButton.setVisibility(View.GONE);
        }

        // Repurpose actionButton as "Edit" and askButton as "Delete" for Admin
        actionButton.setText("Edit");
        actionButton.setVisibility(View.VISIBLE);
        actionButton.setEnabled(true);
        actionButton.setOnClickListener(v -> {
            Intent intent = new Intent(context, BookFormActivity.class);
            intent.putExtra("book_id", book.getId());
            context.startActivity(intent);
        });

        askButton.setText("Delete");
        askButton.setVisibility(View.VISIBLE);
        askButton.setEnabled(true);
        askButton.setBackgroundResource(R.drawable.chip_background_unselected);
        askButton.setTextColor(context.getResources().getColor(R.color.accent));
        askButton.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Delete Book")
                    .setMessage("Are you sure you want to delete '" + book.getTitle() + "'? This will also cancel all of its reservations.")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        if (db.deleteBook(book.getId())) {
                            Toast.makeText(context, "Book deleted successfully.", Toast.LENGTH_SHORT).show();
                            if (listener != null) {
                                listener.onBookChanged();
                            }
                        } else {
                            Toast.makeText(context, "Failed to delete book.", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        // Set layout alpha based on availability to signify state visually
        convertView.setAlpha(book.getAvailable() == 1 ? 1.0f : 0.65f);

        // Handle item clicks to toggle availability easily
        convertView.setOnClickListener(v -> {
            int newAvailable = book.getAvailable() == 1 ? 0 : 1;
            if (db.updateBook(book.getId(), book.getTitle(), book.getAuthor(), book.getCover(), book.getFile(), book.getCategory(), newAvailable)) {
                book.setAvailable(newAvailable);
                Toast.makeText(context, book.getTitle() + " is now " + (newAvailable == 1 ? "Available" : "Not Available"), Toast.LENGTH_SHORT).show();
                notifyDataSetChanged();
            }
        });

        return convertView;
    }
}
