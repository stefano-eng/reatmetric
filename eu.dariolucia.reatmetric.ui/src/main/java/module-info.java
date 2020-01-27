open module eu.dariolucia.reatmetric.ui {

    requires java.logging;

    requires javafx.graphics;
    requires javafx.fxml;
    requires javafx.base;
    requires javafx.controls;

    requires eu.dariolucia.reatmetric.api;

    uses eu.dariolucia.reatmetric.api.IServiceFactory;
}