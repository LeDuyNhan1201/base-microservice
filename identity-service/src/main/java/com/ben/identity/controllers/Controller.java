package com.ben.identity.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.ben.identity.components.Translator.getLocalizedMessage;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping("/test")
@Tag(name = "Test APIs")
@RequiredArgsConstructor
@Slf4j
public class Controller {

    @Operation(summary = "Hello", description = "First API")
    @GetMapping
    @ResponseStatus(OK)
    public ResponseEntity<?> hello() {
        return ResponseEntity.status(OK)
                .body(getLocalizedMessage("msg_test", "Ben"));
    }

}
