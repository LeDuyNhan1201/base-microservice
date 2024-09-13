package com.ben.profile.dtos.debezium;

import lombok.*;
import lombok.experimental.FieldDefaults;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SchemaChanges<T> {

    Payload<T> payload;

}
