package com.ben.identity.services.impls;

import com.ben.grpc.profile.CreateProfileRequest;
import com.ben.identity.entities.User;
import com.ben.identity.entities.Verification;
import com.ben.identity.enums.VerificationType;
import com.ben.identity.exceptions.AppException;
import com.ben.identity.repositories.VerificationRepository;
import com.ben.identity.services.AuthenticationService;
import com.ben.identity.services.BaseRedisService;
import com.ben.identity.services.UserService;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Service;

import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.text.ParseException;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static com.ben.identity.Constants.*;
import static com.ben.identity.enums.VerificationType.*;
import static com.ben.identity.exceptions.AppErrorCode.*;
import static java.time.temporal.ChronoUnit.MINUTES;
import static java.time.temporal.ChronoUnit.SECONDS;
import static org.springframework.http.HttpStatus.*;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationServiceImpl implements AuthenticationService {

    UserService userService;

    VerificationRepository verificationRepository;

    PasswordEncoder passwordEncoder;

    KafkaTemplate<String, CreateProfileRequest> createProfileRequestKafkaTemplate;

    KafkaTemplate<String, String> sendMailKafkaTemplate;

    BaseRedisService<String, String, Object> baseRedisService;

    @NonFinal
    @Value("${jwt.accessSignerKey}")
    protected String ACCESS_SIGNER_KEY;

    @NonFinal
    @Value("${jwt.refreshSignerKey}")
    protected String REFRESH_SIGNER_KEY;

    @NonFinal
    @Value("${jwt.valid-duration}")
    protected long VALID_DURATION;

    @NonFinal
    @Value("${jwt.refreshable-duration}")
    protected long REFRESHABLE_DURATION;

    @Override
    public boolean introspect(String token) throws JOSEException, ParseException {
        boolean isValid = true;

        try {
            verifyToken(token, false);

        } catch (AuthenticationException e) {
            isValid = false;
        }

        return isValid;
    }

    @Override
    public void signUp(User user, String confirmationPassword, String firstName, String lastName) {
        if (userService.existsByEmail(user.getEmail()))
            throw new AppException(EMAIL_ALREADY_IN_USE, CONFLICT, "Email already in use");

        if (!user.getPassword().equals(confirmationPassword))
            throw new AppException(PASSWORD_MIS_MATCH, BAD_REQUEST, "Password mismatch");

        if (isInvalidEmail(user.getEmail()))
            throw new AppException(INVALID_EMAIL, BAD_REQUEST, "Invalid email");

        if (isWeakPassword(user.getPassword()))
            throw new AppException(WEAK_PASSWORD, BAD_REQUEST, "Weak password");

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        User newUser = userService.createUser(user);

        createProfileRequestKafkaTemplate.send(KAFKA_TOPIC_CREATE_PROFILE, newUser.getId(), CreateProfileRequest.newBuilder()
                .setUserId(newUser.getId())
                .setFirstName(firstName)
                .setLastName(lastName)
                .build());
    }

    @Override
    @Transactional
    public void sendEmailVerification(String email, VerificationType verificationType) {
        User user = userService.findByEmail(email);

        List<Verification> verifications = verificationRepository.findByUserAndVerificationType(user, verificationType);

        if (verificationType.equals(VERIFY_EMAIL_BY_CODE) || verificationType.equals(VERIFY_EMAIL_BY_TOKEN)) {
            if (user.isActivated())
                throw new AppException(USER_ALREADY_VERIFIED, BAD_REQUEST, "User already verified");

            else {
                if (!verifications.isEmpty()) verificationRepository.deleteAll(verifications);
                sendEmail(email, verificationType);
            }

        } else {
            if (verifications.isEmpty()) throw new AppException(CANNOT_SEND_EMAIL, BAD_REQUEST, "Cannot send email");

            else {
                verificationRepository.deleteAll(verifications);
                sendEmail(email, verificationType);
            }
        }
    }

    @Override
    @Transactional
    public void verifyEmail(User user, String code, String token) {
        Verification verification = (code != null)
                ? verificationRepository.findByCode(code).orElseThrow(() ->
                new AppException(CODE_INVALID, BAD_REQUEST, "Invalid code"))

                : verificationRepository.findById(token).orElseThrow(() ->
                new AppException(CODE_INVALID, BAD_REQUEST, "Invalid token"));

        if (verification.getExpiryTime().before(new Date()))
            throw new AppException(CODE_INVALID, UNPROCESSABLE_ENTITY, "Code expired");

        userService.activateUser((user != null) ? user : verification.getUser());

        verificationRepository.delete(verification);
    }

    @Override
    public User signIn(String email, String password) {
        User user = userService.findByEmail(email);

        if (isPasswordExpired(user))
            throw new AppException(EXPIRED_PASSWORD, CONFLICT, "Expired password");

        if (isTwoFactorRequired(user))
            throw new AppException(TWO_FACTOR_REQUIRED, FORBIDDEN, "Two factor required");

        if (isUserDisabled(user))
            throw new AppException(USER_DISABLED, FORBIDDEN, "User disabled");

        if (!passwordEncoder.matches(password, user.getPassword()))
            throw new AppException(WRONG_PASSWORD, UNAUTHORIZED, "Wrong password");

        if (!user.isActivated()) throw new AppException(USER_NOT_ACTIVATED, FORBIDDEN, "User not activated");

        return user;
    }

    @Override
    public String generateToken(User user, boolean isRefresh) {
        JWSHeader accessHeader = new JWSHeader(ACCESS_TOKEN_SIGNATURE_ALGORITHM);
        JWSHeader refreshHeader = new JWSHeader(REFRESH_TOKEN_SIGNATURE_ALGORITHM);

        Date expiryTime = (isRefresh)
                ? new Date(Instant.now().plus(REFRESHABLE_DURATION, SECONDS).toEpochMilli())
                : new Date(Instant.now().plus(VALID_DURATION, SECONDS).toEpochMilli());

        String jwtID = UUID.randomUUID().toString();

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getEmail())
                .issuer("com.infinitynet")
                .issueTime(new Date())
                .expirationTime(expiryTime)
                .jwtID(jwtID)
                .build();

        if (!isRefresh) {
            jwtClaimsSet = new JWTClaimsSet.Builder(jwtClaimsSet)
                    .claim("more-info", "???????")
                    .build();
        }

        Payload payload = new Payload(jwtClaimsSet.toJSONObject());

        JWSObject jwsObject = (isRefresh)
                ? new JWSObject(refreshHeader, payload)
                : new JWSObject(accessHeader, payload);

        try {
            if (isRefresh)
                jwsObject.sign(new MACSigner(REFRESH_SIGNER_KEY.getBytes()));
            else
                jwsObject.sign(new MACSigner(ACCESS_SIGNER_KEY.getBytes()));

            return jwsObject.serialize();

        } catch (JOSEException e) {
            log.error("Cannot create token", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public String refresh(String refreshToken, HttpServletRequest servletRequest) throws ParseException, JOSEException {
        SignedJWT signedJWT = verifyToken(refreshToken, true);
        String email = signedJWT.getJWTClaimsSet().getSubject();

        User user;
        try {
            user = userService.findByEmail(email);

        } catch (AuthenticationException e) {
            throw new AppException(INVALID_TOKEN, BAD_REQUEST, "Invalid token");
        }

        if (servletRequest.getHeader("Authorization") == null)
            throw new AppException(INVALID_TOKEN, BAD_REQUEST, "Invalid token");

        String accessToken = servletRequest.getHeader("Authorization").substring(7);
        SignedJWT signedAccessTokenJWT = SignedJWT.parse(accessToken);
        String jwtID = signedAccessTokenJWT.getJWTClaimsSet().getJWTID();
        Date expiryTime = signedAccessTokenJWT.getJWTClaimsSet().getExpirationTime();

        if (!signedAccessTokenJWT.getJWTClaimsSet().getSubject().equals(email))
            throw new AppException(INVALID_TOKEN, BAD_REQUEST, "Invalid token");

        if (expiryTime.after(new Date())) {
            baseRedisService.set(jwtID, "revoked");
            baseRedisService.setTimeToLive(jwtID, expiryTime.getTime() - System.currentTimeMillis());
        }

        return generateToken(user, false);
    }

    @Override
    @Transactional
    public void sendEmailForgotPassword(String email) {
        sendEmail(email, RESET_PASSWORD);
    }

    @Override
    @Transactional
    public String forgotPassword(User user, String code) {
        Verification verification = verificationRepository.findByCode(code).orElseThrow(() ->
                new AppException(CODE_INVALID, BAD_REQUEST, "Invalid code"));

        if (verification.getExpiryTime().before(new Date()))
            throw new AppException(CODE_INVALID, UNPROCESSABLE_ENTITY, "Code expired");

        if (!verification.getUser().getEmail().equals(user.getEmail()))
            throw new AppException(CODE_INVALID, BAD_REQUEST, "Invalid code");

        return verification.getToken();
    }

    @Override
    @Transactional
    public void resetPassword(String token, String password, String confirmationPassword) {
        Verification verification = verificationRepository.findById(token).orElseThrow(() ->
                new AppException(TOKEN_REVOKED, UNPROCESSABLE_ENTITY, "Token revoked"));

        if (verification.getExpiryTime().before(new Date()))
            throw new AppException(TOKEN_EXPIRED, UNPROCESSABLE_ENTITY, "Token expired");

        if (!password.equals(confirmationPassword))
            throw new AppException(PASSWORD_MIS_MATCH, BAD_REQUEST, "Password mismatch");

        if (isWeakPassword(password))
            throw new AppException(WEAK_PASSWORD, BAD_REQUEST, "Weak password");

        User user = verification.getUser();
        userService.updatePassword(user, passwordEncoder.encode(password));
        verificationRepository.delete(verification);
    }

    @Override
    public void signOut(String accessToken, String refreshToken) throws ParseException, JOSEException {
        try {
            SignedJWT signAccessToken = verifyToken(accessToken, false);
            Date AccessTokenExpiryTime = signAccessToken.getJWTClaimsSet().getExpirationTime();

            if (AccessTokenExpiryTime.after(new Date())) {
                baseRedisService.set(signAccessToken.getJWTClaimsSet().getJWTID(), "revoked");
                baseRedisService.setTimeToLive(signAccessToken.getJWTClaimsSet().getJWTID(),
                        AccessTokenExpiryTime.getTime() - System.currentTimeMillis());
            }

            SignedJWT signRefreshToken = verifyToken(refreshToken, true);
            Date RefreshTokenExpiryTime = signRefreshToken.getJWTClaimsSet().getExpirationTime();

            if (RefreshTokenExpiryTime.after(new Date())) {
                baseRedisService.set(signRefreshToken.getJWTClaimsSet().getJWTID(), "revoked");
                baseRedisService.setTimeToLive(signRefreshToken.getJWTClaimsSet().getJWTID(),
                        RefreshTokenExpiryTime.getTime() - System.currentTimeMillis());
            }

        } catch (AuthenticationException exception) {
            log.error("Cannot sign out", exception);
            //TODO: Disable the user account
        }
    }

    @Transactional
    protected void sendEmail(String email, VerificationType verificationType) {
        User user = userService.findByEmail(email);

        Verification verification = verificationRepository.save(Verification.builder()
                .code(generateVerificationCode(6))
                .expiryTime(Date.from(Instant.now().plus(3, MINUTES)))
                .verificationType(verificationType)
                .user(user)
                .build());

        sendMailKafkaTemplate.send(KAFKA_TOPIC_SEND_MAIL,
                verificationType + ":" + email + ":" + verification.getToken() + ":" + verification.getCode());
    }

    private SignedJWT verifyToken(String token, boolean isRefresh) throws JOSEException, ParseException {
        JWSVerifier verifier = (isRefresh)
                ? new MACVerifier(REFRESH_SIGNER_KEY.getBytes())
                : new MACVerifier(ACCESS_SIGNER_KEY.getBytes());

        SignedJWT signedJWT = SignedJWT.parse(token);

        Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();

        boolean verified = signedJWT.verify(verifier);

        if (isRefresh) {
            if (expiryTime.before(new Date()))
                throw new AppException(TOKEN_EXPIRED, UNAUTHORIZED, "Token expired");

            if (!verified)
                throw new AppException(INVALID_SIGNATURE, UNAUTHORIZED, "Invalid signature");

            SecretKeySpec secretKeySpec = new SecretKeySpec(
                    REFRESH_SIGNER_KEY.getBytes(),
                    REFRESH_TOKEN_SIGNATURE_ALGORITHM.getName()
            );
            try {
                NimbusJwtDecoder nimbusJwtDecoder = NimbusJwtDecoder.withSecretKey(secretKeySpec)
                        .macAlgorithm(MacAlgorithm.from(REFRESH_TOKEN_SIGNATURE_ALGORITHM.getName()))
                        .build();
                nimbusJwtDecoder.decode(token);

            } catch (JwtException e) {
                throw new AppException(INVALID_SIGNATURE, UNAUTHORIZED, "Invalid signature");
            }

        } else {
            if (!verified || expiryTime.before(new Date()))
                throw new AppException(TOKEN_INVALID, UNAUTHORIZED, "Token invalid");
        }

        String value = (String) baseRedisService.get(signedJWT.getJWTClaimsSet().getJWTID());

        if (value != null) {
            if (value.equals("revoked"))
                throw new AppException(TOKEN_REVOKED, UNAUTHORIZED, "Token revoked");

            else
                throw new AppException(TOKEN_BLACKLISTED, UNAUTHORIZED, "Token blacklisted");
        }

        return signedJWT;
    }

    public static String generateVerificationCode(int length) {
        StringBuilder code = new StringBuilder();
        SecureRandom random = new SecureRandom();

        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(CHARACTERS.length());
            code.append(CHARACTERS.charAt(randomIndex));
        }

        return code.toString();
    }

    private boolean isPasswordExpired(User user) {
        return false;
    }

    private boolean isTwoFactorRequired(User user) {
        return false;
    }

    private boolean isUserDisabled(User user) {
        return false;
    }

    private boolean isInvalidEmail(String email) {
        return false;
    }

    private boolean isWeakPassword(String password) {
        return false;
    }

}