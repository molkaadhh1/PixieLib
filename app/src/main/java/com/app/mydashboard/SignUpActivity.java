package com.app.mydashboard; // Déclare le package de l'application

// Importe les classes nécessaires
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;

public class SignUpActivity extends AppCompatActivity {

    private static final String TAG = "SignUpActivity";
    // Instance de la classe `DatabaseHelper` pour gérer les opérations de base de données
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); // Appelle la méthode parent pour initialiser l'activité
        setContentView(R.layout.activity_sign_up); // Définit la vue associée à cette activité
        Log.d(TAG, "onCreate: Activity started");

        // Initialise l'objet DatabaseHelper pour accéder à la base de données
        db = new DatabaseHelper(this);

        // Récupère les références des champs d'entrée et du bouton dans le fichier de mise en page
        EditText etNewUsername = findViewById(R.id.editTextName); // Champ pour entrer le nom d'utilisateur
        EditText etNewPassword = findViewById(R.id.editTextPassword); // Champ pour entrer le mot de passe
        Button btnSignUp = findViewById(R.id.buttonSignUp); // Bouton pour s'inscrire
        android.widget.TextView tvLoginLink = findViewById(R.id.textLoginLink);

        // Définit un événement clic pour le lien de connexion
        tvLoginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "tvLoginLink.onClick: Navigating back to LoginActivity");
                Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // Définit un événement clic pour le bouton d'inscription
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Récupère les valeurs saisies dans les champs d'entrée
                String username = etNewUsername.getText().toString().trim(); // Récupère le nom d'utilisateur
                String password = etNewPassword.getText().toString().trim(); // Récupère le mot de passe
                Log.d(TAG, "btnSignUp.onClick: Attempting registration for username: " + username);

                // Vérifie si les champs sont vides
                if (username.isEmpty() || password.isEmpty()) {
                    Log.w(TAG, "btnSignUp.onClick: Empty fields");
                    // Affiche un message pour demander à l'utilisateur de remplir tous les champs
                    Toast.makeText(SignUpActivity.this, "Veuillez remplir tous les champs.", Toast.LENGTH_SHORT).show();
                } else {
                    // Tente d'ajouter un nouvel utilisateur dans la base de données
                    if (db.addUser(username, password, "user")) {
                        Log.d(TAG, "btnSignUp.onClick: Registration successful for " + username + " as user");
                        // Si l'utilisateur a été ajouté avec succès
                        Toast.makeText(SignUpActivity.this, "Inscription réussie !", Toast.LENGTH_SHORT).show();

                        // Crée une intention pour naviguer vers l'écran de connexion (LoginActivity)
                        Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                        startActivity(intent); // Démarre LoginActivity
                        finish();
                    } else {
                        Log.w(TAG, "btnSignUp.onClick: Registration failed, username already exists: " + username);
                        // Si le nom d'utilisateur existe déjà, affiche un message d'erreur
                        Toast.makeText(SignUpActivity.this, "Le nom d'utilisateur existe déjà.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }
}
