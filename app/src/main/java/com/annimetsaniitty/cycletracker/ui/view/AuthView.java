package com.annimetsaniitty.cycletracker.ui.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class AuthView extends StackPane {
    private final TextField loginUsernameField = new TextField();
    private final PasswordField loginPasswordField = new PasswordField();
    private final TextField registerUsernameField = new TextField();
    private final TextField registerEmailField = new TextField();
    private final PasswordField registerPasswordField = new PasswordField();
    private final Button loginButton = new Button("Login");
    private final Button registerButton = new Button("Create Account");
    private final Label statusBanner = new Label();

    private AuthActions actions;

    public AuthView(String apiBaseUrl) {
        getStyleClass().add("auth-root");

        Label title = new Label("Cycle Tracker");
        title.getStyleClass().add("hero-title");

        Label subtitle = new Label(
                "Connect to the local backend and manage cycle tracking from a desktop dashboard.");
        subtitle.getStyleClass().add("muted-copy");
        subtitle.setWrapText(true);

        VBox loginCard = buildLoginCard();
        VBox registerCard = buildRegisterCard();

        HBox cards = new HBox(24, loginCard, registerCard);
        cards.setAlignment(Pos.CENTER);

        statusBanner.setText("Backend base URL: " + apiBaseUrl);
        statusBanner.getStyleClass().addAll("status-banner", "status-info");

        VBox content = new VBox(24, title, subtitle, cards, statusBanner);
        content.setPadding(new Insets(36));
        content.setAlignment(Pos.CENTER);
        content.setMaxWidth(960);

        getChildren().add(content);
    }

    public void setActions(AuthActions actions) {
        this.actions = actions;
    }

    public void setBusy(boolean busy) {
        loginButton.setDisable(busy);
        registerButton.setDisable(busy);
    }

    public void setStatus(String message, boolean error) {
        statusBanner.setText(message);
        statusBanner.getStyleClass().removeAll("status-info", "status-error");
        statusBanner.getStyleClass().add(error ? "status-error" : "status-info");
    }

    private VBox buildLoginCard() {
        Label heading = new Label("Login");
        heading.getStyleClass().add("panel-title");

        loginUsernameField.setPromptText("Username");
        loginPasswordField.setPromptText("Password");

        loginButton.getStyleClass().addAll("action-button", "primary-button");
        loginButton.setMaxWidth(Double.MAX_VALUE);
        loginButton.setOnAction(event -> {
            if (actions != null) {
                actions.onLogin(loginUsernameField.getText(), loginPasswordField.getText());
            }
        });

        VBox card = createCard(heading, loginUsernameField, loginPasswordField, loginButton);
        card.setPrefWidth(360);
        return card;
    }

    private VBox buildRegisterCard() {
        Label heading = new Label("Register");
        heading.getStyleClass().add("panel-title");

        registerUsernameField.setPromptText("Username");
        registerEmailField.setPromptText("Email");
        registerPasswordField.setPromptText("Password");

        registerButton.getStyleClass().addAll("action-button", "accent-button");
        registerButton.setMaxWidth(Double.MAX_VALUE);
        registerButton.setOnAction(event -> {
            if (actions != null) {
                actions.onRegister(
                        registerUsernameField.getText(),
                        registerEmailField.getText(),
                        registerPasswordField.getText());
            }
        });

        VBox card = createCard(
                heading,
                registerUsernameField,
                registerEmailField,
                registerPasswordField,
                registerButton);
        card.setPrefWidth(360);
        return card;
    }

    private VBox createCard(javafx.scene.Node... children) {
        VBox card = new VBox(14, children);
        card.getStyleClass().add("card-panel");
        card.setPadding(new Insets(24));
        return card;
    }

    public interface AuthActions {
        void onLogin(String username, String password);

        void onRegister(String username, String email, String password);
    }
}
