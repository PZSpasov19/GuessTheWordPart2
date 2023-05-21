package com.example.guesstheword;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;

public class GuessTheWordGame extends Application {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/USERS";
    private static final String DB_USERNAME = "your_username";
    private static final String DB_PASSWORD = "your_password";

    private static Connection connection;
    private static String[] words;
    private static int currentWordIndex = 0;
    private static int remainingTrials = 10;

    public static void main(String[] args) {
        // setting the database
        establishDBConnection();

        // Reading the words from the text file
        words = readWordsFromFile("words.txt");

        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        // Creating login form
        Label usernameLabel = new Label("Username:");
        TextField usernameField = new TextField();

        Label passwordLabel = new Label("Password:");
        PasswordField passwordField = new PasswordField();

        Button loginButton = new Button("Login");
        loginButton.setOnAction(event -> {
            String username = usernameField.getText();
            String password = passwordField.getText();

            // Validating the user information
            if (validateUserCredentials(username, password)) {
                // Starting the GuessTheWord game
                startGuessTheWordGame(primaryStage);
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Login Failed");
                alert.setHeaderText(null);
                alert.setContentText("Invalid username or password.");
                alert.showAndWait();
            }
        });

        GridPane loginPane = new GridPane();
        loginPane.setPadding(new Insets(10));
        loginPane.setHgap(5);
        loginPane.setVgap(5);
        loginPane.add(usernameLabel, 0, 0);
        loginPane.add(usernameField, 1, 0);
        loginPane.add(passwordLabel, 0, 1);
        loginPane.add(passwordField, 1, 1);
        loginPane.add(loginButton, 1, 2);

        Scene loginScene = new Scene(loginPane, 300, 150);

        primaryStage.setTitle("GuessTheWord - Login");
        primaryStage.setScene(loginScene);
        primaryStage.show();
    }

    private static void establishDBConnection() {
        try {
            connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
            System.out.println("Connected to the database.");
        } catch (SQLException e) {
            System.err.println("Failed to connect to the database.");
            e.printStackTrace();
        }
    }

    private static String[] readWordsFromFile(String filename) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            StringBuilder wordsBuilder = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                wordsBuilder.append(line).append(",");
            }
            String wordsString = wordsBuilder.toString();
            return wordsString.split(",");
        } catch (IOException e) {
            System.err.println("Failed to read words from file: " + filename);
            e.printStackTrace();
            return new String[0];
        }
    }

    private static boolean validateUserCredentials(String username, String password) {
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM USERS WHERE username = ? AND password = ?");
            statement.setString(1, username);
            statement.setString(2, password);
            ResultSet resultSet = statement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            System.err.println("Failed to validate user credentials.");
            e.printStackTrace();
            return false;
        }
    }

    private void startGuessTheWordGame(Stage primaryStage) {
        // Creating game UI
        Label wordLabel = new Label("Guess the word:");
        Label remainingTrialsLabel = new Label("Remaining trials: " + remainingTrials);
        TextField guessField = new TextField();
        Button guessButton = new Button("Guess");

        GridPane gamePane = new GridPane();
        gamePane.setPadding(new Insets(10));
        gamePane.setHgap(5);
        gamePane.setVgap(5);
        gamePane.add(wordLabel, 0, 0);
        gamePane.add(new Label(words[currentWordIndex]), 1, 0);
        gamePane.add(remainingTrialsLabel, 0, 1);
        gamePane.add(guessField, 0, 2);
        gamePane.add(guessButton, 1, 2);

        Scene gameScene = new Scene(gamePane, 300, 150);

        // Handling guess button action
        guessButton.setOnAction(event -> {
            String guess = guessField.getText();
            if (guess.equalsIgnoreCase(words[currentWordIndex])) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Congratulations!");
                alert.setHeaderText(null);
                alert.setContentText("You guessed the word correctly!");
                alert.showAndWait();

                currentWordIndex++;
                if (currentWordIndex >= words.length) {
                    // End of the game
                    primaryStage.close();
                } else {
                    // Reset remaining trials and update word
                    remainingTrials = 10;
                    remainingTrialsLabel.setText("Remaining trials: " + remainingTrials);
                    wordLabel.setText("Guess the word:");
                    guessField.clear();
                    gamePane.getChildren().remove(1);
                    gamePane.add(new Label(words[currentWordIndex]), 1, 0);
                }
            } else {
                remainingTrials--;
                if (remainingTrials <= 0) {
                    // Game over, reveal the word
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Game Over");
                    alert.setHeaderText(null);
                    alert.setContentText("You failed to guess the word. The word was: " + words[currentWordIndex]);
                    alert.showAndWait();

                    currentWordIndex++;
                    if (currentWordIndex >= words.length) {
                        // End of the game
                        primaryStage.close();
                    } else {
                        // Reset remaining trials and update word
                        remainingTrials = 10;
                        remainingTrialsLabel.setText("Remaining trials: " + remainingTrials);
                        wordLabel.setText("Guess the word:");
                        guessField.clear();
                        gamePane.getChildren().remove(1);
                        gamePane.add(new Label(words[currentWordIndex]), 1, 0);
                    }
                } else {
                    // Incorrect guess, update remaining trials
                    remainingTrialsLabel.setText("Remaining trials: " + remainingTrials);
                    wordLabel.setText("Wrong guess! Guess the word:");
                    guessField.clear();
                }
            }
        });

        // Handle ESC key press to stop the game
        gameScene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                primaryStage.close();
            }
        });

        primaryStage.setTitle("GuessTheWord - Game");
        primaryStage.setScene(gameScene);
        primaryStage.show();
    }
}