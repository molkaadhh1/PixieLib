package com.app.mydashboard;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private DatabaseHelper db;
    private FirebaseAuth mAuth; // Firebase Authentication
    private FirebaseFirestore firestore; // Instance pour interagir avec la base de données SQLite

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Log.d(TAG, "onCreate: Activity started");

        db = new DatabaseHelper(this);
        try {
            mAuth = FirebaseAuth.getInstance();
            firestore = FirebaseFirestore.getInstance();
            Log.d(TAG, "onCreate: Firebase initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "onCreate: Firebase initialization failed", e);
        }

        // Retrieve data from Firestore (example, not necessary for login functionality)
        if (firestore != null) {
            firestore.collection("test").get()
                    .addOnSuccessListener(queryDocumentSnapshots -> Log.d(TAG, "Firestore test query: Data retrieved successfully"))
                    .addOnFailureListener(e -> Log.e(TAG, "Firestore test query: Error retrieving data", e));
        } else {
            Log.w(TAG, "onCreate: Firestore reference is null, skipping test query");
        }

        EditText etUsername = findViewById(R.id.editTextNames);
        EditText etPassword = findViewById(R.id.editTextPassword);
        Button btnLogin = findViewById(R.id.buttonLogin);
        TextView tvSignUp = findViewById(R.id.textSignUpPrompt);

        btnLogin.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            Log.d(TAG, "btnLogin.onClick: Attempting login for username: " + username);

            if (username.isEmpty() || password.isEmpty()) {
                Log.w(TAG, "btnLogin.onClick: Username or password empty");
                Toast.makeText(LoginActivity.this, "Please enter both username and password", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean isUserValid = db.checkUser(username, password);
            Log.d(TAG, "btnLogin.onClick: SQLite credential check = " + isUserValid);

            if (isUserValid) {
                int userId = db.getUserId(username);
                Log.d(TAG, "btnLogin.onClick: SQLite userId fetched = " + userId);

                if (userId != -1) {
                    Toast.makeText(LoginActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();

                    String role = db.getUserRole(userId);
                    Intent intent;
                    if ("admin".equalsIgnoreCase(role)) {
                        intent = new Intent(LoginActivity.this, AdminActivity.class);
                        Log.d(TAG, "btnLogin.onClick: Navigating to AdminActivity");
                    } else {
                        intent = new Intent(LoginActivity.this, MainActivity.class);
                        Log.d(TAG, "btnLogin.onClick: Navigating to MainActivity");
                    }
                    intent.putExtra("username", username);
                    intent.putExtra("user_id", userId);
                    startActivity(intent);

                    storeUserDataInFirebase(userId, username, password); // Store user data including password in Firebase
                    finish(); // Close the login activity
                } else {
                    Log.e(TAG, "btnLogin.onClick: Error: Unable to retrieve user ID for " + username);
                    Toast.makeText(LoginActivity.this, "Error: Unable to retrieve user data", Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.w(TAG, "btnLogin.onClick: Invalid credentials for username: " + username);
                Toast.makeText(LoginActivity.this, "Invalid credentials, please try again", Toast.LENGTH_SHORT).show();
            }
        });

        tvSignUp.setOnClickListener(v -> {
            Log.d(TAG, "tvSignUp.onClick: Navigating to SignUpActivity");
            Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
            startActivity(intent);
        });
    }

    private void storeUserDataInFirebase(int userId, String username, String password) {
        if (firestore == null) {
            Log.w(TAG, "storeUserDataInFirebase: Firestore instance is null, skipping remote sync");
            return;
        }

        Log.d(TAG, "storeUserDataInFirebase: Syncing user " + username + " (ID: " + userId + ") with Firestore");
        // Create a map to hold user data including password
        Map<String, Object> userData = new HashMap<>();
        userData.put("userId", userId);
        userData.put("username", username);
        userData.put("password", password); // Store the password as well (not secure!)

        // Store the data in Firestore under the "users" collection
        firestore.collection("users")
                .document(String.valueOf(userId)) // Use userId as the document ID
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "storeUserDataInFirebase: User data sync succeeded in Firestore");
                    Toast.makeText(LoginActivity.this, "User data saved in Firebase", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "storeUserDataInFirebase: User data sync failed in Firestore", e);
                    Toast.makeText(LoginActivity.this, "Failed to save user data in Firebase", Toast.LENGTH_SHORT).show();
                });
    }
}
