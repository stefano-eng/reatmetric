/*
 * Copyright (c) 2019.  Dario Lucia (dario.lucia@gmail.com)
 * All rights reserved
 *
 * Right to reproduce, use, modify and distribute (in whole or in part) this library for demonstrations/trainings/study/commercial purposes
 * shall be granted by the author in writing.
 */


package eu.dariolucia.reatmetric.ui.controller;

import eu.dariolucia.reatmetric.api.IServiceFactory;
import eu.dariolucia.reatmetric.api.common.AbstractDataItem;
import eu.dariolucia.reatmetric.api.common.AbstractDataItemFilter;
import eu.dariolucia.reatmetric.api.common.RetrievalDirection;
import eu.dariolucia.reatmetric.api.common.exceptions.ReatmetricException;
import eu.dariolucia.reatmetric.ui.ReatmetricUI;
import eu.dariolucia.reatmetric.ui.utils.DataProcessingDelegator;
import eu.dariolucia.reatmetric.ui.utils.ReferenceProperty;
import eu.dariolucia.reatmetric.ui.utils.TableViewUtil;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.stage.Popup;

import java.io.IOException;
import java.net.URL;
import java.time.Instant;
import java.util.*;
import java.util.function.Consumer;

/**
 * FXML Controller class
 *
 * @author dario
 */
public abstract class AbstractDataItemCurrentViewController<T extends AbstractDataItem, V extends AbstractDataItemFilter> extends AbstractDisplayController {

    // Pane control
    @FXML
    protected TitledPane displayTitledPane;

    // Live/retrieval controls
    @FXML
    protected ToggleButton liveTgl;
    @FXML
    protected Button goToStartBtn;
    @FXML
    protected Button goBackFastBtn;
    @FXML
    protected Button goBackOneBtn;
    @FXML
    protected Button goToEndBtn;
    @FXML
    protected Button goForwardFastBtn;
    @FXML
    protected Button goForwardOneBtn;
    @FXML
    protected Button selectTimeBtn;
    
    @FXML
    protected Button filterBtn;
    
    @FXML
    protected Button printBtn;

    // Progress indicator for data retrieval
    @FXML
    protected ProgressIndicator progressIndicator;

    // Table
    @FXML
    protected TableView<ReferenceProperty<T>> dataItemTableView;

    protected final Map<Integer, ReferenceProperty<T>> id2item = new HashMap<>();

    // Popup selector for date/time
    protected final Popup dateTimePopup = new Popup();

    // Popup selector for filter
    protected final Popup filterPopup = new Popup();
    
    // Time selector controller
    protected DateTimePickerWidgetController dateTimePickerController;
    
    // Filter controller
    protected IFilterController<V> dataItemFilterController;
    
    // Temporary object queue
    protected DataProcessingDelegator<T> delegator;
       
