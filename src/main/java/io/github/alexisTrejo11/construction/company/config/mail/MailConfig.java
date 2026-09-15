package io.github.alexisTrejo11.construction.company.config.mail;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class MailConfig {

  private final MailProperties mailProperties;
  private final Environment environment;

  public MailConfig(MailProperties mailProperties, Environment environment) {
    this.mailProperties = mailProperties;
    this.environment = environment;
  }

  @Bean
  public JavaMailSender javaMailSender() {
    JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
    mailSender.setHost(mailProperties.getHost());
    mailSender.setPort(mailProperties.getPort());
    mailSender.setUsername(mailProperties.getUsername());
    mailSender.setPassword(mailProperties.getPassword());

    Properties props = mailSender.getJavaMailProperties();
    String protocol = mailProperties.getProtocol();
    props.put("mail.transport.protocol", protocol);
    String prefix = "spring.mail.properties.mail." + protocol + ".";
    props.put("mail." + protocol + ".auth", environment.getProperty(prefix + "auth", Boolean.class, mailProperties.isAuth()));
    props.put("mail." + protocol + ".starttls.enable", environment.getProperty(prefix + "starttls.enable", Boolean.class, mailProperties.isStarttlsEnable()));
     props.put("mail." + protocol + ".connectiontimeout", environment.getProperty(prefix + "connectiontimeout", Integer.class, mailProperties.getConnectionTimeout()));
    props.put("mail." + protocol + ".timeout", environment.getProperty(prefix + "timeout", Integer.class, mailProperties.getTimeout()));
     props.put("mail." + protocol + ".writetimeout", environment.getProperty(prefix + "writetimeout", Integer.class, mailProperties.getWriteTimeout()));
    props.put("mail.debug", false);

    return mailSender;
  }
}
