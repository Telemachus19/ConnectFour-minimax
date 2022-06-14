module com.connectfourgui {
    requires javafx.controls;
    requires javafx.fxml;

    requires com.almasb.fxgl.all;

    opens com.connectfourgui to javafx.fxml;
    exports com.connectfourgui;
}