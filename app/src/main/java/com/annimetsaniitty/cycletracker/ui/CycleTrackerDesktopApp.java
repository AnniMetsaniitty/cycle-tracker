package com.annimetsaniitty.cycletracker.ui;

import javafx.application.Application;
import javafx.stage.Stage;

public class CycleTrackerDesktopApp extends Application {

    @Override
    public void start(Stage stage) {
        CycleTrackerDesktopController controller = new CycleTrackerDesktopController(stage);
        controller.start();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
