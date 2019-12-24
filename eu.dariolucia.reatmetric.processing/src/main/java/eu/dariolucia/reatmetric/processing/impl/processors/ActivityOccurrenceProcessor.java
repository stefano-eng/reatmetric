/*
 * Copyright (c) 2019.  Dario Lucia (dario.lucia@gmail.com)
 * All rights reserved.
 *
 * Right to reproduce, use, modify and distribute (in whole or in part) this library for demonstrations/trainings/study/commercial purposes shall be granted by the author in writing.
 */

package eu.dariolucia.reatmetric.processing.impl.processors;

import eu.dariolucia.reatmetric.api.activity.ActivityOccurrenceData;
import eu.dariolucia.reatmetric.api.activity.ActivityOccurrenceReport;
import eu.dariolucia.reatmetric.api.activity.ActivityOccurrenceState;
import eu.dariolucia.reatmetric.api.activity.ActivityReportState;
import eu.dariolucia.reatmetric.api.common.AbstractDataItem;
import eu.dariolucia.reatmetric.api.common.IUniqueId;
import eu.dariolucia.reatmetric.api.common.LongUniqueId;
import eu.dariolucia.reatmetric.processing.ProcessingModelException;
import eu.dariolucia.reatmetric.processing.impl.operations.ActivityOccurrenceUpdateOperation;
import eu.dariolucia.reatmetric.processing.input.ActivityProgress;

