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


package eu.dariolucia.reatmetric.api.events;

import eu.dariolucia.reatmetric.api.common.IDataItemProvisionService;
import eu.dariolucia.reatmetric.api.common.exceptions.ReatmetricException;
import eu.dariolucia.reatmetric.api.model.SystemEntityPath;

/**
 *
 * @author dario
 */
public interface IEventDataProvisionService extends IDataItemProvisionService<IEventDataSubscriber, EventDataFilter, EventData> {

    EventDescriptor getDescriptor(SystemEntityPath path) throws ReatmetricException;

    EventDescriptor getDescriptor(int externalId) throws ReatmetricException;
}
