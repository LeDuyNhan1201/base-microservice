package com.ben.mail.services;

import jakarta.mail.MessagingException;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;

@Service
public interface SendMailService {

    void sendMailToVerifyWithToken(String to, String token) throws MessagingException, UnsupportedEncodingException;

    void sendMailToVerifyWithCode(String to, String code) throws MessagingException, UnsupportedEncodingException;

    void sendMailToResetPassword(String to, String code) throws MessagingException, UnsupportedEncodingException;

}
