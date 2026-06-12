package com.app.mydashboard;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "pixieLibb.db";
    private static final int DATABASE_VERSION = 13;

    // Table des utilisateurs
    private static final String TABLE_USERS = "users";
    private static final String COLUMN_USER_ID = "id";
    private static final String COLUMN_USERNAME = "username";
    private static final String COLUMN_PASSWORD = "password";

    // Table des livres
    private static final String TABLE_BOOKS = "books";
    private static final String COLUMN_BOOK_ID = "id";
    private static final String COLUMN_BOOK_TITLE = "title";
    private static final String COLUMN_BOOK_AUTHOR = "author";
    private static final String COLUMN_BOOK_COVER = "cover"; // Image de couverture
    private static final String COLUMN_BOOK_FILE = "file"; // Fichier PDF
    private static final String COLUMN_BOOK_CATEGORY = "category"; // Catégorie du livre

    // Table des réservations
    private static final String TABLE_RESERVATIONS = "reservations";
    private static final String COLUMN_RESERVATION_ID = "id";
    private static final String COLUMN_RESERVATION_USER_ID = "user_id";
    private static final String COLUMN_RESERVATION_BOOK_ID = "book_id";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Créer la table des utilisateurs
        db.execSQL("CREATE TABLE " + TABLE_USERS + " (" +
                COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_USERNAME + " TEXT UNIQUE, " +
                COLUMN_PASSWORD + " TEXT, " +
                "role TEXT DEFAULT 'user')");

        // Créer la table des livres
        db.execSQL("CREATE TABLE " + TABLE_BOOKS + " (" +
                COLUMN_BOOK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_BOOK_TITLE + " TEXT, " +
                COLUMN_BOOK_AUTHOR + " TEXT, " +
                COLUMN_BOOK_COVER + " TEXT, " +
                COLUMN_BOOK_FILE + " TEXT, " +
                COLUMN_BOOK_CATEGORY + " TEXT, " +
                "available INTEGER DEFAULT 1)");

        // Add initial books with category
        db.execSQL("INSERT INTO " + TABLE_BOOKS + " (" +
                COLUMN_BOOK_TITLE + ", " +
                COLUMN_BOOK_AUTHOR + ", " +
                COLUMN_BOOK_COVER + ", " +
                COLUMN_BOOK_FILE + ", " +
                COLUMN_BOOK_CATEGORY + ", available) VALUES ('Artificial Intelligence: A Modern Approach', 'Andreson Coen', 'iaapp', 'Artificial_Intelligence_a_Modern_Approach__-_Anderson_Coen.pdf', 'AI & Networking', 1)");
        db.execSQL("INSERT INTO " + TABLE_BOOKS + " (" +
                COLUMN_BOOK_TITLE + ", " +
                COLUMN_BOOK_AUTHOR + ", " +
                COLUMN_BOOK_COVER + ", " +
                COLUMN_BOOK_FILE + ", " +
                COLUMN_BOOK_CATEGORY + ", available) VALUES ('Clean Code', 'Robert C. Martin', 'clean_code', 'Clean_Code__A_Handbook_of_Agile_Software_C_-_Robert_C_Martin.pdf', 'Development', 1)");
        db.execSQL("INSERT INTO " + TABLE_BOOKS + " (" +
                COLUMN_BOOK_TITLE + ", " +
                COLUMN_BOOK_AUTHOR + ", " +
                COLUMN_BOOK_COVER + ", " +
                COLUMN_BOOK_FILE + ", " +
                COLUMN_BOOK_CATEGORY + ", available) VALUES ('Inroduction to Algorithms', 'Thomas H. Cormen', 'intro_algo', 'Introduction_to_Algorithms_fourth_edition_-_Thomas_H_Cormen.pdf', 'Development', 1)");
        db.execSQL("INSERT INTO " + TABLE_BOOKS + " (" +
                COLUMN_BOOK_TITLE + ", " +
                COLUMN_BOOK_AUTHOR + ", " +
                COLUMN_BOOK_COVER + ", " +
                COLUMN_BOOK_FILE + ", " +
                COLUMN_BOOK_CATEGORY + ", available) VALUES ('Computer Networking', 'Scott Russell', 'computer_networking', 'Computer_Networking_-_Scott_Russell.pdf', 'AI & Networking', 1)");
        db.execSQL("INSERT INTO " + TABLE_BOOKS + " (" +
                COLUMN_BOOK_TITLE + ", " +
                COLUMN_BOOK_AUTHOR + ", " +
                COLUMN_BOOK_COVER + ", " +
                COLUMN_BOOK_FILE + ", " +
                COLUMN_BOOK_CATEGORY + ", available) VALUES ('Design Patterns', 'Erich Gamma', 'design_pattern', 'Design_Patterns__Elements_of_Reusable_Obje_-_Erich_Gamma.pdf', 'Software Architecture', 1)");


        // Créer la table des réservations
        db.execSQL("CREATE TABLE " + TABLE_RESERVATIONS + " (" +
                COLUMN_RESERVATION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_RESERVATION_USER_ID + " INTEGER, " +
                COLUMN_RESERVATION_BOOK_ID + " INTEGER, " +
                "status TEXT DEFAULT 'Pending', " +
                "confirmation_date TEXT, " +
                "FOREIGN KEY(" + COLUMN_RESERVATION_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_ID + "), " +
                "FOREIGN KEY(" + COLUMN_RESERVATION_BOOK_ID + ") REFERENCES " + TABLE_BOOKS + "(" + COLUMN_BOOK_ID + "))");

        // Seed default admin account
        db.execSQL("INSERT INTO " + TABLE_USERS + " (username, password, role) VALUES ('admin', 'admin', 'admin')");

        // Enable foreign key constraints
        db.execSQL("PRAGMA foreign_keys=ON;");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BOOKS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RESERVATIONS);
        onCreate(db);
    }

    // Ajouter un utilisateur (Reader by default)
    public boolean addUser(String username, String password) {
        return addUser(username, password, "user");
    }

    // Ajouter un utilisateur avec un rôle spécifié
    public boolean addUser(String username, String password, String role) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_PASSWORD, password);
        values.put("role", role);
        long result = db.insert(TABLE_USERS, null, values);
        return result != -1;
    }

    // Vérifier les informations de connexion
    public boolean checkUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USERS + " WHERE " +
                COLUMN_USERNAME + " = ? AND " + COLUMN_PASSWORD + " = ?", new String[]{username, password});
        boolean result = cursor.getCount() > 0;
        cursor.close();
        return result;
    }

    // Obtenir tous les livres
    public Cursor getBooks() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_BOOKS, null);
    }

    // Ajouter une réservation
    public boolean addReservation(int userId, int bookId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_RESERVATION_USER_ID, userId);
        values.put(COLUMN_RESERVATION_BOOK_ID, bookId);
        long result = db.insert(TABLE_RESERVATIONS, null, values);
        return result != -1;
    }

    // Supprimer une réservation
    public boolean deleteReservation(int userId, int bookId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_RESERVATIONS, COLUMN_RESERVATION_USER_ID + " = ? AND " + COLUMN_RESERVATION_BOOK_ID + " = ?",
                new String[]{String.valueOf(userId), String.valueOf(bookId)});
        return result > 0;
    }

    // Obtenir les livres réservés par un utilisateur
    public Cursor getReservedBooks(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT b.id, b.title, b.author, b.cover, b.file, b.category, b.available, r.status, r.confirmation_date " +
                " FROM books b INNER JOIN reservations r ON b.id = r.book_id " +
                " WHERE r.user_id = ?", new String[]{String.valueOf(userId)});
    }
    // Check if the book is already reserved by the user
    public boolean isBookReservedByUser(int userId, int bookId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_RESERVATIONS + " WHERE " +
                        COLUMN_RESERVATION_USER_ID + " = ? AND " + COLUMN_RESERVATION_BOOK_ID + " = ?",
                new String[]{String.valueOf(userId), String.valueOf(bookId)});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }
    public int getUserId(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT id FROM users WHERE username = ?";
        Cursor cursor = db.rawQuery(query, new String[]{username});

        if (cursor != null && cursor.moveToFirst()) {
            int userId = cursor.getInt(0); // Assuming "id" is the first column
            cursor.close();
            return userId;
        }

        if (cursor != null) {
            cursor.close();
        }
        return -1; // Return -1 if user not found
    }
    @SuppressLint("Range")
    public String getPdfFileName(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();

        // Query to get the pdf_file_name for the user's reserved book(s)
        String query = "SELECT pdf_file_name FROM reserved_books WHERE user_id = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});

        String pdfFileName = null;

        if (cursor != null && cursor.moveToFirst()) {
            // Get the pdf file name from the result
            pdfFileName = cursor.getString(cursor.getColumnIndex("pdf_file_name"));
            cursor.close();
        }

        return pdfFileName;
    }
    public int getReservedBookCount(int userId) {
        int count = 0;
        SQLiteDatabase db = this.getReadableDatabase();

        // Correct the table and column names in the SQL query
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_RESERVATIONS +
                        " WHERE " + COLUMN_RESERVATION_USER_ID + " = ?",
                new String[]{String.valueOf(userId)});
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }
    @SuppressLint("Range")
    public String getUserPassword(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + COLUMN_PASSWORD + " FROM " + TABLE_USERS + " WHERE " + COLUMN_USER_ID + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});

        String password = null;

        if (cursor != null && cursor.moveToFirst()) {
            password = cursor.getString(cursor.getColumnIndex(COLUMN_PASSWORD));
            cursor.close();
        }

        return password;
    }
    @SuppressLint("Range")
    public String getUsernameById(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + COLUMN_USERNAME + " FROM " + TABLE_USERS + " WHERE " + COLUMN_USER_ID + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});

        String username = null;
        if (cursor != null && cursor.moveToFirst()) {
            // Get the username from the result
            username = cursor.getString(cursor.getColumnIndex(COLUMN_USERNAME));
            cursor.close();
        }

        return username;
    }

    @SuppressLint("Range")
    public String getUserRole(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT role FROM users WHERE id = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});
        String role = "user";
        if (cursor != null && cursor.moveToFirst()) {
            role = cursor.getString(cursor.getColumnIndex("role"));
            cursor.close();
        }
        return role;
    }

    public boolean addBook(String title, String author, String cover, String file, String category, int available) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("title", title);
        values.put("author", author);
        values.put("cover", cover);
        values.put("file", file);
        values.put("category", category);
        values.put("available", available);
        long result = db.insert("books", null, values);
        return result != -1;
    }

    public boolean updateBook(int id, String title, String author, String cover, String file, String category, int available) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("title", title);
        values.put("author", author);
        values.put("cover", cover);
        values.put("file", file);
        values.put("category", category);
        values.put("available", available);
        int result = db.update("books", values, "id = ?", new String[]{String.valueOf(id)});
        return result > 0;
    }

    public boolean deleteBook(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("reservations", "book_id = ?", new String[]{String.valueOf(id)});
        int result = db.delete("books", "id = ?", new String[]{String.valueOf(id)});
        return result > 0;
    }

    public Cursor getAllReservations() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT r.id, r.user_id, u.username, r.book_id, b.title, r.status, r.confirmation_date " +
                " FROM reservations r " +
                " INNER JOIN users u ON r.user_id = u.id " +
                " INNER JOIN books b ON r.book_id = b.id " +
                " ORDER BY r.id DESC", null);
    }

    public boolean confirmReservation(int reservationId, String confirmationDate) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("status", "Confirmed");
        values.put("confirmation_date", confirmationDate);
        int result = db.update("reservations", values, "id = ?", new String[]{String.valueOf(reservationId)});
        return result > 0;
    }

    public boolean deleteReservationById(int reservationId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete("reservations", "id = ?", new String[]{String.valueOf(reservationId)});
        return result > 0;
    }
}