/*
 * Copyright (c) 2020.  Dario Lucia (dario.lucia@gmail.com)
 * All rights reserved.
 *
 * Right to reproduce, use, modify and distribute (in whole or in part) this library for demonstrations/trainings/study/commercial purposes shall be granted by the author in writing.
 */

package eu.dariolucia.reatmetric.core.impl.managers;

import eu.dariolucia.reatmetric.api.common.AbstractDataItem;
import eu.dariolucia.reatmetric.api.parameters.*;
import eu.dariolucia.reatmetric.api.processing.IProcessingModel;

public class ParameterDataAccessManager extends AbstractAccessManager<ParameterData, ParameterDataFilter, IParameterDataSubscriber> implements IParameterDataProvisionService {

    public ParameterDataAccessManager(IParameterDataArchive archive) {
        super(archive);
    }

    @Override
    protected Class<? extends AbstractDataItem> getSupportedClass() {
        return ParameterData.class;
    }

    @Override
    protected String getName() {
        return "Parameter Access Manager";
    }

    @Override
    protected AbstractAccessSubscriber<ParameterData, ParameterDataFilter, IParameterDataSubscriber> createSubscriber(IParameterDataSubscriber subscriber, ParameterDataFilter filter, IProcessingModel model) {
        return new ParameterDataAccessSubscriber(subscriber, filter, model);
    }
}
