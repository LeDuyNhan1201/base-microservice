package com.ben.profile.repositories;

import com.ben.profile.dtos.debezium.ConnectionRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "debezium-client", url = "http://localhost:6063")
public interface DebeziumClient {

    @GetMapping(value = "/connectors", produces = MediaType.APPLICATION_JSON_VALUE)
    List<String> getConnections();

    @PostMapping(value = "/connectors", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity createConnection(@RequestBody ConnectionRequest request);

    @DeleteMapping(value = "/connectors/{name}", produces = MediaType.APPLICATION_JSON_VALUE)
    void deleteConnection(@PathVariable String name);

}