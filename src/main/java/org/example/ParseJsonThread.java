package org.example;

import java.io.*;
import java.nio.file.*;
import java.sql.*;
import java.util.*;
import org.json.*;

public class ParseJsonThread {

    private static final String INPUT_FILE = "src/main/resources/data/input.json";
    private static final String OUTPUT_FILE = "src/main/resources/data/output.json";
    private static final String ERROR_FILE = "src/main/resources/data/error.json";

    public static void main(String[] args) {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                parseJsonAndProcess();
            }
        }, 0, 3600 * 1000);

    }

    private static void parseJsonAndProcess() {
        // Connexion à la base de données
        Connection connection = DBConnection.getConnection();
        if (connection == null) {
            System.err.println("Erreur : Impossible d'établir une connexion à la base de données.");
            return;
        }

        try {


            // Vérifie si le fichier existe et sa taille
            File inputFile = new File(INPUT_FILE);
            if (!inputFile.exists()) {
                System.out.println("Le fichier input.json n'existe pas !");
                return;
            }


            // Lecture du contenu du fichier
            String inputContent = new String(Files.readAllBytes(Paths.get(INPUT_FILE))).trim();

            // Debug : Afficher le contenu du fichier
           System.out.println("Contenu du fichier input.json : " + inputContent);

            // Vérifie si le fichier est vide
            if (inputContent.isEmpty() || inputContent.equals("[]")) {
                System.out.println("Aucune donnée à traiter dans le fichier input.json.");
                return;
            }

            // Conversion du contenu JSON en tableau d'objets JSON
            JSONArray orders = new JSONArray(inputContent);

            // Tableaux pour stocker les commandes valides et invalides
            JSONArray outputOrders = new JSONArray();
            JSONArray errorOrders = new JSONArray();

            // Parcourt chaque commande dans le fichier d'entrée
            for (int i = 0; i < orders.length(); i++) {
                JSONObject order = orders.getJSONObject(i);

                // Vérifie si le client existe dans la base de données
                if (customerExists(connection, order.getInt("customer_id"))) {
                    // Ajoute la commande valide à la base de données et à la liste des sorties
                    addOrderToDatabase(connection, order);
                    outputOrders.put(order);
                } else {
                    // Ajoute la commande invalide à la liste des erreurs
                    errorOrders.put(order);
                }
            }

            // Écrit les commandes valides dans le fichier de sortie en mode append
            Files.write(Paths.get(OUTPUT_FILE), outputOrders.toString(4).getBytes(), StandardOpenOption.APPEND);

           // Écrit les commandes invalides dans le fichier des erreurs en mode append
            Files.write(Paths.get(ERROR_FILE), errorOrders.toString(4).getBytes(), StandardOpenOption.APPEND);

            // Remplace le contenu par un tableau JSON vide
            Files.write(Paths.get(INPUT_FILE), "[]".getBytes());
            Thread.sleep(60000);


        } catch ( IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                connection.close();
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
    }

    // Vérifie si un client existe dans la base de données
    private static boolean customerExists(Connection connection, int customerId) {
        String query = "SELECT COUNT(*) FROM customer WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, customerId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException se) {
            se.printStackTrace();
        }
        return false;
    }

    // Ajoute une commande valide à la base de données
    private static void addOrderToDatabase(Connection connection, JSONObject order) {
        String query = "INSERT INTO `order` (id, date, amount, customer_id, status) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, order.getInt("id"));
            stmt.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            stmt.setDouble(3, order.getDouble("amount"));
            stmt.setInt(4, order.getInt("customer_id"));
            stmt.setString(5, order.getString("status"));
            stmt.executeUpdate();
        } catch (SQLException se) {
            se.printStackTrace();
        }
    }
}
