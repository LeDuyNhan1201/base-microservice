package com.ben.profile.dtos.debezium;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserProfileDataChanges {

    String id;

    String first_name;

    String last_name;

    String user_id;

    String avatar_id;

}