package com.ben.identity.configurations;


import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@Slf4j
public class AppConfiguration {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

//    @Bean
//    public ProducerFactory<Object, Object> producerFactory() {
//        Map<String, Object> props = new HashMap<>();
//        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9093");
//        props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_SSL");
//        props.put(SaslConfigs.SASL_MECHANISM, "PLAIN");
//        props.put(SaslConfigs.SASL_JAAS_CONFIG, String.format(
//                "%s required username=\"%s\" " + "password=\"%s\";", PlainLoginModule.class.getName(), "ben1201", "Ben1201#"
//        ));
//        props.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, "identity-certs\\identity.keystore.jks");
//        props.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, "Ben1201#");
//        props.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, "identity-certs\\identity.truststore.jks");
//        props.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, "Ben1201#");
//        return new DefaultKafkaProducerFactory<>(props);
//    }

}
