package com.expensetracker.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import lombok.NonNull;

import java.util.List;
import java.util.Locale;

/**
 * MessageSourceConfig — configures i18n message resolution.
 *
 * Rules from CLAUDE.md §13:
 *  - Use ReloadableResourceBundleMessageSource with classpath:i18n/messages basename.
 *  - Default encoding: UTF-8.
 *  - fallbackToSystemLocale must be false.
 *  - Locale resolved from Accept-Language HTTP header.
 *  - Default locale: en (English).
 *  - Supported locales: en, hi, te.
 */
@Configuration
public class MessageSourceConfig {

    /**
     * Configures the MessageSource to load message bundles from classpath:i18n/messages*.
     * Using ReloadableResourceBundleMessageSource allows bundles to be reloaded
     * at runtime without a server restart (useful in non-prod environments).
     *
     * @return configured ReloadableResourceBundleMessageSource
     */
    @Bean
    public ReloadableResourceBundleMessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource =
                new ReloadableResourceBundleMessageSource();

        // Bundle basename — Spring appends _hi, _te, etc. for locale variants
        messageSource.setBasename("classpath:i18n/messages");
 
        // Must be false: do not fall back to JVM system locale on missing keys because it can cause unpredictable behavior in different environments
        messageSource.setFallbackToSystemLocale(false);

        // Default encoding for all property files
        messageSource.setDefaultEncoding("UTF-8");

        // Default locale when Accept-Language header is absent or unrecognised
        messageSource.setDefaultLocale(Locale.ENGLISH);

        // Cache indefinitely in production; override to 0 in dev to pick up changes
        messageSource.setCacheSeconds(-1);

        return messageSource;
    }

    /**
     * Resolves the request locale from the Accept-Language HTTP header.
     * Supported locales are restricted to en, hi, and te.
     * Any unsupported locale falls back to English.
     *
     * @return AcceptHeaderLocaleResolver restricted to supported locales
     */
    @Bean
    public LocaleResolver localeResolver() {
        AcceptHeaderLocaleResolver resolver = new AcceptHeaderLocaleResolver();
        resolver.setDefaultLocale(Locale.ENGLISH);
        resolver.setSupportedLocales((@NonNull List<Locale>) List.of(
            Locale.ENGLISH,
            new Locale.Builder().setLanguage("hi").build(),
            new Locale.Builder().setLanguage("te").build()
        ));

        return resolver;
    }
}
