package com.ben.identity.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record IntrospectRequest (

    @NotNull
    @NotBlank
    String token

) {

}