package com.ben.profile.configs;

import com.ben.grpc.file.FileServiceGrpc;
import com.ben.grpc.identity.IdentityServiceGrpc;
import com.ben.profile.interceptors.GrpcClientInterceptor;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Random;

import static com.ben.profile.utils.Constants.MICROSERVICE_NAME;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class GrpcClientConfig {

    private final DiscoveryClient discoveryClient;

    @Bean
    public ManagedChannel identityServiceManagedChannel() {
        return createChannelForService("IDENTITY-SERVICE");
    }

    @Bean
    public ManagedChannel fileServiceManagedChannel() {
        return createChannelForService("FILE-SERVICE");
    }

    private ManagedChannel createChannelForService(String serviceName) {
        List<ServiceInstance> instances = discoveryClient.getInstances(serviceName);

        if (instances.isEmpty()) {
            log.error("[{}]: No available instances of {}", MICROSERVICE_NAME, serviceName);
            throw new RuntimeException("No available instances of " + serviceName);
        }

        ServiceInstance instance = instances.get(new Random().nextInt(instances.size()));

        log.info("[{}]: GRPC Instance: {} | Hostname: {} | Port: {}", serviceName,
                instance.getHost(),
                instance.getPort() % 10 + 10000,
                instance.getUri());

        return ManagedChannelBuilder.forAddress(instance.getHost(), instance.getPort() % 10 + 10000)
                .usePlaintext()
                .intercept(new GrpcClientInterceptor())
                .build();
    }

    @Bean
    public IdentityServiceGrpc.IdentityServiceBlockingStub identityServiceClient(ManagedChannel identityServiceManagedChannel) {
        return IdentityServiceGrpc.newBlockingStub(identityServiceManagedChannel);
    }

    @Bean
    public FileServiceGrpc.FileServiceStub fileServiceClient(ManagedChannel fileServiceManagedChannel) {
        return FileServiceGrpc.newStub(fileServiceManagedChannel);
    }

}
