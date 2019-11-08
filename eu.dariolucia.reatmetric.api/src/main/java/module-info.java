module eu.dariolucia.reatmetric.api {

    requires java.logging;

    exports eu.dariolucia.reatmetric.api;
    exports eu.dariolucia.reatmetric.api.alarms;
    exports eu.dariolucia.reatmetric.api.common;
    exports eu.dariolucia.reatmetric.api.common.exceptions;
    exports eu.dariolucia.reatmetric.api.events;
    exports eu.dariolucia.reatmetric.api.messages;
    exports eu.dariolucia.reatmetric.api.model;
    exports eu.dariolucia.reatmetric.api.parameters;
    exports eu.dariolucia.reatmetric.api.rawdata;
}