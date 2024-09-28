package com.ben.profile.controllers;

import com.ben.profile.dtos.responses.CommonResponse;
import com.ben.profile.repositories.externals.FileServiceGrpcClient;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;

import static com.ben.common.Utils.handleRawFile;
import static com.ben.profile.components.Translator.getLocalizedMessage;
import static com.ben.profile.utils.Constants.MICROSERVICE_NAME;
import static org.springframework.http.HttpStatus.*;

@RestController
@RequestMapping("/user-profile")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Tag(name = "User Profile APIs")
public class UserProfileController {

    FileServiceGrpcClient fileServiceGrpcClient;

    @Operation(summary = "Update", description = "Update user profile")
    @PutMapping
    @ResponseStatus(OK)
    ResponseEntity<?> update() {
        return ResponseEntity.status(OK).body(getLocalizedMessage("profile_update_success"));
    }

    @Operation(summary = "Upload avatar", description = "Upload user avatar")
    @PostMapping("/upload-avatar/{userId}")
    @ResponseStatus(OK)
    @PreAuthorize("#userId == authentication.name")
    public ResponseEntity<?> upload(@PathVariable String userId, @RequestPart MultipartFile image) {
        String fileExtension = Objects.requireNonNull(image.getOriginalFilename()).split("\\.")[1];
        try {
            handleRawFile(userId);
            return ResponseEntity.status(OK).body(fileServiceGrpcClient.uploadFile(userId.concat("." + fileExtension), image));

        } catch (Exception e) {
            log.error("[{}]: Error: ", MICROSERVICE_NAME, e);
            return ResponseEntity.status(BAD_REQUEST).body(
                    CommonResponse.builder().message(getLocalizedMessage("avatar_upload_failed")).build());
        }
    }

}