module com.george.chess {
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.base;
    requires javafx.fxml;  // If you're using FXML
    requires javafx.media; // If using media functionality
    requires javafx.swing; // If using Swing interoperability
    requires java.logging; // Required to access java.util.logging

    // Allow JavaFX to access your window classes
    opens com.george.window to javafx.graphics;
}
