package com.ben.identity.dtos.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SignUpRequest (

        @NotNull(message = "null_email")
        @NotBlank(message = "blank_email")
        @Email(message = "invalid_email")
        String email,

        @NotNull(message = "null_firstName")
        @NotBlank(message = "blank_firstName")
        @Size(min = 2, max = 20, message = "size_firstName")
        String firstName,

        @NotNull(message = "null_lastName")
        @NotBlank(message = "blank_lastName")
        @Size(min = 2, max = 20, message = "size_lastName")
        String lastName,

        @NotNull(message = "null_password")
        @NotBlank(message = "blank_password")
        @Size(min = 6, max = 20, message = "size_password")
        String password,

        @NotNull(message = "null_password")
        @NotBlank(message = "blank_password")
        @Size(min = 6, max = 20, message = "size_password")
        String passwordConfirmation

) {

}