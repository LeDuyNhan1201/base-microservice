package com.ben.identity.controllers;

import com.ben.identity.dtos.responses.CommonResponse;
import com.ben.identity.services.grpc_services.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import static com.ben.identity.Utils.handleRawFile;
import static com.ben.identity.components.Translator.getLocalizedMessage;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping("/test")
@Tag(name = "Test APIs")
@RequiredArgsConstructor
@Slf4j
public class Controller {

    private final FileService fileService;

    @Operation(summary = "Hello", description = "First API")
    @GetMapping
    @ResponseStatus(OK)
    public ResponseEntity<?> hello() {
        return ResponseEntity.status(OK)
                .body(getLocalizedMessage("msg_test", "Ben"));
    }

    @Operation(summary = "Upload test", description = "Upload test")
    @PostMapping("/upload")
    @ResponseStatus(OK)
    @PreAuthorize("authentication.name == 'benlun99999@gmail.com'")
    public ResponseEntity<?> upload(@RequestParam String filePath, @RequestPart MultipartFile file) {
        String fileExtension = file.getOriginalFilename().split("\\.")[1];
        try {
            handleRawFile(filePath);
            return ResponseEntity.status(OK).body(fileService.uploadFile(filePath.concat("." + fileExtension), file));

        } catch (Exception e) {
            return ResponseEntity.status(BAD_REQUEST).body(
                    CommonResponse.builder().message(getLocalizedMessage("file_upload_failed")).build());
        }
    }

}
