package com.ben.identity.configurations;

import lombok.extern.slf4j.Slf4j;

//@Configuration
@Slf4j
public class FlywayConfiguration {

//    @Value("${spring.flyway.locations}")
//    private String[] flywayLocations;
//
//    @Value("${spring.flyway.url}")
//    private String flywayUrl;
//
//    @Value("${spring.flyway.user}")
//    private String flywayUser;
//
//    @Value("${spring.flyway.password}")
//    private String flywayPassword;
//
//    @Bean
//    public Flyway flyway() {
//        Flyway flyway = Flyway.configure()
//            .dataSource(dataSource())
//            .locations(flywayLocations)
//            .baselineOnMigrate(true)
//            .baselineVersion("0")
//            .load();
//        flyway.migrate();
//        log.info("Flyway migration completed");
//        return flyway;
//    }
//
//    @Bean
//    public DataSource dataSource() {
//        DriverManagerDataSource dataSource = new DriverManagerDataSource();
//        dataSource.setUrl(flywayUrl);
//        dataSource.setUsername(flywayUser);
//        dataSource.setPassword(flywayPassword);
//        return dataSource;
//    }

}
