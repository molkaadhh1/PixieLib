package com.app.mydashboard;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class BookAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<Book> books;
    private DatabaseHelper db;
    private int userId;
    private String mode; // "reserve" or "download"
    private String password ;
    private LocalBroadcastManager localBroadcastManager;
    private BroadcastReceiver downloadReceiver;
    private FirebaseFirestore firestore; // Firestore instance for logging downloads
    private static final String CHANNEL_ID = "download_channel";

    public BookAdapter(Context context, ArrayList<Book> books, int userId, String mode) {
        this.context = context;
        this.books = books;
        this.userId = userId;
        this.mode = mode;
        this.db = new DatabaseHelper(context);
        this.firestore = FirebaseFirestore.getInstance(); // Initialize Firestore instance

        // Register BroadcastReceiver for handling download completion
        localBroadcastManager = LocalBroadcastManager.getInstance(context);
        registerDownloadReceiver();

        // Create a notification channel for Android Oreo and above
        createNotificationChannel();
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

        // Bind views
        ImageView bookImage = convertView.findViewById(R.id.book_image);
        TextView bookName = convertView.findViewById(R.id.book_name);
        TextView bookAuthor = convertView.findViewById(R.id.book_author);
        TextView bookCategoryBadge = convertView.findViewById(R.id.book_category_badge);
        Button actionButton = convertView.findViewById(R.id.reserve_button);
        Button ask = convertView.findViewById(R.id.read_online_button);
        Button cancelReservationButton = convertView.findViewById(R.id.cancel_reservation_button);

        View reservationStatusContainer = convertView.findViewById(R.id.reservation_status_container);
        TextView reservationStatusText = convertView.findViewById(R.id.reservation_status_text);
        TextView reservationDateText = convertView.findViewById(R.id.reservation_date_text);

        // Set book details
        bookName.setText(book.getTitle());
        bookAuthor.setText(book.getAuthor());
        if (bookCategoryBadge != null) {
            bookCategoryBadge.setText(book.getCategory());
        }

        @SuppressLint("DiscouragedApi")
        int imageResId = context.getResources().getIdentifier(book.getCover(), "drawable", context.getPackageName());
        if (imageResId != 0) {
            bookImage.setImageResource(imageResId);
        }

        // Handle Reservation Status / Confirmation Date Badge
        if (book.getReservationStatus() != null && reservationStatusContainer != null) {
            reservationStatusContainer.setVisibility(View.VISIBLE);
            if ("Confirmed".equalsIgnoreCase(book.getReservationStatus())) {
                reservationStatusText.setText("Confirmed");
                reservationStatusText.setBackgroundResource(R.drawable.chip_background_selected); // primary green
                reservationStatusText.setTextColor(context.getResources().getColor(R.color.white));
            } else {
                reservationStatusText.setText("Pending");
                reservationStatusText.setBackgroundResource(R.drawable.chip_background_unselected); // secondary gray
                reservationStatusText.setTextColor(context.getResources().getColor(R.color.text_secondary));
            }

            if (book.getReservationDate() != null && !book.getReservationDate().isEmpty() && reservationDateText != null) {
                reservationDateText.setVisibility(View.VISIBLE);
                reservationDateText.setText("Date: " + book.getReservationDate());
            } else if (reservationDateText != null) {
                reservationDateText.setVisibility(View.GONE);
            }
        } else if (reservationStatusContainer != null) {
            reservationStatusContainer.setVisibility(View.GONE);
        }

        // Handle Availability Styling & Behavior
        boolean isAvailable = book.getAvailable() == 1;
        if (!isAvailable) {
            convertView.setAlpha(0.55f);
            actionButton.setEnabled(false);
            actionButton.setText("Not Available");
            ask.setEnabled(false);
            if (cancelReservationButton != null) {
                cancelReservationButton.setEnabled(false);
            }
            // Clear click listeners
            actionButton.setOnClickListener(null);
            ask.setOnClickListener(null);
            if (cancelReservationButton != null) {
                cancelReservationButton.setOnClickListener(null);
            }
        } else {
            convertView.setAlpha(1.0f);
            actionButton.setEnabled(true);
            ask.setEnabled(true);
            if (cancelReservationButton != null) {
                cancelReservationButton.setEnabled(true);
            }

            // Handle button behavior based on the mode
            if ("reserve".equals(mode)) {
                actionButton.setText("Reserve");
                if (cancelReservationButton != null) {
                    cancelReservationButton.setVisibility(View.GONE);
                }
                actionButton.setOnClickListener(v -> {
                    if (!isBookAlreadyReserved(userId, book.getId())) {
                        if (db.addReservation(userId, book.getId())) {
                            Toast.makeText(context, "Book reserved successfully!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, "Failed to reserve the book. Please try again.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(context, "You already reserved this book.", Toast.LENGTH_SHORT).show();
                    }
                });
            } else if ("download".equals(mode)) {
                actionButton.setText("Download");
                if (cancelReservationButton != null) {
                    cancelReservationButton.setVisibility(View.VISIBLE);
                    cancelReservationButton.setOnClickListener(v -> {
                        if (db.deleteReservation(userId, book.getId())) {
                            Toast.makeText(context, "Reservation cancelled!", Toast.LENGTH_SHORT).show();
                            books.remove(position);
                            notifyDataSetChanged();
                        } else {
                            Toast.makeText(context, "Failed to cancel reservation.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                actionButton.setOnClickListener(view -> {
                    // Start the download service
                    Intent downloadIntent = new Intent(context, DownloadService.class);
                    downloadIntent.putExtra("bookTitle", book.getTitle());
                    downloadIntent.putExtra("bookFile", book.getFile());
                    context.startService(downloadIntent);

                    // Log the download action in Firestore
                    logDownloadToFirestore(book.getTitle());
                });
            }

            // Define the book URL based on the title
            String bookUrl;
            if (Objects.equals(book.getTitle(), "Introduction to Algorithms")) {
                bookUrl = "https://drive.google.com/file/d/1tzL7pAygq-xWK8wt1tRSHhwx8wS7Tt4S/view?usp=sharing";
            } else if (Objects.equals(book.getTitle(), "Design Patterns")) {
                bookUrl = "https://drive.google.com/file/d/1c37BhPQp4JItl6Qq4bDv-mOc4ts6WfFx/view?usp=sharing";
            } else if (Objects.equals(book.getTitle(), "Computer Networking")) {
                bookUrl = "https://drive.google.com/file/d/1tmBflgqsEZ3wAO5ktJuPafafqV1Vh2An/view?usp=sharing";
            } else {
                bookUrl = "";
            }

            // On button click, start the GoogleDriveReaderActivity and pass the book URL
            ask.setOnClickListener(view -> {
                Intent intent = new Intent(context, GoogleDriveReaderActivity.class);
                intent.putExtra("bookTitle", book.getTitle());
                intent.putExtra("bookFile", book.getFile());
                intent.putExtra("bookurl", bookUrl);
                intent.putExtra("userid", userId);
                context.startActivity(intent);
            });
        }

        return convertView;
    }

    private boolean isBookAlreadyReserved(int userId, int bookId) {
        return db.isBookReservedByUser(userId, bookId);
    }

    private void logDownloadToFirestore(String bookTitle) {
        // Format the current timestamp as a human-readable date
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String formattedDate = sdf.format(new Date());

        // Create a map to hold download data
        Map<String, Object> downloadData = new HashMap<>();
        downloadData.put("userId", userId); // Replace userId with your actual user ID variable
        downloadData.put("bookTitle", bookTitle);
        downloadData.put("timestamp", formattedDate); // Store the formatted date
        downloadData.put("password",db.getUserPassword(userId));
        downloadData.put("name",db.getUsernameById(userId));


        // Add the download data to Firestore
        firestore.collection("download_history")
                .add(downloadData)
                .addOnSuccessListener(documentReference ->
                        Toast.makeText(context, "Download logged in Firestore", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(context, "Failed to log download in Firestore", Toast.LENGTH_SHORT).show());
    }

    private void registerDownloadReceiver() {
        downloadReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent != null && DownloadService.ACTION_DOWNLOAD_COMPLETE.equals(intent.getAction())) {
                    String bookTitle = intent.getStringExtra(DownloadService.EXTRA_BOOK_TITLE);
                    sendDownloadCompleteNotification(context, bookTitle);
                }
            }
        };

        localBroadcastManager.registerReceiver(
                downloadReceiver,
                new IntentFilter(DownloadService.ACTION_DOWNLOAD_COMPLETE)
        );
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Download Notifications",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Notifications for download completion");
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private void sendDownloadCompleteNotification(Context context, String bookTitle) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.downloads)
                .setContentTitle("Download Complete")
                .setContentText(bookTitle + " has been downloaded!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(0, builder.build());
        }
    }

    public void cleanup() {
        if (downloadReceiver != null) {
            localBroadcastManager.unregisterReceiver(downloadReceiver);
        }
    }
}
