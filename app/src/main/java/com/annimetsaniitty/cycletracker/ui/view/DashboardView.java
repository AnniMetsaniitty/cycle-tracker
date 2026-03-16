package com.annimetsaniitty.cycletracker.ui.view;

import com.annimetsaniitty.cycletracker.dto.CycleResponse;
import com.annimetsaniitty.cycletracker.dto.MedicationStatusResponse;
import com.annimetsaniitty.cycletracker.dto.UserResponse;
import com.annimetsaniitty.cycletracker.ui.DashboardSnapshot;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class DashboardView extends BorderPane {
    private final UserResponse currentUser;

    private final Label statusBanner = new Label("Connected");
    private final Label usernameLabel = new Label();
    private final Label cycleDayValue = metricValueLabel("--");
    private final Label cycleStartValue = metricValueLabel("--");
    private final Label cycleStateValue = metricValueLabel("--");
    private final Label medicationValue = metricValueLabel("--");
    private final Label medicationRangeValue = metricValueLabel("--");
    private final Label nextMedicationValue = metricValueLabel("--");
    private final Label completedCyclesValue = metricValueLabel("--");
    private final Label averageCycleValue = metricValueLabel("--");
    private final Label emptyHistoryLabel = new Label("Use the dashboard actions to create the first cycle and history entries.");
    private final Label detailTitle = new Label("Select a cycle");
    private final Label detailDates = new Label("No cycle selected");
    private final Label detailLength = new Label("--");
    private final Label detailMedicationWindow = new Label("--");
    private final Label detailState = new Label("--");

    private final Button refreshButton = new Button("Refresh");
    private final Button startCycleButton = new Button("Start New Cycle");
    private final Button endCycleButton = new Button("End Active Cycle");
    private final Button viewHistoryButton = new Button("View History");
    private final Button medicationStatusButton = new Button("Medication Status");
    private final Button logoutButton = new Button("Log Out");
    private final TableView<CycleHistoryRow> historyTable = new TableView<>();
    private final VBox medicationCard;
    private boolean hasActiveCycle;
    private DashboardActions actions;

    public DashboardView(UserResponse currentUser) {
        this.currentUser = currentUser;
        getStyleClass().add("dashboard-root");

        medicationCard = buildMedicationCard();

        VBox topSection = new VBox(12, buildHeaderBar(), statusBanner);
        topSection.setPadding(new Insets(24, 24, 12, 24));
        setTop(topSection);

        VBox summaryColumn = new VBox(18, buildCycleCard(), medicationCard, buildInsightCard());
        summaryColumn.setPrefWidth(360);

        VBox historyColumn = new VBox(18, buildActionCard(), buildHistoryCard(), buildHistoryDetailCard());
        HBox.setHgrow(historyColumn, Priority.ALWAYS);

        HBox center = new HBox(22, summaryColumn, historyColumn);
        center.setPadding(new Insets(12, 24, 24, 24));
        setCenter(center);

        statusBanner.getStyleClass().addAll("status-banner", "status-info");
        emptyHistoryLabel.getStyleClass().add("muted-copy");
        emptyHistoryLabel.setWrapText(true);
        detailTitle.getStyleClass().add("panel-title");
        detailDates.getStyleClass().add("muted-copy");
        detailLength.getStyleClass().add("metric-value");
        detailMedicationWindow.getStyleClass().add("metric-value");
        detailState.getStyleClass().add("metric-value");

        usernameLabel.setText("Signed in as " + currentUser.username() + "  •  " + currentUser.email());
        usernameLabel.getStyleClass().add("eyebrow-label");

        historyTable.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> updateHistoryDetail(newValue));
    }

    public void setActions(DashboardActions actions) {
        this.actions = actions;
    }

    public void setBusy(boolean busy) {
        refreshButton.setDisable(busy);
        startCycleButton.setDisable(busy);
        endCycleButton.setDisable(busy || !hasActiveCycle);
        viewHistoryButton.setDisable(busy);
        medicationStatusButton.setDisable(busy);
        logoutButton.setDisable(busy);
    }

    public void setStatus(String message, boolean error) {
        statusBanner.setText(message);
        statusBanner.getStyleClass().removeAll("status-info", "status-error");
        statusBanner.getStyleClass().add(error ? "status-error" : "status-info");
    }

    public void focusHistorySection() {
        Platform.runLater(() -> {
            historyTable.requestFocus();
            if (!historyTable.getItems().isEmpty() && historyTable.getSelectionModel().isEmpty()) {
                historyTable.getSelectionModel().selectFirst();
            }
        });
    }

    public void focusMedicationSection() {
        Platform.runLater(medicationCard::requestFocus);
    }

    public void applySnapshot(DashboardSnapshot snapshot) {
        updateCycleSummary(snapshot.currentCycle(), snapshot.medicationStatus());
        nextMedicationValue.setText(snapshot.nextMedicationText());
        completedCyclesValue.setText(String.valueOf(snapshot.completedCycles()));
        averageCycleValue.setText(snapshot.averageCycleLength());

        List<CycleHistoryRow> rows = snapshot.history().stream()
                .map(CycleHistoryRow::from)
                .toList();
        historyTable.setItems(FXCollections.observableArrayList(rows));
        emptyHistoryLabel.setVisible(rows.isEmpty());
        emptyHistoryLabel.setManaged(rows.isEmpty());

        if (rows.isEmpty()) {
            updateHistoryDetail(null);
        } else {
            historyTable.getSelectionModel().selectFirst();
        }
    }

    private HBox buildHeaderBar() {
        Label appTitle = new Label("Cycle Tracker");
        appTitle.getStyleClass().add("hero-title");

        VBox titleGroup = new VBox(4, usernameLabel, appTitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        refreshButton.getStyleClass().addAll("action-button", "secondary-button");
        refreshButton.setOnAction(event -> runIfBound(actions::onRefresh));

        logoutButton.getStyleClass().addAll("action-button", "ghost-button");
        logoutButton.setOnAction(event -> runIfBound(actions::onLogout));

        HBox header = new HBox(12, titleGroup, spacer, refreshButton, logoutButton);
        header.setAlignment(Pos.CENTER_LEFT);
        return header;
    }

    private VBox buildCycleCard() {
        return createCard(
                sectionTitle("Current Cycle"),
                metricRow("Cycle day", cycleDayValue),
                metricRow("Start date", cycleStartValue),
                metricRow("State", cycleStateValue),
                metricRow("Next medication", nextMedicationValue));
    }

    private VBox buildMedicationCard() {
        startCycleButton.getStyleClass().addAll("action-button", "primary-button");
        startCycleButton.setMaxWidth(Double.MAX_VALUE);
        startCycleButton.setOnAction(event -> runIfBound(actions::onStartCycle));

        endCycleButton.getStyleClass().addAll("action-button", "accent-button");
        endCycleButton.setMaxWidth(Double.MAX_VALUE);
        endCycleButton.setOnAction(event -> runIfBound(actions::onEndCycle));

        medicationStatusButton.getStyleClass().addAll("action-button", "secondary-button");
        medicationStatusButton.setMaxWidth(Double.MAX_VALUE);
        medicationStatusButton.setOnAction(event -> runIfBound(actions::onMedicationStatus));

        VBox card = createCard(
                sectionTitle("Medication Window"),
                metricRow("Status", medicationValue),
                metricRow("Range", medicationRangeValue),
                new Separator(),
                medicationStatusButton,
                startCycleButton,
                endCycleButton);
        card.setPrefWidth(340);
        card.setFocusTraversable(true);
        return card;
    }

    private VBox buildInsightCard() {
        return createCard(
                sectionTitle("History Insights"),
                metricRow("Completed cycles", completedCyclesValue),
                metricRow("Average length", averageCycleValue));
    }

    private VBox buildActionCard() {
        viewHistoryButton.getStyleClass().addAll("action-button", "secondary-button");
        viewHistoryButton.setOnAction(event -> runIfBound(actions::onViewHistory));

        HBox actionRow = new HBox(12, viewHistoryButton);
        actionRow.setAlignment(Pos.CENTER_LEFT);

        return createCard(
                sectionTitle("Dashboard Actions"),
                new Label("Use these shortcuts to move between the live dashboard and cycle history."),
                actionRow);
    }

    private VBox buildHistoryCard() {
        TableColumn<CycleHistoryRow, Long> idColumn = new TableColumn<>("Cycle");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<CycleHistoryRow, String> startColumn = new TableColumn<>("Start");
        startColumn.setCellValueFactory(new PropertyValueFactory<>("startDateText"));

        TableColumn<CycleHistoryRow, String> endColumn = new TableColumn<>("End");
        endColumn.setCellValueFactory(new PropertyValueFactory<>("endDateText"));

        TableColumn<CycleHistoryRow, String> stateColumn = new TableColumn<>("State");
        stateColumn.setCellValueFactory(new PropertyValueFactory<>("state"));

        TableColumn<CycleHistoryRow, String> lengthColumn = new TableColumn<>("Length");
        lengthColumn.setCellValueFactory(new PropertyValueFactory<>("lengthText"));

        historyTable.getColumns().addAll(idColumn, startColumn, endColumn, stateColumn, lengthColumn);
        historyTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        historyTable.setPlaceholder(new Label("No cycle history yet."));

        VBox historyCard = createCard(sectionTitle("Cycle History"), emptyHistoryLabel, historyTable);
        VBox.setVgrow(historyTable, Priority.ALWAYS);
        historyCard.setMinWidth(620);
        historyCard.setPrefHeight(420);
        return historyCard;
    }

    private VBox buildHistoryDetailCard() {
        return createCard(
                sectionTitle("Selected Cycle"),
                detailTitle,
                detailDates,
                metricRow("Length", detailLength),
                metricRow("Medication window", detailMedicationWindow),
                metricRow("State", detailState));
    }

    private void updateCycleSummary(CycleResponse cycle, MedicationStatusResponse medicationStatus) {
        if (cycle == null) {
            hasActiveCycle = false;
            cycleDayValue.setText("--");
            cycleStartValue.setText("--");
            cycleStateValue.setText("No active cycle");
            medicationValue.setText("Unavailable");
            medicationRangeValue.setText("--");
            endCycleButton.setDisable(true);
            return;
        }

        hasActiveCycle = cycle.active();
        cycleDayValue.setText(String.valueOf(cycle.currentDay()));
        cycleStartValue.setText(String.valueOf(cycle.startDate()));
        cycleStateValue.setText(cycle.active() ? "Active" : "Closed");
        endCycleButton.setDisable(!cycle.active());

        if (medicationStatus != null) {
            medicationValue.setText(medicationStatus.active() ? "Active today" : "Not active");
            medicationRangeValue.setText(
                    medicationStatus.medicationStartDay() + " - " + medicationStatus.medicationEndDay());
        } else {
            medicationValue.setText("Unavailable");
            medicationRangeValue.setText("--");
        }
    }

    private void updateHistoryDetail(CycleHistoryRow row) {
        if (row == null) {
            detailTitle.setText("Select a cycle");
            detailDates.setText("No cycle selected");
            detailLength.setText("--");
            detailMedicationWindow.setText("16 - 26");
            detailState.setText("--");
            return;
        }

        detailTitle.setText("Cycle #" + row.getId());
        detailDates.setText(row.getStartDateText() + "  ->  " + row.getEndDateText());
        detailLength.setText(row.getLengthText());
        detailMedicationWindow.setText("Days 16 - 26");
        detailState.setText(row.getState());
    }

    private VBox createCard(javafx.scene.Node... children) {
        VBox card = new VBox(14, children);
        card.getStyleClass().add("card-panel");
        card.setPadding(new Insets(24));
        return card;
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

    private static Label metricValueLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("metric-value");
        return label;
    }

    private void runIfBound(Runnable action) {
        if (actions != null) {
            action.run();
        }
    }

    public interface DashboardActions {
        void onRefresh();

        void onStartCycle();

        void onEndCycle();

        void onViewHistory();

        void onMedicationStatus();

        void onLogout();
    }

    public static class CycleHistoryRow {
        private final Long id;
        private final LocalDate startDate;
        private final LocalDate endDate;
        private final String state;
        private final long currentDay;

        private CycleHistoryRow(Long id, LocalDate startDate, LocalDate endDate, String state, long currentDay) {
            this.id = id;
            this.startDate = startDate;
            this.endDate = endDate;
            this.state = state;
            this.currentDay = currentDay;
        }

        public static CycleHistoryRow from(CycleResponse cycle) {
            return new CycleHistoryRow(
                    cycle.id(),
                    cycle.startDate(),
                    cycle.endDate(),
                    cycle.active() ? "Active" : "Closed",
                    cycle.currentDay());
        }

        public Long getId() {
            return id;
        }

        public String getStartDateText() {
            return String.valueOf(startDate);
        }

        public String getEndDateText() {
            return endDate == null ? "Active" : String.valueOf(endDate);
        }

        public String getState() {
            return state;
        }

        public long getCurrentDay() {
            return currentDay;
        }

        public String getLengthText() {
            if (endDate != null) {
                long totalDays = ChronoUnit.DAYS.between(startDate, endDate) + 1;
                return totalDays + " days";
            }
            return currentDay + " days active";
        }
    }
}
