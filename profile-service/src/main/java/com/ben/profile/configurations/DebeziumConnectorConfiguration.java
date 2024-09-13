package com.ben.profile.configurations;

import com.ben.profile.dtos.debezium.Config;
import com.ben.profile.dtos.debezium.ConnectionRequest;
import com.ben.profile.exceptions.AppErrorCode;
import com.ben.profile.exceptions.AppException;
import com.ben.profile.repositories.DebeziumClient;
import feign.FeignException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static com.ben.profile.exceptions.AppErrorCode.DEBEZIUM_CONNECT_FAILED;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DebeziumConnectorConfiguration {

    private final DebeziumClient debeziumClient;

    private final String CONNECTION_NAME = "profile-service-mysql-connector";

    @PostConstruct
    public void checkAndCreateConnection() {
        try {
            List<String> connections = debeziumClient.getConnections();

            if (!connections.contains(CONNECTION_NAME)) {
                ConnectionRequest request = ConnectionRequest.builder()
                        .name(CONNECTION_NAME)
                        .config(Config.builder()
                                .connectorClass("io.debezium.connector.mysql.MySqlConnector")
                                .tasksMax("1")
                                .databaseHostname("base-microservice-profile-service-mysql")
                                .databasePort("3306")
                                .databaseUser("ben1201")
                                .databasePassword("Ben1201#")
                                .databaseServerId("1")
                                .databaseServerName("profile-service-mysql-db")
                                .databaseIncludeList("profile_service_db")
                                .topicPrefix("profile_service")
                                .schemaHistoryKafkaBootstrapServers("base-microservice-kafka-broker:9092")
                                .schemaHistoryKafkaTopic("schema-changes.profile_service_db")
                                .build())
                        .build();
                createConnectionAndValidate(request);
            } else {
                log.info("Connection '{}' already exists. No action needed.", CONNECTION_NAME);
            }
        } catch (Exception e) {
            log.error("Failed to process Debezium connections", e);
            throw new AppException(DEBEZIUM_CONNECT_FAILED, BAD_REQUEST, "Failed to get connections");
        }
    }

    private void createConnectionAndValidate(ConnectionRequest request) {
        try {
            debeziumClient.createConnection(request);
            log.info("Debezium connection created successfully.");

        } catch (FeignException e) {
            if (e.status() != 201) {
                log.error("Failed to create connection: HTTP Status {}", e.status());
                throw new AppException(DEBEZIUM_CONNECT_FAILED, BAD_REQUEST, "Failed to create Debezium connection");
            }
        }
    }

}