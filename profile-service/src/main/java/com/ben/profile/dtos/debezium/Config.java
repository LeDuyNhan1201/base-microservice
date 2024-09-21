package com.ben.profile.dtos.debezium;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Config {

    @JsonProperty("connector.class")
    String connectorClass;

    @JsonProperty("tasks.max")
    String tasksMax;

    @JsonProperty("database.hostname")
    String databaseHostname;

    @JsonProperty("database.port")
    String databasePort;

    @JsonProperty("database.user")
    String databaseUser;

    @JsonProperty("database.password")
    String databasePassword;

    @JsonProperty("database.server.id")
    String databaseServerId;

    @JsonProperty("database.server.name")
    String databaseServerName;

    @JsonProperty("database.include.list")
    String databaseIncludeList;

    @JsonProperty("topic.prefix")
    String topicPrefix;

    @JsonProperty("schema.history.internal.kafka.bootstrap.servers")
    String schemaHistoryInternalKafkaBootstrapServers;

    @JsonProperty("schema.history.internal.kafka.topic")
    String schemaHistoryInternalKafkaTopic;

}
