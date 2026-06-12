package com.app.mydashboard;

import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class DownloadService extends Service {

    // Action personnalisée pour notifier la fin du téléchargement
    public static final String ACTION_DOWNLOAD_COMPLETE = "com.app.mydashboard.ACTION_DOWNLOAD_COMPLETE";

    // Clé utilisée pour transmettre le titre du livre via Intent
    public static final String EXTRA_BOOK_TITLE = "book_title";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Récupère le titre du livre depuis l'Intent
        String bookTitle = intent.getStringExtra("bookTitle");
        // Récupère le nom du fichier du livre depuis l'Intent
        String bookFileName = intent.getStringExtra("bookFile");

        // Log pour vérifier les données reçues
        Log.d("DownloadService", "Downloading: " + bookTitle);

        // Lancement d'un nouveau thread pour exécuter la copie du fichier
        new Thread(() -> {
            try {
                // Copie le fichier du dossier assets vers le dossier Téléchargements
                copyBookToDownloads(bookFileName);

                // Prépare une Intent pour notifier que le téléchargement est terminé
                Intent broadcastIntent = new Intent(ACTION_DOWNLOAD_COMPLETE);
                // Ajoute le titre du livre dans l'Intent
                broadcastIntent.putExtra(EXTRA_BOOK_TITLE, bookTitle);
                // Envoie une diffusion locale pour notifier les autres composants
                LocalBroadcastManager.getInstance(DownloadService.this).sendBroadcast(broadcastIntent);

            } catch (IOException e) {
                // Affiche la pile d'erreurs si une exception se produit
                e.printStackTrace();
            }
        }).start();

        // Indique que le service ne doit pas redémarrer automatiquement si Android le termine
        return START_NOT_STICKY;
    }

    private void copyBookToDownloads(String fileName) throws IOException {
        // Récupère le répertoire public des téléchargements
        File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

        // Vérifie si le répertoire Téléchargements existe, sinon le crée
        if (!downloadsDir.exists()) {
            downloadsDir.mkdirs();
        }

        // Définit le chemin complet du fichier de sortie dans le répertoire Téléchargements
        File outFile = new File(downloadsDir, fileName);

        // Copie le fichier du dossier assets vers le répertoire de destination
        try (InputStream inputStream = getAssets().open(fileName); // Ouvre le fichier dans les assets
             FileOutputStream outputStream = new FileOutputStream(outFile)) { // Prépare le fichier de destination

            // Tampon pour lire les données du fichier source par morceaux
            byte[] buffer = new byte[1024];
            int length;

            // Lit et écrit les données jusqu'à ce que la fin du fichier soit atteinte
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            // Log pour indiquer que le fichier a été copié avec succès
            Log.d("DownloadService", "File copied to: " + outFile.getAbsolutePath());
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // Ce service ne permet pas de liaison, retourne donc null
        return null;
    }
}
