package com.ben.identity.dtos.responses;

import java.util.Date;

public record SendEmailForgotPasswordResponse(

    String message,

    Date retryAfter

) {

}