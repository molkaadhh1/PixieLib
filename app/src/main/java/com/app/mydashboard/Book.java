package com.app.mydashboard;

public class Book {
    private int id;
    private String title;
    private String author;
    private String cover;
    private String file;
    private String category;
    private int available = 1; // 1 for available, 0 for unavailable
    private String reservationStatus; // "Pending", "Confirmed", etc.
    private String reservationDate; // Confirmation/Pickup date set by admin

    public Book(int id, String title, String author, String cover, String file, String category) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.cover = cover;
        this.file = file;
        this.category = category;
        this.available = 1;
    }

    public Book(int id, String title, String author, String cover, String file, String category, int available) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.cover = cover;
        this.file = file;
        this.category = category;
        this.available = available;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getCover() {
        return cover;
    }

    public String getFile() {
        return file;
    }

    public String getCategory() {
        return category;
    }

    public int getAvailable() {
        return available;
    }

    public void setAvailable(int available) {
        this.available = available;
    }

    public String getReservationStatus() {
        return reservationStatus;
    }

    public void setReservationStatus(String reservationStatus) {
        this.reservationStatus = reservationStatus;
    }

    public String getReservationDate() {
        return reservationDate;
    }

    public void setReservationDate(String reservationDate) {
        this.reservationDate = reservationDate;
    }
}
