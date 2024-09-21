package com.ben.profile.dtos.responses;

import com.ben.profile.dtos.others.FileMetadata;
import com.ben.profile.enums.WriteStatus;
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
