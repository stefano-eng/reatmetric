/*
 * Copyright (c) 2019.  Dario Lucia (dario.lucia@gmail.com)
 * All rights reserved
 *
 * Right to reproduce, use, modify and distribute (in whole or in part) this library for demonstrations/trainings/study/commercial purposes
 * shall be granted by the author in writing.
 */


package eu.dariolucia.reatmetric.ui;

import eu.dariolucia.reatmetric.api.model.AlarmState;
import eu.dariolucia.reatmetric.ui.plugin.ReatmetricServiceHolder;
import eu.dariolucia.reatmetric.ui.preferences.PreferencesManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 *
 * @author dario
 */
public class ReatmetricUI extends Application {
    
    public static final String APPLICATION_NAME = "Reatmetric UI";
    
    public static final String APPLICATION_VERSION = "0.1.0";
    
    private static final ReatmetricServiceHolder SELECTED_SYSTEM = new ReatmetricServiceHolder();
    
    public static ReatmetricServiceHolder selectedSystem() {
        return SELECTED_SYSTEM;
    }
    
    private static final Map<Class<?>, ExecutorService> THREAD_POOL = new ConcurrentHashMap<>();
    
    public static ExecutorService threadPool(final Class<?> clazz) {
    	ExecutorService toReturn = null;
    	synchronized (THREAD_POOL) {
			toReturn = THREAD_POOL.get(clazz);
			if(toReturn == null) {
				toReturn = Executors.newFixedThreadPool(1, r -> {
                    Thread t = new Thread(r);
                    t.setName(clazz.getSimpleName() + " External Thread");
                    t.setDaemon(true);
                    return t;
                });
				THREAD_POOL.put(clazz, toReturn);
			}
		}
        return toReturn;
    }
    
    private static final PreferencesManager PREFERENCES = new PreferencesManager();
    
    public static PreferencesManager preferences() {
        return PREFERENCES;
    }
    
    private static Label STATUS_LABEL = null;

	private static Consumer<AlarmState> STATUS_INDICATOR = null;
    
    public static void registerStatusLabel(Label l) {
        STATUS_LABEL = l;
    }
    
    public static void setStatusLabel(String s) {
        Platform.runLater(() -> {
            if(STATUS_LABEL != null) {
                STATUS_LABEL.setText(s);
            }
        });
    }
    
    public static void registerStatusIndicator(Consumer<AlarmState> statusIndicator) {
		STATUS_INDICATOR = statusIndicator;
	}
    
    public static void setStatusIndicator(AlarmState t) {
    	STATUS_INDICATOR.accept(t);
    }
    
    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(ReatmetricUI.class.getResource("/eu/dariolucia/reatmetric/ui/fxml/MainView.fxml"));
        
        Scene scene = new Scene(root);
        // scene.getStylesheets().add(getClass().getResource("/eu/dariolucia/reatmetric/ui/css/MainView.css").toExternalForm());
        
        stage.setScene(scene);
        stage.setTitle("Reatmetric UI");
        
        stage.setOnCloseRequest(event -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Exit Reatmetric UI");
            alert.setHeaderText("Exit Reatmetric UI");
            String s = "Do you want to close Reatmetric UI?";
            alert.setContentText(s);
            alert.getDialogPane().getStylesheets().add(getClass().getResource("/eu/dariolucia/reatmetric/ui/fxml/css/MainView.css").toExternalForm());
            Optional<ButtonType> result = alert.showAndWait();
            if ((result.isPresent()) && (result.get() == ButtonType.OK)) {
                Platform.exit();
            } else {
                event.consume();
            }
        });
        
        stage.show();
    }

    @Override
    public void stop() {
        ReatmetricUI.threadPool(ReatmetricUI.class).shutdown();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
}
