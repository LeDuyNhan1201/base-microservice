package com.ben.profile.configs;

import com.ben.profile.dtos.debezium.Config;
import com.ben.profile.dtos.debezium.ConnectionRequest;
import com.ben.profile.exceptions.AppException;
import com.ben.profile.repositories.externals.DebeziumRestClient;
import feign.FeignException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.List;

import static com.ben.profile.utils.Constants.MICROSERVICE_NAME;
import static com.ben.profile.exceptions.AppErrorCode.DEBEZIUM_CONNECT_FAILED;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DebeziumConnectorConfig {

    private final DebeziumRestClient debeziumRestClient;

    @Value("${spring.debezium.connectors[0].name}")
    private String connectionName;

    @Value("${spring.debezium.connectors[0].config.connector.class}")
    private String connectorClass;

    @Value("${spring.debezium.connectors[0].config.tasks.max}")
    private String tasksMax;

    @Value("${spring.debezium.connectors[0].config.database.hostname}")
    private String databaseHostname;

    @Value("${spring.debezium.connectors[0].config.database.port}")
    private String databasePort;

    @Value("${spring.debezium.connectors[0].config.database.user}")
    private String databaseUser;

    @Value("${spring.debezium.connectors[0].config.database.password}")
    private String databasePassword;

    @Value("${spring.debezium.connectors[0].config.database.server.id}")
    private String databaseServerId;

    @Value("${spring.debezium.connectors[0].config.database.server.name}")
    private String databaseServerName;

    @Value("${spring.debezium.connectors[0].config.database.include.list}")
    private String databaseIncludeList;

    @Value("${spring.debezium.connectors[0].config.topic.prefix}")
    private String topicPrefix;

    @Value("${spring.debezium.connectors[0].config.schema.history.internal.kafka.bootstrap.servers}")
    private String schemaHistoryInternalKafkaBootstrapServers;

    @Value("${spring.debezium.connectors[0].config.schema.history.internal.kafka.topic}")
    private String schemaHistoryInternalKafkaTopic;

    @PostConstruct
    public void checkAndCreateConnection() {
        try {
            List<String> connections = debeziumRestClient.getConnections();

            if (!connections.contains(connectionName)) {
                ConnectionRequest request = ConnectionRequest.builder()
                        .name(connectionName)
                        .config(Config.builder()
                                .connectorClass(connectorClass)
                                .tasksMax(tasksMax)
                                .databaseHostname(databaseHostname)
                                .databasePort(databasePort)
                                .databaseUser(databaseUser)
                                .databasePassword(databasePassword)
                                .databaseServerId(databaseServerId)
                                .databaseServerName(databaseServerName)
                                .databaseIncludeList(databaseIncludeList)
                                .topicPrefix(topicPrefix)
                                .schemaHistoryInternalKafkaBootstrapServers(schemaHistoryInternalKafkaBootstrapServers)
                                .schemaHistoryInternalKafkaTopic(schemaHistoryInternalKafkaTopic)
                                .build())
                        .build();
                createConnectionAndValidate(request);
            } else {
                log.info("[{}]: Connection '{}' already exists. No action needed.", MICROSERVICE_NAME, connectionName);
            }
        } catch (Exception e) {
            log.error("[{}]: Failed to process Debezium connections", MICROSERVICE_NAME, e);
            throw new AppException(DEBEZIUM_CONNECT_FAILED, BAD_REQUEST, "Failed to get connections");
        }
    }

    private void createConnectionAndValidate(ConnectionRequest request) {
        try {
            debeziumRestClient.createConnection(request);
            log.info("[{}]: Debezium connection created successfully.", MICROSERVICE_NAME);

        } catch (FeignException e) {
            if (e.status() != 201) {
                log.error("[{}]: Failed to create connection: HTTP Status {}", MICROSERVICE_NAME, e.status());
                throw new AppException(DEBEZIUM_CONNECT_FAILED, BAD_REQUEST, "Failed to create Debezium connection");
            }
        }
    }

}