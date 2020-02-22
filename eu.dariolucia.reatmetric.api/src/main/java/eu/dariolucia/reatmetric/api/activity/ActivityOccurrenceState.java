/*
 * Copyright (c) 2019.  Dario Lucia (dario.lucia@gmail.com)
 * All rights reserved.
 *
 * Right to reproduce, use, modify and distribute (in whole or in part) this library for demonstrations/trainings/study/commercial purposes shall be granted by the author in writing.
 */

package eu.dariolucia.reatmetric.api.activity;

public enum ActivityOccurrenceState {
    CREATION("Creation"),
    RELEASE("Release"),
    TRANSMISSION("Transmission"),
    SCHEDULING("Scheduling"),
    EXECUTION("Execution"),
    VERIFICATION("Verification"),
    COMPLETION("Completion");

    String formatString;

    ActivityOccurrenceState(String formatString) {
        this.formatString = formatString;
    }

    public String getFormatString() {
        return formatString;
    }
}