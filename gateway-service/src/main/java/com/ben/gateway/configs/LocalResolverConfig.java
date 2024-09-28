package com.ben.gateway.configs;

import jakarta.annotation.Nonnull;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.i18n.SimpleLocaleContext;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.i18n.AcceptHeaderLocaleContextResolver;
import org.springframework.web.server.i18n.LocaleContextResolver;

import java.util.List;
import java.util.Locale;

// This class is used to resolve the locale of the user (multi-language support)
//@Configuration
//@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
//@Slf4j
public class LocalResolverConfig {

    List<Locale> LOCALES = List.of(
            Locale.forLanguageTag("en"),
            Locale.forLanguageTag("vi")
    );

//    @Bean
//    public LocaleContextResolver localeContextResolver() {
//        return new AcceptHeaderLocaleContextResolver() {
//            @Nonnull
//            @Override
//            public SimpleLocaleContext resolveLocaleContext(@Nonnull ServerWebExchange exchange) {
//                String headerLang = exchange.getRequest().getHeaders().getFirst("Accept-Language");
//                log.info("Accept-Language: {}", headerLang);
//                Locale locale = StringUtils.hasLength(headerLang)
//                        ? Locale.lookup(Locale.LanguageRange.parse(headerLang), LOCALES)
//                        : Locale.getDefault();
//
//                log.info("Resolved Locale: {}", locale);
//                // Return SimpleLocaleContext which implements LocaleContext
//                return new SimpleLocaleContext(locale);
//            }
//        };
//    }
//
//    @Bean
//    public MessageSource messageSource(
//            @Value("${spring.messages.basename}") String basename,
//            @Value("${spring.messages.encoding}") String encoding,
//            @Value("${spring.messages.default-locale}") String defaultLocale,
//            @Value("${spring.messages.cache-duration}") int cacheSeconds
//    ) {
//        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
//        messageSource.setBasename(basename);
//        messageSource.setDefaultEncoding(encoding);
//        messageSource.setDefaultLocale(Locale.of(defaultLocale));
//        messageSource.setCacheSeconds(cacheSeconds);
//        return messageSource;
//    }

}
