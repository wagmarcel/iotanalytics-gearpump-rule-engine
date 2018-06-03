/*
 * Copyright (c) 2016 Intel Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.intel.ruleengine.gearpump.tasks.processors;

import com.intel.ruleengine.gearpump.data.RuleConditionsRepository;
import com.intel.ruleengine.gearpump.data.StatisticsRepository;
import com.intel.ruleengine.gearpump.data.alerts.RuleConditionsHbaseRepository;
import com.intel.ruleengine.gearpump.data.statistics.StatisticsHbaseRepository;
import com.intel.ruleengine.gearpump.rules.RulesObservationChecker;
import com.intel.ruleengine.gearpump.tasks.InvalidMessageTypeException;
import com.intel.ruleengine.gearpump.tasks.RuleEngineTask;
import com.intel.ruleengine.gearpump.tasks.messages.RulesWithObservation;
import com.intel.ruleengine.gearpump.tasks.messages.controllers.MessageReceiver;
import org.apache.gearpump.Message;
import org.apache.gearpump.cluster.UserConfig;
import org.apache.gearpump.streaming.javaapi.Processor;
import org.apache.gearpump.streaming.task.TaskContext;

import java.util.List;
import java.util.stream.Collectors;


public class CheckObservationInRulesTask extends RuleEngineTask {

    private static final String TASK_NAME = "verifySingleRulesCondition";

    private final RuleConditionsRepository ruleConditionsRepository;
    private final StatisticsRepository statisticsRepository;

    public CheckObservationInRulesTask(TaskContext context, UserConfig userConf) {
        this(context, userConf, new RuleConditionsHbaseRepository(userConf), new StatisticsHbaseRepository(userConf));
    }

    public CheckObservationInRulesTask(TaskContext context, UserConfig userConf,
                                       RuleConditionsRepository ruleConditionsRepository,
                                       StatisticsRepository statisticsRepository) {
        super(context, userConf);
        this.ruleConditionsRepository = ruleConditionsRepository;
        this.statisticsRepository = statisticsRepository;
    }

    @Override
    public void onNext(Message message) {
        try {
            List<RulesWithObservation> rulesWithObservation = MessageReceiver.build(message).getMessage();
            List<RulesWithObservation> checkedRulesWithObservation =
                    rulesWithObservation.stream()
                            .map(ruleWithObservation -> new RulesObservationChecker(ruleWithObservation, ruleConditionsRepository, statisticsRepository)
                                    .checkRulesForObservation())
                            .collect(Collectors.toList());

            getMessageSender().send(checkedRulesWithObservation);
        } catch (InvalidMessageTypeException ex) {
            getLogger().error("Incorrect format of message found - {}", message.msg().getClass().getCanonicalName());
        }
    }

    public static Processor getProcessor(UserConfig config, int parallelProcessorNumber) {
        return createProcessor(CheckObservationInRulesTask.class, config, parallelProcessorNumber, TASK_NAME);
    }

}
