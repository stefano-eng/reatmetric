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

package eu.dariolucia.reatmetric.driver.spacecraft.definition;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class TimeCorrelationServiceConfiguration {

    @XmlAttribute(name = "generation-frame-period")
    private int generationPeriod = 256; // The generation period of the time packet in number of frames

    @XmlAttribute(name = "on-board-delay")
    private long onBoardDelay = 0L; // The amount of microseconds for the onboard delay

    @XmlAttribute(name = "generation-period-reported")
    private boolean generationPeriodReported = false; // Whether the generation period is reported

    @XmlAttribute(name = "maximum-frame-time-delay")
    private long maximumFrameTimeDelay = 1200000000L; // The maximum delay in microseconds that it is allows to match a frame identified by the generation period with the corresponding time packet

    @XmlElement(name = "time-format", required = true)
    private CucConfiguration timeFormat; // The time format in the time packet

    public int getGenerationPeriod() {
        return generationPeriod;
    }

    public void setGenerationPeriod(int generationPeriod) {
        this.generationPeriod = generationPeriod;
    }

    public long getOnBoardDelay() {
        return onBoardDelay;
    }

    public void setOnBoardDelay(long onBoardDelay) {
        this.onBoardDelay = onBoardDelay;
    }

    public boolean isGenerationPeriodReported() {
        return generationPeriodReported;
    }

    public void setGenerationPeriodReported(boolean generationPeriodReported) {
        this.generationPeriodReported = generationPeriodReported;
    }

    public CucConfiguration getTimeFormat() {
        return timeFormat;
    }

    public void setTimeFormat(CucConfiguration timeFormat) {
        this.timeFormat = timeFormat;
    }

    public long getMaximumFrameTimeDelay() {
        return maximumFrameTimeDelay;
    }

    public void setMaximumFrameTimeDelay(long maximumFrameTimeDelay) {
        this.maximumFrameTimeDelay = maximumFrameTimeDelay;
    }
}
