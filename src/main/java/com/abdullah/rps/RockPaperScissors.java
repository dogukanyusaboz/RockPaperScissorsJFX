package com.abdullah.rps;

import java.util.List;
import java.util.ArrayList;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.animation.*;
import javafx.util.Duration;
import javafx.scene.control.ListView;
import javafx.scene.layout.Priority;
import javafx.scene.control.Dialog;
import javafx.scene.control.ButtonType;


public class RockPaperScissors extends Application {

    private int playerScore = 0;
    private int computerScore = 0;
    private int currentRound = 1;
    private int doublePointRound;
    private boolean isDarkTheme = false;
    private ListView<String> historyListView;


    private final Random random = new Random();
    private final Map<String, String> userDatabase = new HashMap<>();
    private String currentUser = "";

    private Label scoreLabel, resultLabel, roundLabel;
    private ImageView playerChoiceImageView = new ImageView();
    private ImageView computerChoiceImageView = new ImageView();
    private BorderPane root;
    private Scene scene;
    private HBox imageButtonsBox;
    private Stage primaryStage;

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        loadUserDatabase();
        showLoginScene();
    }

    private void showLoginScene() {
        Label userLabel = new Label("Username:");
        TextField userField = new TextField();
        Label passLabel = new Label("Password:");
        PasswordField passField = new PasswordField();
        Label messageLabel = new Label();
        messageLabel.setTextFill(Color.RED);

        Button loginBtn = new Button("Login");
        Button registerBtn = new Button("Register");

        loginBtn.setOnAction(e -> {
            String user = userField.getText();
            String pass = passField.getText();
            if (userDatabase.containsKey(user) && userDatabase.get(user).equals(pass)) {
                currentUser = user;
                showGameScene();
            } else {
                messageLabel.setText("Wrong username or password!");
            }
        });

        registerBtn.setOnAction(e -> {
            String user = userField.getText();
            String pass = passField.getText();
            if (user.isEmpty() || pass.isEmpty()) {
                messageLabel.setText("Fields cannot be empty!");
            } else if (userDatabase.containsKey(user)) {
                messageLabel.setText("This username is already taken.");
            } else {
                userDatabase.put(user, pass);
                saveUserToFile(user, pass);
                messageLabel.setTextFill(Color.GREEN);
                messageLabel.setText("Register is successful!");
            }
        });

        VBox loginBox = new VBox(10, userLabel, userField, passLabel, passField, loginBtn, registerBtn, messageLabel);
        loginBox.setAlignment(Pos.CENTER);
        loginBox.setPadding(new javafx.geometry.Insets(20));

        Scene loginScene = new Scene(loginBox, 400, 300);
        primaryStage.setTitle("Login/Register Scene");
        primaryStage.setScene(loginScene);
        primaryStage.show();
    }

    private void showGameScene() {
        playerScore = 0;
        computerScore = 0;
        currentRound = 1;
        doublePointRound = random.nextInt(5) + 1;

        scoreLabel = new Label(currentUser + " : 0           AI: 0");
        scoreLabel.setFont(new Font("Arial", 18));
        scoreLabel.setStyle("-fx-font-weight: bold;");

        roundLabel = new Label("💥 2X Point Round: " + doublePointRound);
        roundLabel.setFont(new Font("Arial", 16));

        resultLabel = new Label("Make a choice!");
        resultLabel.setFont(new Font("Arial", 16));

        historyListView = new ListView<>();
        historyListView.setPrefWidth(350);
        historyListView.setPrefHeight(250);


        VBox centerBox = new VBox(15, roundLabel, resultLabel, createChoicesDisplay());
        centerBox.setAlignment(Pos.CENTER);
        centerBox.setMaxWidth(Double.MAX_VALUE);
        VBox.setVgrow(centerBox, Priority.ALWAYS);


        VBox bottomBox = new VBox(15, createImageButtons(), createThemeButton(), createHistoryButton(), createResetButton("New Game"), createLogoutButton());
        bottomBox.setAlignment(Pos.CENTER);
        bottomBox.setPadding(new javafx.geometry.Insets(10, 10, 20, 10));

        root = new BorderPane();
        root.setCenter(centerBox);
        BorderPane.setAlignment(centerBox, Pos.CENTER);

        root.setBottom(bottomBox);
        BorderPane.setAlignment(bottomBox, Pos.CENTER);

        HBox topBox = new HBox(20, scoreLabel);
        topBox.setAlignment(Pos.CENTER);
        topBox.setPadding(new javafx.geometry.Insets(10));
        root.setTop(topBox);
        BorderPane.setAlignment(topBox, Pos.CENTER);


        applyTheme();

        scene = new Scene(root, 700, 650);
        primaryStage.setTitle("Rock Paper Scissors ");
        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();
        primaryStage.show();

    }

    private HBox createChoicesDisplay() {
        VBox playerBox = new VBox(5, new Label("YOU:"), playerChoiceImageView);
        VBox computerBox = new VBox(5, new Label("AI:"), computerChoiceImageView);

        playerBox.setAlignment(Pos.CENTER);
        computerBox.setAlignment(Pos.CENTER);

        playerChoiceImageView.setFitWidth(120);
        playerChoiceImageView.setFitHeight(120);
        playerChoiceImageView.setPreserveRatio(true);

        computerChoiceImageView.setFitWidth(120);
        computerChoiceImageView.setFitHeight(120);
        computerChoiceImageView.setPreserveRatio(true);

        HBox choicesBox = new HBox(40, playerBox, computerBox);
        choicesBox.setAlignment(Pos.CENTER);

        return choicesBox;
    }


    private HBox createImageButtons() {
        Image rockImg = loadImage("rock.png");
        Image paperImg = loadImage("paper.png");
        Image scissorsImg = loadImage("scissors.png");

        Button rockBtn = createGameButton(rockImg, "rock");
        Button paperBtn = createGameButton(paperImg, "paper");
        Button scissorsBtn = createGameButton(scissorsImg, "scissors");

        imageButtonsBox = new HBox(20, rockBtn, paperBtn, scissorsBtn);
        imageButtonsBox.setAlignment(Pos.CENTER);
        return imageButtonsBox;
    }

    private Button createGameButton(Image image, String choice) {
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(80);
        imageView.setFitHeight(80);
        imageView.setPreserveRatio(true);

        Button button = new Button("", imageView);
        button.setOnAction(e -> playGame(choice));
        styleButton(button);
        return button;
    }

    private void styleButton(Button button) {
        String normalStyle;
        String hoverStyle;
        if (isDarkTheme) {
            normalStyle = "-fx-background-color: transparent; -fx-border-radius: 15; -fx-border-color: #555555; -fx-padding: 10;";
            hoverStyle = "-fx-background-color: #4a4a4a; -fx-border-radius: 15; -fx-border-color: #666666; -fx-padding: 10;";
        } else {
            normalStyle = "-fx-background-color: transparent; -fx-border-radius: 15; -fx-border-color: #cccccc; -fx-padding: 10;";
            hoverStyle = "-fx-background-color: #e0e0e0; -fx-border-radius: 15; -fx-border-color: #bbbbbb; -fx-padding: 10;";
        }
        button.setStyle(normalStyle);
        button.setOnMouseEntered(e -> button.setStyle(hoverStyle));
        button.setOnMouseExited(e -> button.setStyle(normalStyle));
    }

    private Button createThemeButton() {
        Button themeBtn = new Button("Change Theme");
        themeBtn.setOnAction(e -> {
            isDarkTheme = !isDarkTheme;
            applyTheme();
            for (javafx.scene.Node node : imageButtonsBox.getChildren()) {
                if (node instanceof Button) {
                    styleButton((Button)node);
                }
            }
        });
        themeBtn.setStyle("-fx-background-color: #444; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 16;");
        themeBtn.setMaxWidth(200);
        return themeBtn;
    }

    private Button createHistoryButton() {
        Button historyBtn = new Button("Game History");
        historyBtn.setOnAction(e -> showHistoryDialog());
        historyBtn.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 16;");
        historyBtn.setMaxWidth(200);
        return historyBtn;
    }

    private Button createResetButton(String text) {
        Button resetBtn = new Button(text);
        resetBtn.setOnAction(e -> showGameScene());
        resetBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 16;");
        resetBtn.setMaxWidth(200);
        return resetBtn;
    }

    private Button createLogoutButton() {
        Button logoutBtn = new Button("Log Out");
        logoutBtn.setOnAction(e -> {
            currentUser = "";
            if (historyListView != null) {
                historyListView.getItems().clear();
            }
            showLoginScene();
        });
        logoutBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 16;");
        logoutBtn.setMaxWidth(200);
        return logoutBtn;
    }

    private void applyTheme() {
        if (root == null) return;

        if (isDarkTheme) {
            root.setStyle("-fx-background-color: #2b2b2b;");
            if (scoreLabel != null) scoreLabel.setTextFill(Color.WHITE);
            if (roundLabel != null) roundLabel.setTextFill(Color.WHITE);
            if (resultLabel != null) resultLabel.setTextFill(Color.WHITE);
            if (playerChoiceImageView != null && playerChoiceImageView.getParent() instanceof VBox) {
                VBox parent = (VBox) playerChoiceImageView.getParent();
                if (!parent.getChildren().isEmpty() && parent.getChildren().get(0) instanceof Label) {
                    ((Label)parent.getChildren().get(0)).setTextFill(Color.WHITE);
                }
            }
            if (computerChoiceImageView != null && computerChoiceImageView.getParent() instanceof VBox) {
                VBox parent = (VBox) computerChoiceImageView.getParent();
                if (!parent.getChildren().isEmpty() && parent.getChildren().get(0) instanceof Label) {
                    ((Label)parent.getChildren().get(0)).setTextFill(Color.WHITE);
                }
            }

        } else {
            root.setStyle("-fx-background-color: #f4f4f4;");
            if (scoreLabel != null) scoreLabel.setTextFill(Color.BLACK);
            if (roundLabel != null) roundLabel.setTextFill(Color.BLACK);
            if (resultLabel != null) resultLabel.setTextFill(Color.BLACK);
            if (playerChoiceImageView != null && playerChoiceImageView.getParent() instanceof VBox) {
                VBox parent = (VBox) playerChoiceImageView.getParent();
                if (!parent.getChildren().isEmpty() && parent.getChildren().get(0) instanceof Label) {
                    ((Label)parent.getChildren().get(0)).setTextFill(Color.BLACK);
                }
            }
            if (computerChoiceImageView != null && computerChoiceImageView.getParent() instanceof VBox) {
                VBox parent = (VBox) computerChoiceImageView.getParent();
                if (!parent.getChildren().isEmpty() && parent.getChildren().get(0) instanceof Label) {
                    ((Label)parent.getChildren().get(0)).setTextFill(Color.BLACK);
                }
            }
        }
        updateRoundLabelStyle();
    }

    private void updateRoundLabelStyle() {
        if (roundLabel == null) return;

        if (currentRound == doublePointRound && playerScore < 5 && computerScore < 5) {
            roundLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 18px;");
            roundLabel.setText("🔥 2x Point Round: 🔥 (Round " + currentRound + ")");
            roundLabel.setTextFill(Color.GOLD);
        } else {
            roundLabel.setStyle("-fx-font-size: 16px;" + (isDarkTheme ? "-fx-text-fill: lightgray;" : "-fx-text-fill: darkblue;"));
            if (playerScore < 5 && computerScore < 5) {
                roundLabel.setText("2x Point Round is: " + doublePointRound);
            } else {
                roundLabel.setText("Game Over!");
            }
        }
    }

    private void playGame(String playerChoice) {
        disableChoiceButtons(true);

        playerChoiceImageView.setImage(loadImage(getImageName(playerChoice)));
        animateSelection(playerChoiceImageView);

        PauseTransition pause = new PauseTransition(Duration.seconds(1));
        pause.setOnFinished(e -> {
            String[] choices = {"rock", "paper", "scissors"};
            String computerChoice = choices[random.nextInt(3)];

            boolean isDoubleRound = currentRound == doublePointRound;
            int point = isDoubleRound ? 2 : 1;

            String resultText;
            if (playerChoice.equals(computerChoice)) {
                resultText = "Draw!";
            } else if ((playerChoice.equals("rock") && computerChoice.equals("scissors")) ||
                    (playerChoice.equals("paper") && computerChoice.equals("rock")) ||
                    (playerChoice.equals("scissors") && computerChoice.equals("paper"))) {
                playerScore += point;
                resultText = currentUser + " Wins Round!" + (isDoubleRound ? " (+"+point+" Points)" : " (+"+point+" Point)");
            } else {
                computerScore += point;
                resultText = "AI Wins Round!" + (isDoubleRound ? " (+"+point+" Points)" : " (+"+point+" Point)");
            }

            computerChoiceImageView.setImage(loadImage(getImageName(computerChoice)));
            animateSelection(computerChoiceImageView);

            scoreLabel.setText(currentUser + ": " + playerScore + "   |   AI: " + computerScore);
            resultLabel.setText("Round " + currentRound + ": " + resultText);

            if (playerScore >= 5 || computerScore >= 5) {
                disableChoiceButtons(true);
                String gameEndMessage = "GAME OVER! ";
                if (playerScore > computerScore) {
                    gameEndMessage += "WINNER WINNER CHICKEN DINNER!";
                } else {
                    gameEndMessage += " LOOOOOOOOOOOOOOOOSER!";
                }
                resultLabel.setText(gameEndMessage);
                saveGameResultToHistoryFile(currentUser, playerScore, computerScore);
                updateRoundLabelStyle();
            } else {
                currentRound++;
                updateRoundLabelStyle();
                disableChoiceButtons(false);
            }
        });

        pause.play();
    }

    private void disableChoiceButtons(boolean disable) {
        if (imageButtonsBox != null) {
            for (javafx.scene.Node node : imageButtonsBox.getChildren()) {
                if (node instanceof Button) {
                    node.setDisable(disable);
                }
            }
        }
    }

    private void showHistoryDialog() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Game History for " + currentUser);
        dialog.setHeaderText("Showing all past game results for " + currentUser);
        ButtonType closeButtonType = new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().add(closeButtonType);

        historyListView.getItems().clear();
        List<String> history = loadHistoryFromFile(currentUser);
        if (history.isEmpty()) {
            historyListView.getItems().add("No game history found for " + currentUser + ".");
        } else {
            historyListView.getItems().addAll(history);
        }

        ScrollPane scrollPane = new ScrollPane(historyListView);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        VBox content = new VBox(scrollPane);
        content.setPrefSize(380, 280);

        dialog.getDialogPane().setContent(content);

        if (isDarkTheme) {
            dialog.getDialogPane().setStyle("-fx-background-color: #3c3c3c;");
        } else {
            dialog.getDialogPane().setStyle("-fx-background-color: #f4f4f4;");
        }


        dialog.showAndWait();
    }
    private void saveUserToFile(String username, String password) {
        try (PrintWriter writer = new PrintWriter(new FileWriter("users.txt", true))) {
            writer.println(username + "," + password);
        } catch (IOException e) {
            System.err.println("Could not write to user file: " + e.getMessage());
        }
    }

    private void loadUserDatabase() {
        try {
            File file = new File("users.txt");
            if (!file.exists()) {
                file.createNewFile();
            }
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(",");
                    if (parts.length == 2) {
                        userDatabase.put(parts[0], parts[1]);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to read user data: " + e.getMessage());
        }
    }
    private List<String> loadHistoryFromFile(String username) {
        List<String> history = new ArrayList<>();
        String filename = username + "_history.txt";
        File file = new File(filename);
        if (file.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = br.readLine()) != null) {
                    history.add(line);
                }
            } catch (IOException e) {
                System.err.println("Could not load history: " + e.getMessage());
            }
        }
        return history;
    }
    private Image loadImage(String imageName) {
        InputStream is = getClass().getResourceAsStream("/images/" + imageName);

        if (is == null) {
            try {
                File imageFile = new File("images/" + imageName);
                if (imageFile.exists()) {
                    is = new FileInputStream(imageFile);
                } else {
                    imageFile = new File("../images/" + imageName);
                    if (imageFile.exists()) {
                        is = new FileInputStream(imageFile);
                    } else {
                        System.err.println(imageName + " could not be found in classpath or filesystem (images/" + imageName + " or ../images/" + imageName + ")");
                        return null;
                    }
                }
            } catch (FileNotFoundException e) {
                System.err.println("Image file not found (fallback): " + e.getMessage());
                return null;
            }
        }

        try {
            if (is != null) {
                Image loadedImage = new Image(is);
                is.close();
                return loadedImage;
            }
        } catch (IOException e) {
            System.err.println("Image loading IO Error: " + e.getMessage());
        } catch (NullPointerException e) {
            System.err.println("Null pointer during image loading, image path might be incorrect: " + imageName);
        }
        return null;
    }

    private String getImageName(String choice) {
        switch (choice) {
            case "rock": return "rock.png";
            case "paper": return "paper.png";
            case "scissors": return "scissors.png";
            default: return "";
        }
    }
    private void saveGameResultToHistoryFile(String username, int playerScore, int computerScore) {
        String filename = username + "_history.txt";
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename, true))) {
            String result = (playerScore > computerScore) ? "Win" : (playerScore < computerScore ? "Lose" : "Draw");
            writer.println("Score: " + playerScore + " - " + computerScore + " => " + result );
        } catch (IOException e) {
            System.err.println("Could not write history file: " + e.getMessage());
        }
    }
    private void animateSelection(ImageView imageView) {
        if (imageView.getImage() == null) return;

        FadeTransition fade = new FadeTransition(Duration.millis(300), imageView);
        fade.setFromValue(0.0);
        fade.setToValue(1.0);

        ScaleTransition scale = new ScaleTransition(Duration.millis(300), imageView);
        scale.setFromX(0.8);
        scale.setFromY(0.8);
        scale.setToX(1.0);
        scale.setToY(1.0);

        ParallelTransition animation = new ParallelTransition(fade, scale);
        animation.play();
    }
    public static void main(String[] args) {
        launch(args);
    }
}