import javax.script.ScriptException;
import java.time.Instant;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ActivityOccurrenceProcessor {

    private static final Logger LOG = Logger.getLogger(ActivityOccurrenceProcessor.class.getName());

    public static final String SELF_BINDING = "self";

    public static final String CREATION_STAGE_NAME = "Creation";
    public static final String RELEASE_TO_ACTIVITY_HANDLER_STAGE_NAME = "Release to Activity Handler";
    public static final String VERIFICATION_STAGE_NAME = "Verification";
    public static final String PURGE_STAGE_NAME = "Purge";

    private final ActivityProcessor parent;
    private final IUniqueId occurrenceId;
    private final Instant creationTime;
    private final Map<String, Object> arguments;
    private final Map<String, String> properties;
    private final List<ActivityOccurrenceReport> reports;
    private final String route;

    private ActivityOccurrenceState currentState;
    private Instant executionTime;

    private ActivityOccurrenceState currentTimeoutState;
    private Instant currentTimeoutAbsoluteTime;
    private TimerTask currentTimeoutTask;

    private List<ActivityOccurrenceData> temporaryDataItemList = new ArrayList<>(10);

    public ActivityOccurrenceProcessor(ActivityProcessor parent, IUniqueId occurrenceId, Instant creationTime, Map<String, Object> arguments, Map<String, String> properties, List<ActivityOccurrenceReport> reports, String route) {
        this.parent = parent;
        this.occurrenceId = occurrenceId;
        this.creationTime = creationTime;
        this.arguments = Collections.unmodifiableMap(arguments);
        this.properties = Collections.unmodifiableMap(properties);
        this.reports = reports;
        this.route = route;
    }

    public IUniqueId getOccurrenceId() {
        return occurrenceId;
    }

    /**
     * This method initialises the activity occurrence and forwards it to the selected activity handler.
     *
     * @return the list of state changes at the end of the dispatching
     */
    public List<AbstractDataItem> dispatch() {
        // Clear temporary list
        temporaryDataItemList.clear();
        // Set the initial state and generate the report for the creation of the activity occurrence (start of the lifecycle)
        currentState = ActivityOccurrenceState.CREATION;
        generateReport(CREATION_STAGE_NAME, creationTime,null, ActivityReportState.OK, null, ActivityOccurrenceState.RELEASE);
        // Forward occurrence to the activity handler
        boolean forwardOk = false;
        Instant nextTime = Instant.now();
        try {
            forwardOccurrence();
            forwardOk = true;
        } catch(Exception e) {
            LOG.log(Level.SEVERE, String.format("Failure forwarding activity occurrence %s of activity %s to the activity handler on route %s", occurrenceId, parent.getPath(), route), e);
        }
        // Generate ActivityOccurrenceReport and notify activity release: positive or negative if exception is thrown (FATAL)
        if(forwardOk) {
            generateReport(RELEASE_TO_ACTIVITY_HANDLER_STAGE_NAME, nextTime,null, ActivityReportState.OK, null, ActivityOccurrenceState.RELEASE);
        } else {
            generateReport(RELEASE_TO_ACTIVITY_HANDLER_STAGE_NAME, nextTime,null, ActivityReportState.FATAL, null, ActivityOccurrenceState.COMPLETION);
        }
        // Return list
        return List.copyOf(temporaryDataItemList);
    }

    /**
     * This method creates an activity occurrence with the given state. The activity occurrence is not forwarded to the
     * activity handler. This method is used to register activity occurrences that are created externally and not by
     * the Reatmetric system.
     *
     * @param progress the progress information to be used to derive the activity occurrence state
     * @return the list of state changes at the end of the creation
     */
    public List<AbstractDataItem> create(ActivityProgress progress) {
        // Clear temporary list
        temporaryDataItemList.clear();
        // Set the initial state and generate the report for the creation of the activity occurrence (start of the lifecycle)
        currentState = ActivityOccurrenceState.CREATION;
        generateReport(CREATION_STAGE_NAME, creationTime,progress.getExecutionTime(), ActivityReportState.OK, progress.getResult(), progress.getState());
        List<AbstractDataItem> toReturn = new LinkedList<>(temporaryDataItemList);
        // Process the progress state
        toReturn.addAll(progress(progress));
        // Return list
        return toReturn;
    }

    private void forwardOccurrence() throws ProcessingModelException {
        // Forward to the activity handler
        parent.processor.forwardActivityToHandler(occurrenceId, parent.getSystemEntityId(), parent.getPath(), parent.getDefinition().getType(), arguments, properties, route);
    }

    private void generateReport(String name, Instant generationTime, Instant executionTime, ActivityReportState reportState, Object result, ActivityOccurrenceState nextState) {
        // Create the report
        ActivityOccurrenceReport report = new ActivityOccurrenceReport(new LongUniqueId(parent.processor.getNextId(ActivityOccurrenceReport.class)), generationTime, null, name, currentState, executionTime, reportState, nextState, result);
        // Add the report to the list
        reports.add(report);
        // Set the current state
        currentState = nextState;
        // Set the execution time if any
        this.executionTime = executionTime != null ? executionTime : this.executionTime;
        // Generate the ActivityOccurrenceData and add it to the temporary list
        ActivityOccurrenceData activityOccurrenceData = new ActivityOccurrenceData(this.occurrenceId, creationTime, null, parent.getSystemEntityId(), parent.getPath().getLastPathElement(), parent.getPath(), parent.getDefinition().getType(), this.arguments, this.properties, List.copyOf(this.reports), this.route);
        temporaryDataItemList.add(activityOccurrenceData);
    }

    public List<AbstractDataItem> progress(ActivityProgress progress) {
        if(currentState == ActivityOccurrenceState.COMPLETION) {
            // Activity occurrence in its final state, update discarded
            if(LOG.isLoggable(Level.WARNING)) {
                LOG.warning(String.format("Reported progress of activity occurrence %s of activity %s completed", occurrenceId, parent.getPath()));
            }
            return Collections.emptyList();
        }
        if(progress.getNextState() == ActivityOccurrenceState.COMPLETION) {
            // Progress with COMPLETION as next state is not allowed
            if(LOG.isLoggable(Level.SEVERE)) {
                LOG.severe(String.format("Reported progress for activity occurrence %s of activity %s has next state set to COMPLETION, which is not supported. Reported states can be up to VERIFICATION.", occurrenceId, parent.getPath()));
            }
            return Collections.emptyList();
        }
        // Clear temporary list
        temporaryDataItemList.clear();
        // Generate ActivityOccurrenceReport according to progress
        ActivityOccurrenceState previousState = currentState;
        // A FATAL blocks the activity occurrence tracking
        ActivityOccurrenceState nextState = progress.getStatus() == ActivityReportState.FATAL ? ActivityOccurrenceState.COMPLETION : progress.getNextState();
        generateReport(progress.getName(), progress.getGenerationTime(), progress.getExecutionTime(), progress.getStatus(), progress.getResult(), nextState);

        // Enable timeout, if the situation is appropriate
        if(previousState != ActivityOccurrenceState.TRANSMISSION && currentState == ActivityOccurrenceState.TRANSMISSION) {
            // If progress triggers the transition to TRANSMISSION, start the TRANSMISSION timeout if specified
            if(parent.getDefinition().getTransmissionTimeout() > 0) {
                startTimeout(ActivityOccurrenceState.TRANSMISSION, parent.getDefinition().getTransmissionTimeout());
            }
        } else if(previousState != ActivityOccurrenceState.SCHEDULING && currentState == ActivityOccurrenceState.SCHEDULING) {
            // Stop transmission timeout
            stopTimeout(ActivityOccurrenceState.TRANSMISSION);
        } else if(previousState != ActivityOccurrenceState.EXECUTION && currentState == ActivityOccurrenceState.EXECUTION) {
            // Stop transmission timeout
            stopTimeout(ActivityOccurrenceState.TRANSMISSION);
            // If progress triggers the transition to EXECUTION, start the EXECUTION timeout if specified
            if(parent.getDefinition().getExecutionTimeout() > 0) {
                startTimeout(ActivityOccurrenceState.EXECUTION, parent.getDefinition().getExecutionTimeout());
            }
        } else if(previousState != ActivityOccurrenceState.VERIFICATION && currentState == ActivityOccurrenceState.VERIFICATION) {
            // Stop execution timeout
            stopTimeout(ActivityOccurrenceState.EXECUTION);
            // If progress triggers the transition to VERIFICATION, start the VERIFICATION timeout if specified and if an expression is defined
            if(parent.getDefinition().getVerification() != null && parent.getDefinition().getVerificationTimeout() > 0) {
                startTimeout(ActivityOccurrenceState.VERIFICATION, parent.getDefinition().getVerificationTimeout());
            }
            // If an expression is specified, run the expression now as verification (if in the correct state)
            if(parent.getDefinition().getVerification() != null) {
                try {
                    Boolean verificationResult = (Boolean) parent.getDefinition().getVerification().execute(parent.processor, Map.of(SELF_BINDING, this));
                    if(verificationResult) {
                        if(LOG.isLoggable(Level.INFO)) {
                            LOG.log(Level.INFO, String.format("Verification of activity occurrence %s of activity %s completed", occurrenceId, parent.getPath()));
                        }
                        // Activity occurrence confirmed by parameter data, add report with OK state and move state to COMPLETION
                        generateReport(VERIFICATION_STAGE_NAME, Instant.now(), null, ActivityReportState.OK, null, ActivityOccurrenceState.COMPLETION);
                    } else if(parent.getDefinition().getVerificationTimeout() == 0) {
                        if(LOG.isLoggable(Level.WARNING)) {
                            LOG.log(Level.WARNING, String.format("Verification of activity occurrence %s of activity %s failed", occurrenceId, parent.getPath()));
                        }
                        // No timeout, so derive the final state now
                        generateReport(VERIFICATION_STAGE_NAME, Instant.now(), null, ActivityReportState.FAIL, null, ActivityOccurrenceState.COMPLETION);
                    } else {
                        if(LOG.isLoggable(Level.FINE)) {
                            LOG.log(Level.FINE, String.format("Verification of activity occurrence %s of activity %s pending", occurrenceId, parent.getPath()));
                        }
                        // Expression not OK but there is a timeout, announce the PENDING
                        generateReport(VERIFICATION_STAGE_NAME, Instant.now(), null, ActivityReportState.PENDING, null, ActivityOccurrenceState.VERIFICATION);
                    }
                } catch (ScriptException|ClassCastException e) {
                    // Expression has a radical error
                    LOG.log(Level.SEVERE, String.format("Error while evaluating verification expression of activity occurrence %s of activity %s: %s", occurrenceId, parent.getPath(), e.getMessage()), e);
                    generateReport(VERIFICATION_STAGE_NAME, Instant.now(), null, ActivityReportState.ERROR, null, ActivityOccurrenceState.COMPLETION);
                }
            } else {
                if(LOG.isLoggable(Level.FINE)) {
                    LOG.log(Level.FINE, String.format("Verification of activity occurrence %s of activity %s completed. no expression defined", occurrenceId, parent.getPath()));
                }
                // If no expression is defined, move currentState to COMPLETION
                generateReport(VERIFICATION_STAGE_NAME, Instant.now(), null, ActivityReportState.OK, null, ActivityOccurrenceState.COMPLETION);
            }
        }
        // Verify timeout completions: this can generate an additional ActivityOccurrenceData object
        verifyTimeout();
        // Return list
        return List.copyOf(temporaryDataItemList);
    }

    private void abortTimeout() {
        if(this.currentTimeoutTask != null) {
            this.currentTimeoutTask.cancel();
            this.currentTimeoutTask = null;
            this.currentTimeoutState = null;
            this.currentTimeoutAbsoluteTime = null;
        }
    }

    private void stopTimeout(ActivityOccurrenceState theState) {
        if(currentTimeoutState == theState) {
            abortTimeout();
        }
    }

    private void startTimeout(ActivityOccurrenceState theState, int transmissionTimeout) {
        // If there is another timeout set, stop it
        abortTimeout();
        // Schedule operation to re-evaluate this occurrence at a given time
        this.currentTimeoutAbsoluteTime = Instant.now().plusSeconds(transmissionTimeout);
        this.currentTimeoutState = theState;
        this.currentTimeoutTask = new TimerTask() {
            @Override
            public void run() {
                if(currentTimeoutTask == this) {
                    parent.processor.scheduleTask(Collections.singletonList(new ActivityOccurrenceUpdateOperation(occurrenceId)));
                }
            }
        };
        this.parent.processor.scheduleAt(this.currentTimeoutAbsoluteTime, this.currentTimeoutTask);
    }

    private boolean verifyTimeout() {
        if(currentState == ActivityOccurrenceState.COMPLETION) {
            // Stop everything
            abortTimeout();
            return false;
        }
        // Check if the current timeout is applicable to the current state and if it is in timeout. If it is the case,
        // then generates a report and stop the timer
        if(this.currentTimeoutState == this.currentState) {
            Instant toCheck = Instant.now();
            if(this.currentTimeoutTask != null && (toCheck.equals(this.currentTimeoutAbsoluteTime) || toCheck.isAfter(this.currentTimeoutAbsoluteTime))) {
                generateReport(this.currentState.name() + " Timeout", Instant.now(), null, ActivityReportState.TIMEOUT, null, this.currentState);
                abortTimeout();
                return true;
            }
        }
        return false;
    }

    public List<AbstractDataItem> purge() {
        if(currentState == ActivityOccurrenceState.COMPLETION) {
            // Activity occurrence in its final state, update discarded
            if(LOG.isLoggable(Level.WARNING)) {
                LOG.warning(String.format("Purge request for activity occurrence %s of activity %s discarded, activity occurrence already completed", occurrenceId, parent.getPath()));
            }
            return Collections.emptyList();
        }
        // Clear temporary list
        temporaryDataItemList.clear();
        // Abort timeout
        abortTimeout();
        // Move to COMPLETION state
        generateReport(PURGE_STAGE_NAME, Instant.now(), null, ActivityReportState.OK, null, ActivityOccurrenceState.COMPLETION);
        // Return list
        return List.copyOf(temporaryDataItemList);
    }

    public List<AbstractDataItem> evaluate() {
        // Clear temporary list
        temporaryDataItemList.clear();
        // If currentTimeoutState is applicable, currentTimeoutTask is pending and it is expired, generate ActivityOccurrenceReport accordingly
        boolean expired = verifyTimeout();
        // If currentState is VERIFICATION, check expression: if OK, then announce ActivityOccurrenceReport OK and move to COMPLETION
        if(currentState == ActivityOccurrenceState.VERIFICATION) {
            try {
                Boolean verificationResult = (Boolean) parent.getDefinition().getVerification().execute(parent.processor, Map.of(SELF_BINDING, this));
                if(verificationResult) {
                    if(LOG.isLoggable(Level.INFO)) {
                        LOG.log(Level.INFO, String.format("Verification of activity occurrence %s of activity %s completed", occurrenceId, parent.getPath()));
                    }
                    // Activity occurrence confirmed by parameter data, add report with OK state and move state to COMPLETION
                    generateReport(VERIFICATION_STAGE_NAME, Instant.now(), null, ActivityReportState.OK, null, ActivityOccurrenceState.COMPLETION);
                } else if(expired) {
                    if(LOG.isLoggable(Level.WARNING)) {
                        LOG.log(Level.WARNING, String.format("Verification of activity occurrence %s of activity %s failed", occurrenceId, parent.getPath()));
                    }
                    // No timeout, so derive the final state now
                    generateReport(VERIFICATION_STAGE_NAME, Instant.now(), null, ActivityReportState.FAIL, null, ActivityOccurrenceState.COMPLETION);
                }
            } catch (ScriptException|ClassCastException e) {
                // Expression has a radical error
                LOG.log(Level.SEVERE, String.format("Error while evaluating verification expression of activity occurrence %s of activity %s: %s", occurrenceId, parent.getPath(), e.getMessage()), e);
                generateReport(VERIFICATION_STAGE_NAME, Instant.now(), null, ActivityReportState.ERROR, null, ActivityOccurrenceState.COMPLETION);
            }
        }
        // Return list
        return List.copyOf(temporaryDataItemList);
    }

    public Set<String> arguments() {
        return this.arguments.keySet();
    }

    public Object argument(String argName) {
        return this.arguments.get(argName);
    }

    public Set<String> properties() {
        return this.properties.keySet();
    }

    public String property(String argName) {
        return this.properties.get(argName);
    }

    public String route() {
        return this.route;
    }

    public Instant creationTime() {
        return creationTime;
    }

    public Instant executionTime() {
        return executionTime;
    }

}
