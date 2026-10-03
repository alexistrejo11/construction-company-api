package io.github.alexisTrejo11.construction.company.config.mail;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "spring.mail")
@Data
@NoArgsConstructor
public class MailProperties {
    private String host;
    private int port;
    private String username;
    private String password;
    private String protocol = "smtp";
    private boolean auth;
    private boolean starttlsEnable;
    private int connectionTimeout = 5000;
    private int timeout = 5000;
    private int writeTimeout = 5000;
}
