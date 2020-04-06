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

package eu.dariolucia.reatmetric.processing.extension.internal;

import eu.dariolucia.reatmetric.api.processing.scripting.IBindingResolver;
import eu.dariolucia.reatmetric.processing.extension.ICheckExtension;

import java.time.Instant;
import java.util.Map;

public class NoCheck implements ICheckExtension {
    @Override
    public String getFunctionName() {
        return "__nocheck";
    }

    @Override
    public boolean check(Object currentValue, Instant generationTime, Map<String, String> properties, IBindingResolver resolver) {
        return false;
    }
}
