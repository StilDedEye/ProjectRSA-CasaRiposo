module com.drimtim.projectrsacasariposo {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    requires java.desktop;
    requires java.management;
    // Modulo corretto per Selenium

    opens com.drimtim.projectrsacasariposo.MAIN_client to javafx.fxml;
    exports com.drimtim.projectrsacasariposo.MAIN_server;
    exports com.drimtim.projectrsacasariposo.MAIN_client;
    opens com.drimtim.projectrsacasariposo.MAIN_server to javafx.fxml;
}
