/*
 * Copyright (c) 2019.  Dario Lucia (dario.lucia@gmail.com)
 * All rights reserved
 *
 * Right to reproduce, use, modify and distribute (in whole or in part) this library for demonstrations/trainings/study/commercial purposes
 * shall be granted by the author in writing.
 */


package eu.dariolucia.reatmetric.ui.plugin;

import eu.dariolucia.reatmetric.api.IServiceFactory;

/**
 *
 * @author dario
 */
public interface IMonitoringCentreServiceListener {
    
    public void systemAdded(IServiceFactory system);
    
    public void systemRemoved(IServiceFactory system);
    
}
