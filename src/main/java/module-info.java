module com.abdullah.rps {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.abdullah.rps to javafx.fxml;
    exports com.abdullah.rps;
}