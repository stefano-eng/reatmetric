/*
 * Copyright (c)  2020 Dario Lucia (https://www.dariolucia.eu)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package eu.dariolucia.reatmetric.ui.controller;

import eu.dariolucia.reatmetric.api.common.RetrievalDirection;
import eu.dariolucia.reatmetric.api.common.exceptions.ReatmetricException;
import eu.dariolucia.reatmetric.api.messages.IOperationalMessageSubscriber;
import eu.dariolucia.reatmetric.api.messages.OperationalMessage;
import eu.dariolucia.reatmetric.api.messages.OperationalMessageFilter;
import eu.dariolucia.reatmetric.api.messages.Severity;
import eu.dariolucia.reatmetric.ui.ReatmetricUI;
import eu.dariolucia.reatmetric.ui.utils.InstantCellFactory;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.fxml.FXML;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.paint.Color;

import java.net.URL;
import java.time.Instant;
import java.util.List;
import java.util.ResourceBundle;

/**
 * FXML Controller class
 *
 * @author dario
 */
public class OperationalMessageViewController extends AbstractDataItemLogViewController<OperationalMessage, OperationalMessageFilter> implements IOperationalMessageSubscriber {

    @FXML
    private TableColumn<OperationalMessage, String> idCol;
    @FXML
    private TableColumn<OperationalMessage, Severity> severityCol;
    @FXML
    private TableColumn<OperationalMessage, Instant> genTimeCol;
    @FXML
    private TableColumn<OperationalMessage, String> sourceCol;
    @FXML
    private TableColumn<OperationalMessage, String> messageCol;

    @Override
    protected void doInitialize(URL url, ResourceBundle rb) {
        super.doInitialize(url, rb);
        
        this.idCol.setCellValueFactory(o -> new ReadOnlyObjectWrapper<>(o.getValue().getId()));
        this.severityCol.setCellValueFactory(o -> new ReadOnlyObjectWrapper<>(o.getValue().getSeverity()));
        this.genTimeCol.setCellValueFactory(o -> new ReadOnlyObjectWrapper<>(o.getValue().getGenerationTime()));
        this.sourceCol.setCellValueFactory(o -> new ReadOnlyObjectWrapper<>(o.getValue().getSource()));
        this.messageCol.setCellValueFactory(o -> new ReadOnlyObjectWrapper<>(o.getValue().getMessage()));

        this.genTimeCol.setCellFactory(new InstantCellFactory<>());
        this.severityCol.setCellFactory(column -> {
            return new TableCell<OperationalMessage, Severity>() {
                @Override
                protected void updateItem(Severity item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item != null && !empty && !isEmpty()) {
                        setText(item.name());
                        switch (item) {
                            case ALARM:
                                setTextFill(Color.DARKRED);
                                // setStyle("-fx-font-weight: bold");
                                // setStyle("-fx-font-weight: bold; -fx-background-color: DarkRed; -fx-border-width: 0 1 1 0; -fx-border-color: Black");
                                break;
                            case WARN:
                                setTextFill(Color.DARKORANGE);
                                // setStyle("-fx-font-weight: bold");
                                // setStyle("-fx-font-weight: bold; -fx-background-color: Chocolate; -fx-border-width: 0 1 1 0; -fx-border-color: Black");
                                break;
                            default:
                                setTextFill(Color.BLACK);
                                // setStyle("");
                                break;
                        }
                    } else {
                        setText("");
                        setGraphic(null);
                    }
                }
            };
        });
    }

    @Override
    public void dataItemsReceived(List<OperationalMessage> messages) {
        informDataItemsReceived(messages);
    }

    @Override
    protected void doServiceSubscribe(OperationalMessageFilter selectedFilter) throws ReatmetricException {
        ReatmetricUI.selectedSystem().getSystem().getOperationalMessageMonitorService().subscribe(this, selectedFilter);
    }

    @Override
    protected void doServiceUnsubscribe() throws ReatmetricException {
        ReatmetricUI.selectedSystem().getSystem().getOperationalMessageMonitorService().unsubscribe(this);
    }

    @Override
    protected List<OperationalMessage> doRetrieve(OperationalMessage om, int n, RetrievalDirection direction, OperationalMessageFilter filter) throws ReatmetricException {
        return ReatmetricUI.selectedSystem().getSystem().getOperationalMessageMonitorService().retrieve(om, n, direction, filter);
    }

    @Override
    protected List<OperationalMessage> doRetrieve(Instant selectedTime, int n, RetrievalDirection direction, OperationalMessageFilter filter) throws ReatmetricException {
        return ReatmetricUI.selectedSystem().getSystem().getOperationalMessageMonitorService().retrieve(selectedTime, n, direction, filter);
    }

    @Override
    protected Instant doGetGenerationTime(OperationalMessage om) {
        return om.getGenerationTime();
    }

    @Override
    protected URL doGetFilterWidget() {
        return getClass().getResource("/eu/dariolucia/reatmetric/ui/fxml/OperationalMessageFilterWidget.fxml");
    }

    @Override
    protected String doGetComponentId() {
        return "OperationalMessageView";
    }

}
