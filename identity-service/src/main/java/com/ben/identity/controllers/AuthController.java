package com.ben.identity.controllers;

import com.ben.identity.annotations.RateLimit;
import com.ben.identity.dtos.others.Tokens;
import com.ben.identity.dtos.requests.*;
import com.ben.identity.dtos.responses.*;
import com.ben.identity.entities.User;
import com.ben.identity.exceptions.AppException;
import com.ben.identity.mappers.UserMapper;
import com.ben.identity.services.AuthService;
import com.ben.identity.services.UserService;
import com.nimbusds.jose.JOSEException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import static com.ben.identity.components.Translator.getLocalizedMessage;
import static com.ben.identity.enums.RateLimitKeyType.BY_IP;
import static com.ben.identity.enums.RateLimitKeyType.BY_TOKEN;
import static com.ben.identity.exceptions.AppErrorCode.INVALID_SIGNATURE;
import static org.springframework.http.HttpStatus.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Tag(name = "Authentication APIs")
public class AuthController {

    AuthService authService;

    UserService userService;

    UserMapper userMapper = UserMapper.INSTANCE;

    @Operation(summary = "Sign up", description = "Create new user")
    @PostMapping("/sign-up")
    @ResponseStatus(CREATED)
    ResponseEntity<SignUpResponse> signUp(@RequestBody @Valid SignUpRequest request) {
        User user = userMapper.toUser(request);

        authService.signUp(user, request.passwordConfirmation(), request.firstName(), request.lastName());

        return ResponseEntity.status(CREATED).body(
                new SignUpResponse(getLocalizedMessage("sign_up_success")));
    }

    @Operation(summary = "Send email verification", description = "Send email verification")
    @PostMapping("/send-email-verification")
    @ResponseStatus(OK)
    ResponseEntity<SendEmailVerificationResponse> sendEmailVerification(@RequestBody @Valid SendEmailVerificationRequest request) {
        authService.sendEmailVerification(request.email(), request.type());

        return ResponseEntity.status(OK).body(
                new SendEmailVerificationResponse(getLocalizedMessage("resend_verification_email_success")));
    }

    @Operation(summary = "Verify email by code", description = "Verify email by code")
    @PostMapping("/verify-email-by-code")
    @ResponseStatus(OK)
    ResponseEntity<VerifyEmailResponse> verifyEmail(@RequestBody @Valid VerifyEmailByCodeRequest request) {
        User user = userService.findByEmail(request.email());

        authService.verifyEmail(user, request.code(), null);

        return ResponseEntity.status(OK).body(
                new VerifyEmailResponse(getLocalizedMessage("verify_email_success")));
    }

    @Operation(summary = "Verify email by token", description = "Verify email by token")
    @GetMapping("/verify-email-by-token")
    @ResponseStatus(OK)
    ResponseEntity<VerifyEmailResponse> verifyEmail(@RequestParam(name = "token") String token) {
        authService.verifyEmail(null, null, token);

        return ResponseEntity.status(OK).body(
                new VerifyEmailResponse(getLocalizedMessage("verify_email_success")));
    }

    @Operation(summary = "Sign in", description = "Authenticate user and return token")
    @PostMapping("/sign-in")
    @ResponseStatus(OK)
    @RateLimit(limitKeyTypes = { BY_IP })
    ResponseEntity<SignInResponse> signIn(@RequestBody @Valid SignInRequest request) {
        User signInUser = authService.signIn(request.email(), request.password());

        String accessToken = authService.generateToken(signInUser, false);

        String refreshToken = authService.generateToken(signInUser, true);

        return ResponseEntity.status(OK).body(
                SignInResponse.builder()
                        .tokens(new Tokens(accessToken, refreshToken))
                        .user(userMapper.toUserInfo(signInUser)).build()
        );
    }

    @Operation(summary = "Refresh", description = "Refresh token")
    @PostMapping("/refresh")
    @ResponseStatus(OK)
    @RateLimit(limitKeyTypes = { BY_TOKEN })
    ResponseEntity<RefreshResponse> refresh(@RequestBody @Valid RefreshRequest request, HttpServletRequest httpServletRequest) {
        String newAccessToken;
        try {
            newAccessToken = authService.refresh(request.refreshToken(), httpServletRequest);

        } catch (ParseException | JOSEException e) {
            throw new AppException(INVALID_SIGNATURE, UNPROCESSABLE_ENTITY, "Invalid signature");
        }

        return ResponseEntity.status(OK).body(new RefreshResponse(
                getLocalizedMessage("refresh_token_success"),
                newAccessToken
        ));
    }

    @Operation(summary = "Sign out", description = "Sign out user")
    @PostMapping("/sign-out")
    @ResponseStatus(OK)
    void signOut(@RequestBody @Valid SignOutRequest request) {
        try {
            authService.signOut(request.accessToken(), request.refreshToken());

        } catch (ParseException | JOSEException e) {
            throw new AppException(INVALID_SIGNATURE, UNPROCESSABLE_ENTITY, "Invalid signature");
        }
    }

    @Operation(summary = "Send email forgot password", description = "Send email forgot password")
    @PostMapping("/send-forgot-password")
    @ResponseStatus(OK)
    ResponseEntity<SendEmailForgotPasswordResponse> sendEmailForgotPassword(
            @RequestBody @Valid SendEmailForgotPasswordRequest request) {
        authService.sendEmailForgotPassword(request.email());

        return ResponseEntity.status(OK).body(new SendEmailForgotPasswordResponse(
                getLocalizedMessage("send_forgot_password_email_success"),
                Date.from(Instant.now().plus(1, ChronoUnit.MINUTES))
        ));
    }

    @Operation(summary = "Verify forgot password code", description = "Verify forgot password code")
    @PostMapping("/forgot-password")
    @ResponseStatus(OK)
    ResponseEntity<ForgotPasswordResponse> forgotPassword(@RequestBody @Valid ForgotPasswordRequest request) {
        User user = userService.findByEmail(request.email());
        String forgotPasswordToken = authService.forgotPassword(user, request.code());

        return ResponseEntity.status(OK).body(new ForgotPasswordResponse(
                getLocalizedMessage("verify_forgot_password_code_success"),
                forgotPasswordToken
        ));
    }

    @Operation(summary = "Reset password", description = "Reset password")
    @PostMapping("/reset-password")
    @ResponseStatus(OK)
    ResponseEntity<ResetPasswordResponse> resetPassword(@RequestBody @Valid ResetPasswordRequest request) {
        authService.resetPassword(request.token(), request.password(), request.passwordConfirmation());

        return ResponseEntity.status(OK).body(
                new ResetPasswordResponse(getLocalizedMessage("reset_password_success")));
    }

    @Operation(summary = "Introspect", description = "Introspect provided token")
    @PostMapping("/introspect")
    @ResponseStatus(OK)
    ResponseEntity<IntrospectResponse> introspect(@RequestBody @Valid IntrospectRequest request)
            throws ParseException, JOSEException {
        boolean isValid = authService.introspect(request.token());

        return ResponseEntity.status(OK).body(new IntrospectResponse(isValid));
    }

}