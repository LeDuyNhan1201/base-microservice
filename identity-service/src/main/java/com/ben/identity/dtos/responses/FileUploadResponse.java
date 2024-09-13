package com.ben.identity.dtos.responses;

import com.ben.identity.dtos.others.FileMetadata;
import com.ben.identity.enums.WriteStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FileUploadResponse {

    FileMetadata fileMetadata;

    WriteStatus uploadStatus;

}
