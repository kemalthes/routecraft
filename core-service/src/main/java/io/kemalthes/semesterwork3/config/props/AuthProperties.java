package io.kemalthes.semesterwork3.config.props;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "auth")
public class AuthProperties {

    private Jwt jwt = new Jwt();
    private Verification verification = new Verification();
    private Mail mail = new Mail();

    @Getter
    @Setter
    public static class Jwt {
        private String secret;
        private Duration accessTokenTtl = Duration.ofMinutes(15);
        private Duration refreshTokenTtl = Duration.ofDays(30);
    }

    @Getter
    @Setter
    public static class Verification {
        private Duration codeTtl = Duration.ofMinutes(10);
    }

    @Getter
    @Setter
    public static class Mail {
        private String from = "franker848@gmail.com";
    }
}
