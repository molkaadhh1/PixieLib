package com.app.mydashboard; // Déclare le package de l'application

// Importe les classes nécessaires
import static androidx.core.content.ContextCompat.startActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.util.Log;

public class WelcomeActivity extends AppCompatActivity {

    private static final String TAG = "WelcomeActivity";
    // Déclare la barre de progression
    private ProgressBar progressBar;

    // Déclare un TextView pour afficher le pourcentage de progression
    private TextView progressText;

    // Variable pour suivre la progression actuelle (0-100)
    private int progress = 0;

    // Handler pour exécuter des tâches sur le thread principal
    private Handler handler;

    // Booléen pour vérifier si l'activité est en pause
    private boolean isPaused = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Load theme settings before setting view
        android.content.SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        boolean isDarkMode = prefs.getBoolean("dark_mode", false);
        Log.d(TAG, "onCreate: Theme loaded, isDarkMode = " + isDarkMode);
        if (isDarkMode) {
            androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO);
        }

        super.onCreate(savedInstanceState); // Appelle la méthode parent pour initialiser l'activité
        setContentView(R.layout.activity_welcome); // Définit la vue associée à cette activité
        Log.d(TAG, "onCreate: Content view set, starting animations and progress simulation");

        // Initialise la barre de progression depuis le fichier de mise en page
        progressBar = findViewById(R.id.progress_bar);

        // Initialise le TextView pour afficher le pourcentage
        progressText = findViewById(R.id.progress_text);

        // Find animatable views
        android.view.View logoCard = findViewById(R.id.logo_card);
        android.view.View appTitle = findViewById(R.id.app_title);
        android.view.View appSubtitle = findViewById(R.id.app_subtitle);

        // Prepare and play animations
        if (logoCard != null && appTitle != null && appSubtitle != null) {
            logoCard.setAlpha(0f);
            appTitle.setAlpha(0f);
            appSubtitle.setAlpha(0f);

            logoCard.animate().alpha(1f).setDuration(800).start();
            appTitle.animate().alpha(1f).setDuration(800).setStartDelay(200).start();
            appSubtitle.animate().alpha(1f).setDuration(800).setStartDelay(400).start();
        }

        // Initialise le Handler pour gérer les actions retardées
        handler = new Handler(Looper.getMainLooper());

        // Démarre la simulation de progression
        simulateProgress();
    }

    private void simulateProgress() {
        Log.d(TAG, "simulateProgress: Start progress simulation");
        // Exécute une tâche répétée avec un délai
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isPaused) { // Vérifie si l'activité n'est pas en pause
                    progress++; // Incrémente la progression
                    progressBar.setProgress(progress); // Met à jour la barre de progression
                    progressText.setText(progress + "%"); // Met à jour le texte du pourcentage

                    if (progress % 20 == 0 || progress == 100) {
                        Log.d(TAG, "simulateProgress: Current progress = " + progress + "%");
                    }

                    if (progress < 100) { // Si la progression est inférieure à 100
                        handler.postDelayed(this, 50); // Réexécute cette tâche après 50ms
                    } else {
                        goToLogin(); // Une fois à 100%, passe à l'écran de connexion
                    }
                }
            }
        }, 50); // Délai initial de 50ms
    }

    private void goToLogin() {
        Log.d(TAG, "goToLogin: Navigation triggered. Starting LoginActivity");
        // Crée une intention pour démarrer l'activité de connexion
        Intent intent = new Intent(WelcomeActivity.this, LoginActivity.class);

        // Démarre l'activité de connexion
        startActivity(intent);

        // Termine l'activité actuelle
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause(); // Appelle la méthode parent
        isPaused = true; // Met à jour l'état pour indiquer que l'activité est en pause
    }

    @Override
    protected void onResume() {
        super.onResume(); // Appelle la méthode parent
        if (isPaused) { // Si l'activité était en pause
            isPaused = false; // Réinitialise l'état pour indiquer que l'activité reprend
            simulateProgress(); // Relance la simulation de progression
        }
    }
}
