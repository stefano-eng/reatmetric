/*
 * Copyright (c) 2020.  Dario Lucia (dario.lucia@gmail.com)
 * All rights reserved.
 *
 * Right to reproduce, use, modify and distribute (in whole or in part) this library for demonstrations/trainings/study/commercial purposes shall be granted by the author in writing.
 */

package eu.dariolucia.reatmetric.core.impl.managers;

import eu.dariolucia.reatmetric.api.common.Pair;
import eu.dariolucia.reatmetric.api.parameters.IParameterDataSubscriber;
import eu.dariolucia.reatmetric.api.parameters.ParameterData;
import eu.dariolucia.reatmetric.api.parameters.ParameterDataFilter;
import eu.dariolucia.reatmetric.api.processing.IProcessingModel;

public class ParameterDataAccessSubscriber extends AbstractAccessSubscriber<ParameterData, ParameterDataFilter, IParameterDataSubscriber> {

    public ParameterDataAccessSubscriber(IParameterDataSubscriber subscriber, ParameterDataFilter filter, IProcessingModel model) {
        super(subscriber, filter, model);
    }

    @Override
    protected Pair<Integer, Long> computeId(ParameterData item) {
        return Pair.of(item.getExternalId(), 0L);
    }

    @Override
    protected String getName() {
        return "Parameter Access Subscriber";
    }

}