    /**
     * Initializes the controller class.
     */
    @Override
    protected void doInitialize(URL url, ResourceBundle rb) {
    	this.dataItemTableView.setPlaceholder(new Label(""));
    	
    	if(this.liveTgl != null) {
	        this.goToStartBtn.disableProperty().bind(this.liveTgl.selectedProperty());
	        this.goBackFastBtn.disableProperty().bind(this.liveTgl.selectedProperty());
	        this.goBackOneBtn.disableProperty().bind(this.liveTgl.selectedProperty());
	        this.goToEndBtn.disableProperty().bind(this.liveTgl.selectedProperty());
	        this.goForwardFastBtn.disableProperty().bind(this.liveTgl.selectedProperty());
	        this.goForwardOneBtn.disableProperty().bind(this.liveTgl.selectedProperty());
	        this.selectTimeBtn.disableProperty().bind(this.liveTgl.selectedProperty());
    	}
    	
        this.dateTimePopup.setAutoHide(true);
        this.dateTimePopup.setHideOnEscape(true);

        try {
            URL datePickerUrl = getClass().getResource("/eu/dariolucia/reatmetric/ui/fxml/DateTimePickerWidget.fxml");
            FXMLLoader loader = new FXMLLoader(datePickerUrl);
            Parent dateTimePicker = loader.load();
            this.dateTimePickerController = loader.getController();
            this.dateTimePopup.getContent().addAll(dateTimePicker);
            // Load the controller hide with select
            this.dateTimePickerController.setActionAfterSelection(() -> {
                this.dateTimePopup.hide();
                moveToTime(this.dateTimePickerController.getSelectedTime(), RetrievalDirection.TO_PAST, getNumVisibleRow() * 2, this.dataItemFilterController.getSelectedFilter());
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        this.filterPopup.setAutoHide(true);
        this.filterPopup.setHideOnEscape(true);
        
        try {
            URL filterWidgetUrl = doGetFilterWidget();
            FXMLLoader loader = new FXMLLoader(filterWidgetUrl);
            Parent filterSelector = loader.load();
            this.dataItemFilterController = loader.getController();
            this.filterPopup.getContent().addAll(filterSelector);
            // Load the controller hide with select
            this.dataItemFilterController.setActionAfterSelection(() -> {
                this.filterPopup.hide();
                applyFilter(this.dataItemFilterController.getSelectedFilter());
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.delegator = new DataProcessingDelegator<>(doGetComponentId(), buildIncomingDataDelegatorAction());
    }

    protected Consumer<List<T>> buildIncomingDataDelegatorAction() {
        return (List<T> t) -> {
            addDataItems(t, true, true);
        };
    }
    
    @FXML
    protected void liveToggleSelected(ActionEvent e) {
        if (this.liveTgl.isSelected()) {
        	clearTable();
        	startSubscription();
        } else {
            stopSubscription();
            updateSelectTime();
        }
    }

    @FXML
    protected void filterButtonSelected(ActionEvent e) {
        if (this.filterPopup.isShowing()) {
            this.filterPopup.hide();
        } else {
            Bounds b = this.filterBtn.localToScreen(this.filterBtn.getBoundsInLocal());
            this.filterPopup.setX(b.getMinX());
            this.filterPopup.setY(b.getMaxY());
            this.filterPopup.getScene().getRoot().getStylesheets().add(getClass().getResource("/eu/dariolucia/reatmetric/ui/fxml/css/MainView.css").toExternalForm());
            this.filterPopup.show(this.displayTitledPane.getScene().getWindow());
        }
    }

    @FXML
    protected void goToStart(ActionEvent e) {
        if(!isProgressBusy()) {
            moveToTime(Instant.MIN, RetrievalDirection.TO_FUTURE, 1, this.dataItemFilterController.getSelectedFilter());
        }
    }

    @FXML
    protected void goBackFast(ActionEvent e) {
        if(!isProgressBusy()) {
            fetchRecords(getNumVisibleRow(), RetrievalDirection.TO_PAST);
        }
    }

    @FXML
    protected void goBackOne(ActionEvent e) {
        if(!isProgressBusy()) {
            fetchRecords(1, RetrievalDirection.TO_PAST);
        }
    }

    @FXML
    protected void goToEnd(ActionEvent e) {
        if(!isProgressBusy()) {
            moveToTime(Instant.MAX, RetrievalDirection.TO_PAST, getNumVisibleRow() * 2, this.dataItemFilterController.getSelectedFilter());
        }
    }

    @FXML
    protected void goForwardFast(ActionEvent e) {
        if(!isProgressBusy()) {
            fetchRecords(getNumVisibleRow(), RetrievalDirection.TO_FUTURE);
        }
    }

    @FXML
    protected void goForwardOne(ActionEvent e) {
        if(!isProgressBusy()) {
            fetchRecords(1, RetrievalDirection.TO_FUTURE);
        }
    }

    protected void fetchRecords(int n, RetrievalDirection direction) {
        if(dataItemTableView.getItems().isEmpty()) {
            return;
        }
        // Get the first message in the table
        T om = direction == RetrievalDirection.TO_FUTURE ? getFirst() : getLast();
        // Retrieve the next one and add it on top
        markProgressBusy();
        ReatmetricUI.threadPool(getClass()).execute(() -> {
            try {
                List<T> messages = doRetrieve(om, n, direction, this.dataItemFilterController.getSelectedFilter());
                addDataItems(messages, false, true);
            } catch (Exception e) {
                e.printStackTrace();
            }
            markProgressReady();
        });
    }

    @FXML
    protected void selectTimeButtonSelected(ActionEvent e) {
        if (this.dateTimePopup.isShowing()) {
            this.dateTimePopup.hide();
        } else {
            Bounds b = this.selectTimeBtn.localToScreen(this.selectTimeBtn.getBoundsInLocal());
            this.dateTimePopup.setX(b.getMinX());
            this.dateTimePopup.setY(b.getMaxY());
            this.dateTimePopup.getScene().getRoot().getStylesheets().add(getClass().getResource("/eu/dariolucia/reatmetric/ui/fxml/css/MainView.css").toExternalForm());
            this.dateTimePopup.show(this.displayTitledPane.getScene().getWindow());
        }
    }

    @Override
    protected void doSystemDisconnected(IServiceFactory system, boolean oldState) {
        if(this.liveTgl != null) {
            this.liveTgl.setSelected(false);
        }
        this.displayTitledPane.setDisable(true);

        if(oldState) {
            persistColumnConfiguration();
        }
        // Clear the table                
        clearTable();
        // Remove the additional header fields
        removeAdditionalHeaderFields();
    }

    @Override
    protected void doSystemConnected(IServiceFactory system, boolean oldState) {
        if(this.liveTgl != null) {
            this.liveTgl.setSelected(true);
        }
        this.displayTitledPane.setDisable(false);

        // Restore column configuration
        restoreColumnConfiguration();
        // Start subscription if there
        if (this.liveTgl == null || this.liveTgl.isSelected()) {
        	clearTable();
            startSubscription();
        }
    }
    
    private void restoreColumnConfiguration() {
        TableViewUtil.restoreColumnConfiguration(this.system.getSystem(), this.user, doGetComponentId(), this.dataItemTableView);
    }
    
    private void persistColumnConfiguration() {
        TableViewUtil.persistColumnConfiguration(this.system.getSystem(), this.user, doGetComponentId(), this.dataItemTableView);
    }

    protected void informDataItemsReceived(List<T> objects) {
        this.delegator.delegate(objects);
    }

    protected void addDataItems(List<T> messages, boolean fromLive, boolean addOnTop) {
        Platform.runLater(() -> {
            if (!this.displayTitledPane.isDisabled() && (!fromLive || (fromLive && (this.liveTgl == null || this.liveTgl.isSelected())))) {
                for(T apd : messages) {
                    System.out.println("Checking... " + apd);
                    int externalId = doGetItemId(apd);
                    ReferenceProperty<T> prop = id2item.get(externalId);
                    if(prop != null) {
                        // In table already
                        if(doCheckForItemRemoval(apd)) {
                            // If in the table, remove it from the table
                            System.out.println("REMOVE: " + apd);
                            this.id2item.remove(externalId);
                            this.dataItemTableView.getItems().remove(prop);
                        } else {
                            // If it is already in the table, then update the value in place
                            System.out.println("UPDATE: " + apd);
                            prop.set(apd);
                        }
                    } else {
                        // Not in table
                        if(doCheckForItemAddition(apd)) {
                            // If not in the table, add it to top if addOnTop is true, add it to bottom if addOnTop is false
                            System.out.println("ADD: " + apd);
                            prop = new ReferenceProperty<>(apd);
                            this.id2item.put(doGetItemId(apd), prop);
                            if(addOnTop) {
                                this.dataItemTableView.getItems().add(0, prop);
                            } else {
                                this.dataItemTableView.getItems().add(prop);
                            }
                        }
                    }
                }
                if (!fromLive) {
                    this.dataItemTableView.scrollTo(0);
                }
                this.dataItemTableView.refresh();
                updateSelectTime();
            }
        });
    }

    protected abstract boolean doCheckForItemAddition(T apd);

    protected abstract boolean doCheckForItemRemoval(T apd);

    protected abstract int doGetItemId(T apd);

    protected final void startSubscription() {
        ReatmetricUI.threadPool(getClass()).execute(() -> {
        	this.delegator.resume();
            try {
                doServiceSubscribe(getCurrentFilter());
            } catch (ReatmetricException e) {
                e.printStackTrace();
            }
        });
    }

    protected final void stopSubscription() {
        ReatmetricUI.threadPool(getClass()).execute(() -> {
        	try {
                doServiceUnsubscribe();
            } catch (ReatmetricException e) {
                e.printStackTrace();
            }
        	this.delegator.suspend();
        });
    }

    protected void updateSelectTime() {
    	if(this.selectTimeBtn == null) {
    		return;
    	}
        // Take the first item from the table and use the generation time as value of the text
        if (this.dataItemTableView.getItems().isEmpty()) {
            this.selectTimeBtn.setText("---");
        } else {
            ReferenceProperty<T> om = this.dataItemTableView.getItems().get(0);
            Instant time = doGetGenerationTime(om.get());
            this.selectTimeBtn.setText(formatTime(time));
            this.dateTimePickerController.setSelectedTime(time);
        }
    }

    protected void clearTable() {
        this.dataItemTableView.getItems().clear();
        this.dataItemTableView.layout();
        this.dataItemTableView.refresh();
        updateSelectTime();
    }

    private void removeAdditionalHeaderFields() {
        this.dataItemTableView.layout();
        this.dataItemTableView.refresh();
    }

    private void moveToTime(Instant selectedTime, RetrievalDirection direction, int n, V currentFilter) {
    	if(this.selectTimeBtn != null) {
    		this.selectTimeBtn.setText(formatTime(selectedTime));
    	}
    	
        markProgressBusy();
        ReatmetricUI.threadPool(getClass()).execute(() -> {
            try {
                List<T> messages = doRetrieve(selectedTime, n, direction, currentFilter);
                addDataItems(messages, false, true);
            } catch (Exception e) {
                e.printStackTrace();
            }
            markProgressReady();
        });
    }

    private void markProgressBusy() {
        this.progressIndicator.setVisible(true);
    }

    private void markProgressReady() {
        Platform.runLater(() -> {
            this.progressIndicator.setVisible(false);
        });
    }

    private boolean isProgressBusy() {
        return this.progressIndicator.isVisible();
    }
    
    private T getFirst() {
        return this.dataItemTableView.getItems().get(0).get();
    }

    private T getLast() {
        return this.dataItemTableView.getItems().get(this.dataItemTableView.getItems().size() - 1).get();
    }

    private int getNumVisibleRow() {
        double h = this.dataItemTableView.getHeight();
        h -= 30; // Header 
        return (int) (h / this.dataItemTableView.getFixedCellSize()) + 1;
    }

    protected void applyFilter(V selectedFilter) {
    	this.dataItemFilterController.setSelectedFilter(selectedFilter);
        if(this.liveTgl == null || this.liveTgl.isSelected()) {
            if(selectedFilter == null || selectedFilter.isClear()) {
                ReatmetricUI.threadPool(getClass()).execute(() -> {
                    try {
                        doServiceSubscribe(selectedFilter);
                        markFilterDeactivated();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            } else {
                ReatmetricUI.threadPool(getClass()).execute(() -> {
                    try {
                        doServiceSubscribe(selectedFilter);
                        markFilterActivated();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        } else {
            moveToTime(this.dateTimePickerController.getSelectedTime(), RetrievalDirection.TO_PAST, getNumVisibleRow(), selectedFilter);
        }
    }

    private void markFilterDeactivated() {
        this.filterBtn.setStyle("");
    }

    private void markFilterActivated() {
        this.filterBtn.setStyle("-fx-background-color: -fx-faint-focus-color");
    }
   
    @Override
    protected Control doBuildNodeForPrinting() {
        List<?> items = new ArrayList<>(this.dataItemTableView.getItems());
        TableView<?> cloned = new TableView<>(FXCollections.observableArrayList(items));
        double width = 0;
        double height = this.dataItemTableView.getItems().size() * 24 + 30;
        for(TableColumn tc : this.dataItemTableView.getColumns()) {
            TableColumn newTc = new TableColumn();
            newTc.setText(tc.getText());
            newTc.setCellFactory(tc.getCellFactory());
            newTc.setCellValueFactory(tc.getCellValueFactory());
            newTc.setPrefWidth(tc.getWidth());
            width += tc.getWidth();
            cloned.getColumns().add(newTc);
        }
        cloned.setPrefWidth(width);
        cloned.setPrefHeight(height);
        return cloned;
    }
    
    protected V getCurrentFilter() {
    	return this.dataItemFilterController != null ? this.dataItemFilterController.getSelectedFilter() : null; 
    }

    protected abstract void doServiceSubscribe(V selectedFilter) throws ReatmetricException;
    
    protected abstract void doServiceUnsubscribe() throws ReatmetricException;
    
    protected abstract List<T> doRetrieve(T om, int n, RetrievalDirection direction, V filter) throws ReatmetricException;

    protected abstract List<T> doRetrieve(Instant selectedTime, int n, RetrievalDirection direction, V filter) throws ReatmetricException;

    protected abstract Instant doGetGenerationTime(T om);

    protected abstract URL doGetFilterWidget();
    
    protected abstract String doGetComponentId();
    
}
