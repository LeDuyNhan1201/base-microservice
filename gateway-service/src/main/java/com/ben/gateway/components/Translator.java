package com.ben.gateway.components;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Component;

import java.util.Locale;

@RequiredArgsConstructor
@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class Translator {

    static ResourceBundleMessageSource messageSource;

    @Autowired
    public Translator(ResourceBundleMessageSource messageSource) {
        Translator.messageSource = messageSource;
    }

    public static String getLocalizedMessage(String messageKey, Object... args) {
        Locale locale = LocaleContextHolder.getLocale();
        try {
            log.info("Getting message for key: {} and locale: {}", messageKey, locale);
            return messageSource.getMessage(messageKey, args, locale);

        } catch (NoSuchMessageException exception) {
            return exception.getMessage();
        }
    }

}
