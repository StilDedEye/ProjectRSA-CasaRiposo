package com.drimtim.projectrsacasariposo.sockets.Utilities;

import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;

import java.net.URL;
import java.nio.file.Paths;
import java.util.*;

public class Utilities {
    private static final String pathToScSound = "/com/drimtim/projectrsacasariposo/sounds/notificaSc.mp3";
    private static final String pathToWhatsappSound = "/com/drimtim/projectrsacasariposo/sounds/notificaWhatsapp.mp3";
    private static final String pathToOuterWilds = "/com/drimtim/projectrsacasariposo/sounds/notificaOuter.mp3";
    private static final String pathToBatteriaSound = "/com/drimtim/projectrsacasariposo/sounds/notificaBatteria.mp3";



    public static List<Color> getDominantColors(Image image, int numColors) {
        // Leggi i pixel dall'immagine
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        PixelReader pixelReader = image.getPixelReader();

        Map<Color, Integer> colorCountMap = new HashMap<>();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color color = pixelReader.getColor(x, y);
                colorCountMap.put(color, colorCountMap.getOrDefault(color, 0) + 1);
            }
        }
        // Ordina i colori per occorrenza
        List<Map.Entry<Color, Integer>> sortedColors = new ArrayList<>(colorCountMap.entrySet());
        sortedColors.sort((entry1, entry2) -> entry2.getValue().compareTo(entry1.getValue()));

        // Restituisci i colori dominanti
        List<Color> dominantColors = new ArrayList<>();
        for (int i = 0; i < numColors && i < sortedColors.size(); i++) {
            dominantColors.add(sortedColors.get(i).getKey());
        }
        return dominantColors;
    }

    public static String colorToHex(Color color) {
        int red = (int) (color.getRed() * 255);
        int green = (int) (color.getGreen() * 255);
        int blue = (int) (color.getBlue() * 255);
        int alpha = (int) (color.getOpacity() * 255);
        return String.format("#%02X%02X%02X%02X", red, green, blue, alpha);
    }

    public static int getNumberFromUsername(String word) {
        // Calcola un hash e lo converte in un numero nell'intervallo 1-16
        int hash = Math.abs(word.hashCode()); // valore assouluto perchè potrebbe essere negativo
        return ((hash) % 16) + 1;  // resto divisione. con +1 il range va da 1 a 16
    }

    public static void playSound (String message) {
        new Thread(() -> {
            Platform.runLater(() -> {
                try {
                    // Load the MP3 file as a resource
                    String mp3FilePath = Objects.requireNonNull(Utilities.class.getResource(pathToOuterWilds)).toExternalForm();
                    if (message.equals("sc"))
                        mp3FilePath = Objects.requireNonNull(Utilities.class.getResource(pathToScSound)).toExternalForm();
                    else if (message.equals("batteria"))
                        mp3FilePath = Objects.requireNonNull(Utilities.class.getResource(pathToBatteriaSound)).toExternalForm();

                    // Create Media and MediaPlayer objects
                    Media media = new Media(mp3FilePath);
                    MediaPlayer mediaPlayer = new MediaPlayer(media);

                    // Play sound
                    mediaPlayer.play();
                } catch (NullPointerException e) {}
            });
        }).start();

    }
}
