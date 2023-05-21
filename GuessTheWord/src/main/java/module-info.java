module com.example.guesstheword {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;


    opens com.example.guesstheword to javafx.fxml;
    exports com.example.guesstheword;
}