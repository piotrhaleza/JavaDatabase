module com.example.destkopapp {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens com.example.destkopapp to javafx.fxml;
    exports com.example.destkopapp;
}