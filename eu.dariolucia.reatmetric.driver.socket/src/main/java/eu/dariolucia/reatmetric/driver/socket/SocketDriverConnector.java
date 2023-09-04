/*
 * Copyright (c)  2023 Dario Lucia (https://www.dariolucia.eu)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *           http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package eu.dariolucia.reatmetric.driver.socket;

import eu.dariolucia.reatmetric.api.common.Pair;
import eu.dariolucia.reatmetric.api.model.AlarmState;
import eu.dariolucia.reatmetric.api.transport.AbstractTransportConnector;
import eu.dariolucia.reatmetric.api.transport.TransportConnectionStatus;
import eu.dariolucia.reatmetric.api.transport.exceptions.TransportException;
import eu.dariolucia.reatmetric.driver.socket.configuration.SocketConfiguration;
import eu.dariolucia.reatmetric.driver.socket.configuration.connection.AbstractConnectionConfiguration;
import eu.dariolucia.reatmetric.driver.socket.configuration.connection.InitType;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SocketDriverConnector extends AbstractTransportConnector {

    private static final Logger LOG = Logger.getLogger(SocketDriverConnector.class.getName());

    private final SocketConfiguration configuration;
    public SocketDriverConnector(SocketConfiguration configuration) {
        super(configuration.getName(), configuration.getDescription());
        this.configuration = configuration;
    }

    @Override
    protected Pair<Long, Long> computeBitrate() {
        return null;
    }

    @Override
    protected void doConnect() {
        updateAlarmState(AlarmState.NOT_APPLICABLE);
        updateConnectionStatus(TransportConnectionStatus.CONNECTING);
        int failedConnections = 0;
        int connectionsToOpen = 0;
        for(AbstractConnectionConfiguration con : configuration.getConnections()) {
            if(con.getInit() == InitType.CONNECTOR) {
                ++connectionsToOpen;
                try {
                    con.openConnection();
                    // TODO: start an internal reading thread in the connection class. Read data goes to RouteConfiguration
                } catch (IOException e) {
                    ++failedConnections;
                    LOG.log(Level.WARNING, "Cannot open connection '" + con.getName() + "'");
                }
            }
        }
        if(failedConnections == 0) {
            updateConnectionStatus(TransportConnectionStatus.OPEN);
            updateAlarmState(AlarmState.NOMINAL);
        } else if(failedConnections < connectionsToOpen) {
            // At least one connection is open
            updateConnectionStatus(TransportConnectionStatus.OPEN);
            updateAlarmState(AlarmState.WARNING);
        } else {
            // All connections failed
            updateConnectionStatus(TransportConnectionStatus.ERROR);
            updateAlarmState(AlarmState.ALARM);
        }
    }

    @Override
    protected void doDisconnect() throws TransportException {
        updateAlarmState(AlarmState.NOT_APPLICABLE);
        updateConnectionStatus(TransportConnectionStatus.DISCONNECTING);
        for(AbstractConnectionConfiguration con : configuration.getConnections()) {
            if(con.getInit() == InitType.CONNECTOR) {
                try {
                    con.closeConnection();
                } catch (IOException e) {
                    LOG.log(Level.WARNING, "Cannot close connection '" + con.getName() + "'");
                }
            }
        }
        updateConnectionStatus(TransportConnectionStatus.IDLE);
        updateAlarmState(AlarmState.NOMINAL);
    }

    @Override
    protected void doDispose() {
        // Nothing specific to do
    }

    @Override
    public void abort() throws TransportException, RemoteException {
        disconnect();
    }
}
