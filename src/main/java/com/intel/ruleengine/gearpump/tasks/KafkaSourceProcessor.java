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

package com.intel.ruleengine.gearpump.tasks;

import org.apache.gearpump.cluster.UserConfig;
import org.apache.gearpump.cluster.client.ClientContext;
import org.apache.gearpump.streaming.javaapi.Processor;
import org.apache.gearpump.streaming.kafka.KafkaSource;
import org.apache.gearpump.streaming.kafka.KafkaStoreFactory;
import org.apache.gearpump.streaming.kafka.util.KafkaConfig;
import kafka.api.OffsetRequest;
import org.apache.gearpump.streaming.state.impl.InMemoryCheckpointStoreFactory;
import org.apache.gearpump.streaming.transaction.api.CheckpointStoreFactory;

import java.util.Properties;

public class KafkaSourceProcessor {

    public static final String KAFKA_TOPIC_PROPERTY = "KAFKA_TOPIC";
    public static final String KAFKA_URI_PROPERTY = "KAFKA_URI";
    public static final String KAFKA_ZOOKEEPER_PROPERTY = "KAFKA_URI_ZOOKEEPER";

    private static String name;

    private final KafkaSource kafkaSource;
    private final ClientContext context;

    public KafkaSourceProcessor(UserConfig userConfig, String name, String topic) {
        this.name = name;

        String zookeeperQuorum = userConfig.getString(KAFKA_ZOOKEEPER_PROPERTY).get();
        String serverUri = userConfig.getString(KAFKA_URI_PROPERTY).get();

        Properties props = new Properties();
        props.put(KafkaConfig.ZOOKEEPER_CONNECT_CONFIG, zookeeperQuorum);
        props.put(KafkaConfig.GROUP_ID_CONFIG, "gearpump");
        // todo what is the default storage on TAP?
        props.put(KafkaConfig.CHECKPOINT_STORE_NAME_PREFIX_CONFIG, "gearpump");
        props.put(KafkaConfig.BOOTSTRAP_SERVERS_CONFIG, serverUri);
                props.put(KafkaConfig.CONSUMER_START_OFFSET_CONFIG,
                new java.lang.Long(OffsetRequest.LatestTime()));

        KafkaStoreFactory kafkaStoreFactory = new KafkaStoreFactory(props);
        kafkaSource = new KafkaSource(topic, props);
        kafkaSource.setCheckpointStore(kafkaStoreFactory);
        context = ClientContext.apply();
    }

    public Processor getKafkaSourceProcessor(int parallelProcessorNumber) {
        return Processor.source(kafkaSource, parallelProcessorNumber, name, UserConfig.empty(), context.system());
    }
}
