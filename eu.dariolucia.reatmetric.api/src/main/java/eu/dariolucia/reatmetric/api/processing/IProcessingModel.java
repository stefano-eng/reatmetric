/*
 * Copyright (c) 2020.  Dario Lucia (dario.lucia@gmail.com)
 * All rights reserved.
 *
 * Right to reproduce, use, modify and distribute (in whole or in part) this library for demonstrations/trainings/study/commercial purposes shall be granted by the author in writing.
 */

package eu.dariolucia.reatmetric.api.processing;

import eu.dariolucia.reatmetric.api.activity.ActivityOccurrenceData;
import eu.dariolucia.reatmetric.api.common.AbstractDataItem;
import eu.dariolucia.reatmetric.api.common.AbstractDataItemFilter;
import eu.dariolucia.reatmetric.api.common.IUniqueId;
import eu.dariolucia.reatmetric.api.common.Pair;
import eu.dariolucia.reatmetric.api.model.SystemEntity;
import eu.dariolucia.reatmetric.api.model.SystemEntityPath;
import eu.dariolucia.reatmetric.api.processing.exceptions.ProcessingModelException;
import eu.dariolucia.reatmetric.api.processing.input.ActivityProgress;
import eu.dariolucia.reatmetric.api.processing.input.ActivityRequest;
import eu.dariolucia.reatmetric.api.processing.input.EventOccurrence;
import eu.dariolucia.reatmetric.api.processing.input.ParameterSample;

import java.util.List;

// TODO introduce a dispose operation, to avoid archive exceptions when the system is shutdown
public interface IProcessingModel {

    void injectParameters(List<ParameterSample> sampleList);

    void raiseEvent(EventOccurrence event);

    IUniqueId startActivity(ActivityRequest request) throws ProcessingModelException;

    IUniqueId createActivity(ActivityRequest request, ActivityProgress currentProgress) throws ProcessingModelException;

    void reportActivityProgress(ActivityProgress progress);

    void purgeActivities(List<Pair<Integer, IUniqueId>> activityOccurrenceIds) throws ProcessingModelException;

    List<ActivityOccurrenceData> getActiveActivityOccurrences();

    // TODO add a way to set parameter value

    void visit(IProcessingModelVisitor visitor);

    List<AbstractDataItem> get(AbstractDataItemFilter<?> filter);

    List<AbstractDataItem> getByPath(List<SystemEntityPath> paths) throws ProcessingModelException;

    List<AbstractDataItem> getById(List<Integer> ids) throws ProcessingModelException;

    void enable(SystemEntityPath path) throws ProcessingModelException;

    void disable(SystemEntityPath path) throws ProcessingModelException;

    SystemEntity getRoot() throws ProcessingModelException;

    List<SystemEntity> getContainedEntities(SystemEntityPath path) throws ProcessingModelException;

    SystemEntity getSystemEntityAt(SystemEntityPath path) throws ProcessingModelException;

    SystemEntity getSystemEntityOf(int id) throws ProcessingModelException;

    int getExternalIdOf(SystemEntityPath path) throws ProcessingModelException;

    SystemEntityPath getPathOf(int id) throws ProcessingModelException;

    void registerActivityHandler(IActivityHandler handler) throws ProcessingModelException;

    void deregisterActivityHandler(IActivityHandler handler) throws ProcessingModelException;
}
