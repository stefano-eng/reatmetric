/*
 * Copyright (c) 2019.  Dario Lucia (dario.lucia@gmail.com)
 * All rights reserved
 *
 * Right to reproduce, use, modify and distribute (in whole or in part) this library for demonstrations/trainings/study/commercial purposes
 * shall be granted by the author in writing.
 */

package eu.dariolucia.reatmetric.processing;

import eu.dariolucia.ccsds.encdec.definition.Definition;

public interface IProcessingModelBuilder {

    void setOutput(IProcessingModelOutput output);

    void setCommandHandler(String commandType, ICommandHandler handler);

    IProcessingModel build(Definition definitionDatabase);
}
