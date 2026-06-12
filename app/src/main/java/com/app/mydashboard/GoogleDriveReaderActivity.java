package com.app.mydashboard;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;

public class GoogleDriveReaderActivity extends AppCompatActivity {
    private String bookTitle;
    private String bookFileName;
    private String bookFileUrl;  // Holds the URL of the PDF in Google Drive
    private int lastPage = 0; // Default to page 0
    private int userId; // User ID to differentiate progress
    private FirebaseFirestore firestore;

    private PdfRenderer pdfRenderer;
    private PdfRenderer.Page currentPage;
    private ParcelFileDescriptor fileDescriptor;
    private File tempFile;

    private ImageView pageImageView;
    private TextView pageIndicator;
    private androidx.appcompat.widget.AppCompatButton btnPrev;
    private androidx.appcompat.widget.AppCompatButton btnNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_drive_reader);

        firestore = FirebaseFirestore.getInstance();

        // Get book details from the intent
        bookTitle = getIntent().getStringExtra("bookTitle");
        bookFileName = getIntent().getStringExtra("bookFile");
        bookFileUrl = getIntent().getStringExtra("bookurl");  // Get the book URL
        userId = getIntent().getIntExtra("userid", -1);  // Get user ID

        if (userId == -1) {
            Toast.makeText(this, "Invalid user", Toast.LENGTH_SHORT).show();
            finish();  // Exit if no user ID is provided
            return;
        }

        // Setup Toolbar
        TextView tvTitle = findViewById(R.id.reader_book_title);
        if (tvTitle != null) {
            tvTitle.setText(bookTitle != null ? bookTitle : "Book Reader");
        }

        ImageView btnClose = findViewById(R.id.btn_close_reader);
        if (btnClose != null) {
            btnClose.setOnClickListener(v -> finish());
        }

        // Setup views
        pageImageView = findViewById(R.id.pdf_page_image);
        pageIndicator = findViewById(R.id.reader_page_indicator);
        btnPrev = findViewById(R.id.btn_prev_page);
        btnNext = findViewById(R.id.btn_next_page);

        // Setup share/drive backup buttons
        ImageView btnDriveBackup = findViewById(R.id.btn_drive_backup);
        if (btnDriveBackup != null) {
            btnDriveBackup.setOnClickListener(v -> openPdfInGoogleDrive(bookFileUrl));
        }

        TextView textOpenGoogleDrive = findViewById(R.id.text_open_google_drive);
        if (textOpenGoogleDrive != null) {
            textOpenGoogleDrive.setOnClickListener(v -> openPdfInGoogleDrive(bookFileUrl));
        }

        // Setup page controls
        if (btnPrev != null) {
            btnPrev.setOnClickListener(v -> {
                if (lastPage > 0) {
                    showPage(lastPage - 1);
                    saveLastReadPage();
                }
            });
        }

        if (btnNext != null) {
            btnNext.setOnClickListener(v -> {
                if (pdfRenderer != null && lastPage < pdfRenderer.getPageCount() - 1) {
                    showPage(lastPage + 1);
                    saveLastReadPage();
                }
            });
        }

        // Load asset PDF and start rendering
        try {
            copyAssetToTempFile(bookFileName);
            // Load progress and display page
            loadLastReadPage(() -> {
                initPdfRenderer();
                showPage(lastPage);
            });
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error copying book file from assets", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void copyAssetToTempFile(String assetName) throws IOException {
        tempFile = new File(getCacheDir(), assetName);
        if (!tempFile.exists()) {
            InputStream in = getAssets().open(assetName);
            OutputStream out = new FileOutputStream(tempFile);
            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            out.flush();
            out.close();
        }
    }

    private void initPdfRenderer() {
        try {
            fileDescriptor = ParcelFileDescriptor.open(tempFile, ParcelFileDescriptor.MODE_READ_ONLY);
            if (fileDescriptor != null) {
                pdfRenderer = new PdfRenderer(fileDescriptor);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to load document view", Toast.LENGTH_SHORT).show();
        }
    }

    private void showPage(int pageIndex) {
        if (pdfRenderer == null) return;

        // Check bounds
        if (pageIndex < 0 || pageIndex >= pdfRenderer.getPageCount()) {
            pageIndex = 0;
        }

        // Close current page
        if (currentPage != null) {
            currentPage.close();
        }

        try {
            currentPage = pdfRenderer.openPage(pageIndex);

            // Render page on bitmap
            int width = currentPage.getWidth();
            int height = currentPage.getHeight();

            // Set rendering scale
            float scale = 1.5f;
            int bitmapWidth = (int) (width * scale);
            int bitmapHeight = (int) (height * scale);

            // Check memory safe limits
            if (bitmapWidth > 2048 || bitmapHeight > 2048) {
                float ratio = Math.min(2048f / bitmapWidth, 2048f / bitmapHeight);
                bitmapWidth = (int) (bitmapWidth * ratio);
                bitmapHeight = (int) (bitmapHeight * ratio);
            }

            Bitmap bitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888);
            currentPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);

            if (pageImageView != null) {
                pageImageView.setImageBitmap(bitmap);
            }

            if (pageIndicator != null) {
                pageIndicator.setText("Page " + (pageIndex + 1) + " of " + pdfRenderer.getPageCount());
            }

            if (btnPrev != null) btnPrev.setEnabled(pageIndex > 0);
            if (btnNext != null) btnNext.setEnabled(pageIndex < pdfRenderer.getPageCount() - 1);

            lastPage = pageIndex;

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error rendering page", Toast.LENGTH_SHORT).show();
        }
    }

    private void openPdfInGoogleDrive(String googleDriveUrl) {
        if (googleDriveUrl != null && !googleDriveUrl.isEmpty()) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(googleDriveUrl));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try {
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, "No browser or Drive app available", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Drive link not available for this book", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadLastReadPage(Runnable onLoaded) {
        // Fetch the last page read from Firestore for the specific user and book
        firestore.collection("reading_progress")
                .document(getUserBookProgressDocumentId())
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists() && document.contains("lastPage")) {
                        lastPage = document.getLong("lastPage").intValue();
                    }
                    onLoaded.run();
                })
                .addOnFailureListener(e -> onLoaded.run());
    }

    private String getUserBookProgressDocumentId() {
        // Use the user ID and book title to create a unique document ID
        return userId + "_" + bookTitle; // Example: "1_Introduction to Algorithms"
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        saveLastReadPage(); // Save the current progress when the activity is destroyed
        closeRenderer();
    }

    private void saveLastReadPage() {
        // Save the progress to Firestore for the specific user and book
        HashMap<String, Object> progressData = new HashMap<>();
        progressData.put("lastPage", lastPage);

        firestore.collection("reading_progress")
                .document(getUserBookProgressDocumentId())
                .set(progressData);
    }

    private void closeRenderer() {
        try {
            if (currentPage != null) {
                currentPage.close();
            }
            if (pdfRenderer != null) {
                pdfRenderer.close();
            }
            if (fileDescriptor != null) {
                fileDescriptor.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (tempFile != null && tempFile.exists()) {
            tempFile.delete();
        }
    }

    // Call this method to update the current page in the `lastPage` variable
    public void updateProgress(int page) {
        lastPage = page;
    }
}
