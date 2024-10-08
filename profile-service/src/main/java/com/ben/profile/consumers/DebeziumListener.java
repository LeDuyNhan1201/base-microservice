package com.ben.profile.consumers;

import static java.util.stream.Collectors.toMap;

//@Slf4j
//@Component
public class DebeziumListener {

//    private final Executor executor = Executors.newSingleThreadExecutor();
//    private final DebeziumEngine<RecordChangeEvent<SourceRecord>> debeziumEngine;
//
//    public DebeziumListener(Configuration customerConnectorConfiguration) {
//        this.debeziumEngine = DebeziumEngine.create(ChangeEventFormat.of(Connect.class))
//            .using(customerConnectorConfiguration.asProperties())
//            .notifying(this::handleChangeEvent)
//            .build();
//    }
//
//    private void handleChangeEvent(RecordChangeEvent<SourceRecord> sourceRecordRecordChangeEvent) {
//        var sourceRecord = sourceRecordRecordChangeEvent.record();
//        log.info("Key = {}, Value = {}", sourceRecord.key(), sourceRecord.value());
//        var sourceRecordChangeValue= (Struct) sourceRecord.value();
//        log.info("SourceRecordChangeValue = '{}'", sourceRecordChangeValue);
//         if (sourceRecordChangeValue != null) {
//             Operation operation = Operation.forCode((String) sourceRecordChangeValue.get(OPERATION));
//
////         Operation.READ operation events are always triggered when application initializes
////         We're only interested in CREATE operation which are triggered upon new insert registry
//             if(operation != Operation.READ) {
//                 String record = operation == Operation.DELETE ? BEFORE : AFTER; // Handling Update & Insert operations.
//
//                 Struct struct = (Struct) sourceRecordChangeValue.get(record);
//                 Map<String, Object> payload = struct.schema().fields().stream()
//                     .map(Field::name)
//                     .filter(fieldName -> struct.get(fieldName) != null)
//                     .map(fieldName -> Pair.of(fieldName, struct.get(fieldName)))
//                     .collect(toMap(Pair::getKey, Pair::getValue));
//
//                 log.info("Updated Data: {} with Operation: {}", payload, operation.name());
//             }
//         }
//    }
//
//    @PostConstruct
//    private void start() {
//        this.executor.execute(debeziumEngine);
//    }
//
//    @PreDestroy
//    private void stop() throws IOException {
//        if (Objects.nonNull(this.debeziumEngine)) {
//            this.debeziumEngine.close();
//        }
//    }

}