package com.annimetsaniitty.cycletracker.ui;

import com.annimetsaniitty.cycletracker.client.ApiClientException;
import com.annimetsaniitty.cycletracker.client.CycleTrackerApiClient;
import com.annimetsaniitty.cycletracker.dto.CycleResponse;
import com.annimetsaniitty.cycletracker.dto.MedicationStatusResponse;
import com.annimetsaniitty.cycletracker.dto.UserResponse;
import java.util.List;
import java.util.concurrent.Callable;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Separator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class CycleTrackerDesktopApp extends Application {
    private final SessionState sessionState = new SessionState();
    private final CycleTrackerApiClient apiClient = new CycleTrackerApiClient(resolveApiBaseUrl());

    private Stage primaryStage;
    private Label statusBanner;
    private Label usernameLabel;
    private Label cycleDayValue;
    private Label cycleStartValue;
    private Label cycleStateValue;
    private Label medicationValue;
    private Label medicationRangeValue;
    private Label emptyHistoryLabel;
    private TableView<CycleHistoryRow> historyTable;
    private Button startCycleButton;
    private Button endCycleButton;
    private Button refreshButton;

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        primaryStage.setTitle("Cycle Tracker Desktop");
        primaryStage.setMinWidth(1040);
        primaryStage.setMinHeight(720);
        showAuthScene();
        primaryStage.show();
    }

    private void showAuthScene() {
        String apiBaseUrl = resolveApiBaseUrl();
        Label title = new Label("Cycle Tracker");
        title.getStyleClass().add("hero-title");

        Label subtitle = new Label("Connect to the local backend and manage cycle tracking from a desktop dashboard.");
        subtitle.getStyleClass().add("muted-copy");
        subtitle.setWrapText(true);

        VBox loginCard = buildLoginCard();
        VBox registerCard = buildRegisterCard();

        HBox cards = new HBox(24, loginCard, registerCard);
        cards.setAlignment(Pos.CENTER);

        statusBanner = new Label("Backend base URL: " + apiBaseUrl);
        statusBanner.getStyleClass().addAll("status-banner", "status-info");

        VBox content = new VBox(24, title, subtitle, cards, statusBanner);
        content.setPadding(new Insets(36));
        content.setAlignment(Pos.CENTER);
        content.setMaxWidth(960);

        StackPane root = new StackPane(content);
        root.getStyleClass().add("auth-root");
        Scene scene = new Scene(root, 1100, 760);
        scene.getStylesheets().add(getClass().getResource("/ui/cycle-tracker.css").toExternalForm());
        primaryStage.setScene(scene);
    }

    private VBox buildLoginCard() {
        Label heading = new Label("Login");
        heading.getStyleClass().add("panel-title");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        Button loginButton = new Button("Login");
        loginButton.getStyleClass().addAll("action-button", "primary-button");
        loginButton.setMaxWidth(Double.MAX_VALUE);
        loginButton.setOnAction(event -> runUiTask(
                () -> apiClient.login(usernameField.getText().trim(), passwordField.getText()),
                user -> {
                    sessionState.setCurrentUser(user);
                    showDashboardScene();
                },
                loginButton));

        VBox card = createCard(heading, usernameField, passwordField, loginButton);
        card.setPrefWidth(360);
        return card;
    }

    private VBox buildRegisterCard() {
        Label heading = new Label("Register");
        heading.getStyleClass().add("panel-title");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");

        TextField emailField = new TextField();
        emailField.setPromptText("Email");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        Button registerButton = new Button("Create Account");
        registerButton.getStyleClass().addAll("action-button", "accent-button");
        registerButton.setMaxWidth(Double.MAX_VALUE);
        registerButton.setOnAction(event -> runUiTask(
                () -> apiClient.register(
                        usernameField.getText().trim(),
                        emailField.getText().trim(),
                        passwordField.getText()),
                user -> {
                    sessionState.setCurrentUser(user);
                    showDashboardScene();
                },
                registerButton));

        VBox card = createCard(heading, usernameField, emailField, passwordField, registerButton);
        card.setPrefWidth(360);
        return card;
    }

    private VBox createCard(javafx.scene.Node... children) {
        VBox card = new VBox(14, children);
        card.getStyleClass().add("card-panel");
        card.setPadding(new Insets(24));
        return card;
    }

    private void showDashboardScene() {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("dashboard-root");

        statusBanner = new Label("Connected");
        statusBanner.getStyleClass().addAll("status-banner", "status-info");

        VBox topSection = new VBox(12, buildHeaderBar(), statusBanner);
        topSection.setPadding(new Insets(24, 24, 12, 24));
        root.setTop(topSection);

        VBox summaryColumn = buildSummaryColumn();
        VBox historyColumn = buildHistoryColumn();

        HBox center = new HBox(22, summaryColumn, historyColumn);
        center.setPadding(new Insets(12, 24, 24, 24));
        HBox.setHgrow(summaryColumn, Priority.NEVER);
        HBox.setHgrow(historyColumn, Priority.ALWAYS);
        root.setCenter(center);

        Scene scene = new Scene(root, 1180, 820);
        scene.getStylesheets().add(getClass().getResource("/ui/cycle-tracker.css").toExternalForm());
        primaryStage.setScene(scene);
        refreshDashboard();
    }

    private HBox buildHeaderBar() {
        Label appTitle = new Label("Cycle Tracker");
        appTitle.getStyleClass().add("hero-title");

        usernameLabel = new Label();
        usernameLabel.getStyleClass().add("eyebrow-label");

        VBox titleGroup = new VBox(4, usernameLabel, appTitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        refreshButton = new Button("Refresh");
        refreshButton.getStyleClass().addAll("action-button", "secondary-button");
        refreshButton.setOnAction(event -> refreshDashboard());

        Button logoutButton = new Button("Log Out");
        logoutButton.getStyleClass().addAll("action-button", "ghost-button");
        logoutButton.setOnAction(event -> {
            sessionState.clear();
            showAuthScene();
        });

        HBox header = new HBox(12, titleGroup, spacer, refreshButton, logoutButton);
        header.setAlignment(Pos.CENTER_LEFT);
        return header;
    }

    private VBox buildSummaryColumn() {
        cycleDayValue = metricValueLabel("--");
        cycleStartValue = metricValueLabel("--");
        cycleStateValue = metricValueLabel("--");
        medicationValue = metricValueLabel("--");
        medicationRangeValue = metricValueLabel("--");

        VBox cycleCard = createCard(
                sectionTitle("Current Cycle"),
                metricRow("Cycle day", cycleDayValue),
                metricRow("Start date", cycleStartValue),
                metricRow("State", cycleStateValue));

        startCycleButton = new Button("Start New Cycle");
        startCycleButton.getStyleClass().addAll("action-button", "primary-button");
        startCycleButton.setMaxWidth(Double.MAX_VALUE);
        startCycleButton.setOnAction(event -> runDashboardTask(
                () -> apiClient.startCycle(sessionState.getCurrentUser().id()),
                cycle -> refreshDashboard(),
                startCycleButton,
                endCycleButton,
                refreshButton));

        endCycleButton = new Button("End Active Cycle");
        endCycleButton.getStyleClass().addAll("action-button", "accent-button");
        endCycleButton.setMaxWidth(Double.MAX_VALUE);
        endCycleButton.setOnAction(event -> runDashboardTask(
                () -> apiClient.endCycle(sessionState.getCurrentUser().id()),
                cycle -> refreshDashboard(),
                startCycleButton,
                endCycleButton,
                refreshButton));

        VBox medicationCard = createCard(
                sectionTitle("Medication Window"),
                metricRow("Status", medicationValue),
                metricRow("Range", medicationRangeValue),
                new Separator(),
                startCycleButton,
                endCycleButton);
        medicationCard.setPrefWidth(340);

        VBox column = new VBox(18, cycleCard, medicationCard);
        column.setPrefWidth(360);
        return column;
    }

    private VBox buildHistoryColumn() {
        TableColumn<CycleHistoryRow, Long> idColumn = new TableColumn<>("Cycle");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<CycleHistoryRow, String> startColumn = new TableColumn<>("Start");
        startColumn.setCellValueFactory(new PropertyValueFactory<>("startDate"));

        TableColumn<CycleHistoryRow, String> endColumn = new TableColumn<>("End");
        endColumn.setCellValueFactory(new PropertyValueFactory<>("endDate"));

        TableColumn<CycleHistoryRow, String> stateColumn = new TableColumn<>("State");
        stateColumn.setCellValueFactory(new PropertyValueFactory<>("state"));

        TableColumn<CycleHistoryRow, Long> dayColumn = new TableColumn<>("Current Day");
        dayColumn.setCellValueFactory(new PropertyValueFactory<>("currentDay"));

        historyTable = new TableView<>();
        historyTable.getColumns().addAll(idColumn, startColumn, endColumn, stateColumn, dayColumn);
        historyTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        historyTable.setPlaceholder(new Label("No cycle history yet."));

        emptyHistoryLabel = new Label("Use the dashboard actions to create the first cycle and history entries.");
        emptyHistoryLabel.getStyleClass().add("muted-copy");
        emptyHistoryLabel.setWrapText(true);

        VBox historyCard = createCard(
                sectionTitle("Cycle History"),
                emptyHistoryLabel,
                historyTable);
        VBox.setVgrow(historyTable, Priority.ALWAYS);
        historyCard.setMinWidth(620);
        historyCard.setPrefHeight(560);
        return historyCard;
    }

    private Label sectionTitle(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("panel-title");
        return label;
    }

    private HBox metricRow(String labelText, Label valueLabel) {
        Label label = new Label(labelText);
        label.getStyleClass().add("metric-label");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox row = new HBox(12, label, spacer, valueLabel);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private Label metricValueLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("metric-value");
        return label;
    }

    private void refreshDashboard() {
        UserResponse user = sessionState.getCurrentUser();
        if (user == null) {
            showAuthScene();
            return;
        }

        usernameLabel.setText("Signed in as " + user.username() + "  •  " + user.email());
        runDashboardTask(
                () -> new DashboardSnapshot(
                        safeGetCurrentCycle(user.id()),
                        safeGetMedicationStatus(user.id()),
                        apiClient.getCycleHistory(user.id())),
                snapshot -> populateDashboard(snapshot),
                startCycleButton,
                endCycleButton,
                refreshButton);
    }

    private CycleResponse safeGetCurrentCycle(Long userId) {
        try {
            return apiClient.getCurrentCycle(userId);
        } catch (ApiClientException exception) {
            if (exception.getMessage().startsWith("No active cycle")) {
                return null;
            }
            throw exception;
        }
    }

    private MedicationStatusResponse safeGetMedicationStatus(Long userId) {
        try {
            return apiClient.getMedicationStatus(userId);
        } catch (ApiClientException exception) {
            if (exception.getMessage().startsWith("No active cycle")) {
                return null;
            }
            throw exception;
        }
    }

    private void populateDashboard(DashboardSnapshot snapshot) {
        CycleResponse cycle = snapshot.currentCycle();
        MedicationStatusResponse medicationStatus = snapshot.medicationStatus();

        if (cycle == null) {
            cycleDayValue.setText("--");
            cycleStartValue.setText("--");
            cycleStateValue.setText("No active cycle");
            medicationValue.setText("Unavailable");
            medicationRangeValue.setText("--");
            endCycleButton.setDisable(true);
        } else {
            cycleDayValue.setText(String.valueOf(cycle.currentDay()));
            cycleStartValue.setText(String.valueOf(cycle.startDate()));
            cycleStateValue.setText(cycle.active() ? "Active" : "Closed");
            endCycleButton.setDisable(!cycle.active());
        }

        if (medicationStatus != null) {
            medicationValue.setText(medicationStatus.active() ? "Active today" : "Not active");
            medicationRangeValue.setText(
                    medicationStatus.medicationStartDay() + " - " + medicationStatus.medicationEndDay());
        }

        List<CycleHistoryRow> rows = snapshot.history().stream()
                .map(CycleHistoryRow::from)
                .toList();
        historyTable.setItems(FXCollections.observableArrayList(rows));
        emptyHistoryLabel.setVisible(rows.isEmpty());
        emptyHistoryLabel.setManaged(rows.isEmpty());
        setStatus("Dashboard updated", false);
    }

    private <T> void runUiTask(Callable<T> callable, java.util.function.Consumer<T> onSuccess, Button... controls) {
        runTask(callable, onSuccess, controls);
    }

    private <T> void runDashboardTask(Callable<T> callable, java.util.function.Consumer<T> onSuccess, Button... controls) {
        runTask(callable, onSuccess, controls);
    }

    private <T> void runTask(Callable<T> callable, java.util.function.Consumer<T> onSuccess, Button... controls) {
        setControlsDisabled(true, controls);
        Task<T> task = new Task<>() {
            @Override
            protected T call() throws Exception {
                return callable.call();
            }
        };

        task.setOnSucceeded(event -> {
            setControlsDisabled(false, controls);
            onSuccess.accept(task.getValue());
        });

        task.setOnFailed(event -> {
            setControlsDisabled(false, controls);
            Throwable exception = task.getException();
            setStatus(exception == null ? "Request failed" : exception.getMessage(), true);
        });

        Thread thread = new Thread(task, "cycle-tracker-ui-task");
        thread.setDaemon(true);
        thread.start();
    }

    private void setControlsDisabled(boolean disabled, Button... controls) {
        for (Button control : controls) {
            if (control != null) {
                control.setDisable(disabled);
            }
        }
    }

    private void setStatus(String message, boolean error) {
        Platform.runLater(() -> {
            statusBanner.setText(message);
            statusBanner.getStyleClass().removeAll("status-info", "status-error");
            statusBanner.getStyleClass().add(error ? "status-error" : "status-info");
        });
    }

    public static void main(String[] args) {
        launch(args);
    }

    private static String resolveApiBaseUrl() {
        String configuredValue = System.getProperty("cycletracker.api.base-url");
        if (configuredValue == null || configuredValue.isBlank()) {
            return "http://localhost:8080";
        }
        return configuredValue.trim();
    }

    public record DashboardSnapshot(
            CycleResponse currentCycle,
            MedicationStatusResponse medicationStatus,
            List<CycleResponse> history) {
    }

    public static class CycleHistoryRow {
        private final Long id;
        private final String startDate;
        private final String endDate;
        private final String state;
        private final Long currentDay;

        private CycleHistoryRow(Long id, String startDate, String endDate, String state, Long currentDay) {
            this.id = id;
            this.startDate = startDate;
            this.endDate = endDate;
            this.state = state;
            this.currentDay = currentDay;
        }

        public static CycleHistoryRow from(CycleResponse cycle) {
            return new CycleHistoryRow(
                    cycle.id(),
                    String.valueOf(cycle.startDate()),
                    cycle.endDate() == null ? "Active" : String.valueOf(cycle.endDate()),
                    cycle.active() ? "Active" : "Closed",
                    cycle.currentDay());
        }

        public Long getId() {
            return id;
        }

        public String getStartDate() {
            return startDate;
        }

        public String getEndDate() {
            return endDate;
        }

        public String getState() {
            return state;
        }

        public Long getCurrentDay() {
            return currentDay;
        }
    }
}
