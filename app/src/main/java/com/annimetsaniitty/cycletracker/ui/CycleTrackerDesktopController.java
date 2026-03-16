package com.annimetsaniitty.cycletracker.ui;

import com.annimetsaniitty.cycletracker.client.ApiClientException;
import com.annimetsaniitty.cycletracker.client.CycleTrackerApiClient;
import com.annimetsaniitty.cycletracker.dto.CycleResponse;
import com.annimetsaniitty.cycletracker.dto.MedicationStatusResponse;
import com.annimetsaniitty.cycletracker.dto.UserResponse;
import com.annimetsaniitty.cycletracker.ui.view.AuthView;
import com.annimetsaniitty.cycletracker.ui.view.DashboardView;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class CycleTrackerDesktopController {
    private final Stage stage;
    private final SessionState sessionState = new SessionState();
    private final CycleTrackerApiClient apiClient = new CycleTrackerApiClient(resolveApiBaseUrl());

    private AuthView authView;
    private DashboardView dashboardView;

    public CycleTrackerDesktopController(Stage stage) {
        this.stage = stage;
    }

    public void start() {
        stage.setTitle("Cycle Tracker Desktop");
        stage.setMinWidth(1040);
        stage.setMinHeight(720);
        showAuthScene();
        stage.show();
    }

    private void showAuthScene() {
        authView = new AuthView(resolveApiBaseUrl());
        authView.setActions(new AuthView.AuthActions() {
            @Override
            public void onLogin(String username, String password) {
                login(username, password);
            }

            @Override
            public void onRegister(String username, String email, String password) {
                register(username, email, password);
            }
        });

        stage.setScene(buildScene(authView, 1100, 760));
    }

    private void showDashboardScene() {
        dashboardView = new DashboardView(sessionState.getCurrentUser());
        dashboardView.setActions(new DashboardView.DashboardActions() {
            @Override
            public void onRefresh() {
                refreshDashboard();
            }

            @Override
            public void onStartCycle() {
                startCycle();
            }

            @Override
            public void onEndCycle() {
                endCycle();
            }

            @Override
            public void onViewHistory() {
                dashboardView.focusHistorySection();
            }

            @Override
            public void onMedicationStatus() {
                refreshDashboard();
                dashboardView.focusMedicationSection();
            }

            @Override
            public void onLogout() {
                sessionState.clear();
                showAuthScene();
            }
        });

        stage.setScene(buildScene(dashboardView, 1180, 820));
        refreshDashboard();
    }

    private Scene buildScene(javafx.scene.Parent root, double width, double height) {
        Scene scene = new Scene(root, width, height);
        scene.getStylesheets().add(getClass().getResource("/ui/cycle-tracker.css").toExternalForm());
        return scene;
    }

    private void login(String username, String password) {
        if (username.isBlank() || password.isBlank()) {
            authView.setStatus("Username and password are required.", true);
            return;
        }

        runTask(
                () -> apiClient.login(username.trim(), password),
                user -> {
                    sessionState.setCurrentUser(user);
                    showDashboardScene();
                },
                authView::setBusy,
                authView::setStatus);
    }

    private void register(String username, String email, String password) {
        if (username.isBlank() || email.isBlank() || password.isBlank()) {
            authView.setStatus("Username, email, and password are required.", true);
            return;
        }
        if (!email.contains("@")) {
            authView.setStatus("Enter a valid email address.", true);
            return;
        }

        runTask(
                () -> apiClient.register(username.trim(), email.trim(), password),
                user -> {
                    sessionState.setCurrentUser(user);
                    showDashboardScene();
                },
                authView::setBusy,
                authView::setStatus);
    }

    private void refreshDashboard() {
        UserResponse user = sessionState.getCurrentUser();
        if (user == null) {
            showAuthScene();
            return;
        }

        runTask(
                () -> {
                    CycleResponse currentCycle = safeGetCurrentCycle(user.id());
                    MedicationStatusResponse medicationStatus = safeGetMedicationStatus(user.id());
                    List<CycleResponse> history = apiClient.getCycleHistory(user.id());
                    return new DashboardSnapshot(
                            currentCycle,
                            medicationStatus,
                            history,
                            buildNextMedicationText(currentCycle, medicationStatus),
                            buildCompletedCycleCount(history),
                            buildAverageCycleLength(history));
                },
                dashboardView::applySnapshot,
                dashboardView::setBusy,
                dashboardView::setStatus);
    }

    private void startCycle() {
        runTask(
                () -> apiClient.startCycle(sessionState.getCurrentUser().id()),
                cycle -> refreshDashboard(),
                dashboardView::setBusy,
                dashboardView::setStatus);
    }

    private void endCycle() {
        runTask(
                () -> apiClient.endCycle(sessionState.getCurrentUser().id()),
                cycle -> refreshDashboard(),
                dashboardView::setBusy,
                dashboardView::setStatus);
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

    private String buildNextMedicationText(CycleResponse cycle, MedicationStatusResponse medicationStatus) {
        if (cycle == null || medicationStatus == null) {
            return "Start a cycle first";
        }

        if (medicationStatus.active()) {
            return "Today";
        }

        long currentDay = cycle.currentDay();
        if (currentDay < medicationStatus.medicationStartDay()) {
            long daysUntilMedication = medicationStatus.medicationStartDay() - currentDay;
            LocalDate nextDate = cycle.startDate().plusDays(medicationStatus.medicationStartDay() - 1L);
            return nextDate + " (" + daysUntilMedication + " day" + (daysUntilMedication == 1 ? "" : "s") + ")";
        }

        return "Medication window completed for this cycle";
    }

    private long buildCompletedCycleCount(List<CycleResponse> history) {
        return history.stream().filter(cycle -> !cycle.active()).count();
    }

    private String buildAverageCycleLength(List<CycleResponse> history) {
        List<Long> lengths = history.stream()
                .filter(cycle -> cycle.endDate() != null)
                .map(cycle -> ChronoUnit.DAYS.between(cycle.startDate(), cycle.endDate()) + 1)
                .toList();
        if (lengths.isEmpty()) {
            return "--";
        }
        long total = lengths.stream().mapToLong(Long::longValue).sum();
        double average = (double) total / lengths.size();
        return String.format("%.1f days", average);
    }

    private <T> void runTask(
            Callable<T> callable,
            Consumer<T> onSuccess,
            Consumer<Boolean> setBusy,
            DashboardStatusSink statusSink) {
        setBusy.accept(true);
        Task<T> task = new Task<>() {
            @Override
            protected T call() throws Exception {
                return callable.call();
            }
        };

        task.setOnSucceeded(event -> {
            setBusy.accept(false);
            onSuccess.accept(task.getValue());
            statusSink.setStatus("Ready", false);
        });

        task.setOnFailed(event -> {
            setBusy.accept(false);
            Throwable exception = task.getException();
            statusSink.setStatus(exception == null ? "Request failed" : exception.getMessage(), true);
        });

        Thread thread = new Thread(task, "cycle-tracker-ui-task");
        thread.setDaemon(true);
        thread.start();
    }

    private static String resolveApiBaseUrl() {
        String configuredValue = System.getProperty("cycletracker.api.base-url");
        if (configuredValue == null || configuredValue.isBlank()) {
            return "http://localhost:8080";
        }
        return configuredValue.trim();
    }

    @FunctionalInterface
    private interface DashboardStatusSink {
        void setStatus(String message, boolean error);
    }
}
