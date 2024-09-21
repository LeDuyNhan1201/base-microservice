package com.ben.file.configs;

//@Configuration
//@Slf4j
//@RequiredArgsConstructor
public class GrpcServerConfig {

//    @NonFinal
//    @Value("${server.port}")
//    private int port;
//
//    private final FileServiceGrpc.FileServiceImplBase fileService;
//
//    @Bean
//    public Server grpcServer() throws IOException, InterruptedException {
//        Server server = ServerBuilder.forPort(port % 10 + 10000)
///*                                 .useTransportSecurity(
//                                     new File("file-service/certs/file.crt"),
//                                     new File("file-service/certs/file.pem")
//                                 )*/
//                                 .addService(fileService)
//                                 .build();
//        server.start();
//        log.info("[{}]: gRPC Server started with TLS at on port: {}", MICROSERVICE_NAME, server.getPort());
//
//        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
//            log.info("[{}]: Shutting down gRPC Server", MICROSERVICE_NAME);
//
//            if (server != null) server.shutdown();
//        }));
//
//        return server;
//    }
}
