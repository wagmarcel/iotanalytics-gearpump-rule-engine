package com.intel.ruleengine.gearpump.graph;

import org.apache.gearpump.streaming.javaapi.Processor;

import java.util.*;

/**
 * Copyright (c) 2016 Intel Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
class GraphDefinition {


    private final Map<Processor, List<Processor>> definition;

    private final Processor kafkaSourceObservationsProcessor;
    private final Processor kafkaSourceRulesUpdateProcessor;
    private final Processor kafkaSinkHeartbeatProcessor;

    private final Processor checkObservationInRules;
    private final Processor sendAlerts;
    private final Processor downloadRulesTask;
    private final Processor persistRulesTask;
    private final Processor persistComponentAlerts;
    private final Processor checkRules;
    private final Processor getRulesForComponent;
    private final Processor persistObservation;
    private final Processor heartbeatTask;

    GraphDefinition(ProcessorsBuilder processorsBuilder) {
        sendAlerts = processorsBuilder.getSendAlertsProcessor();
        checkObservationInRules = processorsBuilder.getCheckObservationInRulesProcessor();
        downloadRulesTask = processorsBuilder.getDownloadRulesProcessor();
        persistRulesTask = processorsBuilder.getPersistRulesProcessor();
        persistComponentAlerts = processorsBuilder.getPersistComponentAlertsProccesor();
        checkRules = processorsBuilder.getCheckRulesProcessor();
        getRulesForComponent = processorsBuilder.getRulesForComponentProcessor();
        kafkaSourceObservationsProcessor = processorsBuilder.getKafkaSourceObservations();
        kafkaSourceRulesUpdateProcessor = processorsBuilder.getKafkaSourceRulesUpdate();
        persistObservation = processorsBuilder.getPersistObservationProcessor();
        heartbeatTask = processorsBuilder.getHeartbeatProcessor();
        kafkaSinkHeartbeatProcessor = processorsBuilder.getKafkaSinkHeartbeat();
        this.definition = new HashMap<>();
        buildDefinition();
    }

    public Map<Processor, List<Processor>> getDefinition() {
        return definition;
    }

    private void buildDefinition() {
        definition.put(kafkaSourceObservationsProcessor, Arrays.asList(getRulesForComponent));
        definition.put(getRulesForComponent, Arrays.asList(persistObservation));
        definition.put(persistObservation, Arrays.asList(checkObservationInRules));
        definition.put(checkObservationInRules, Arrays.asList(persistComponentAlerts));
        definition.put(persistComponentAlerts, Arrays.asList(checkRules));
        definition.put(checkRules, Arrays.asList(sendAlerts));
        definition.put(sendAlerts, new ArrayList<>());

        definition.put(kafkaSourceRulesUpdateProcessor, Arrays.asList(downloadRulesTask));
        definition.put(downloadRulesTask, Arrays.asList(persistRulesTask));
        definition.put(persistRulesTask, new ArrayList<>());

        definition.put(heartbeatTask, Arrays.asList(kafkaSinkHeartbeatProcessor));
        definition.put(kafkaSinkHeartbeatProcessor, new ArrayList<>());
    }
}
