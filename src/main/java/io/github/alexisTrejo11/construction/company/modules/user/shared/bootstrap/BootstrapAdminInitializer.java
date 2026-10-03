package io.github.alexisTrejo11.construction.company.modules.user.shared.bootstrap;

import io.github.alexisTrejo11.construction.company.modules.user.shared.domain.User;
import io.github.alexisTrejo11.construction.company.modules.user.shared.domain.UserRole;
import io.github.alexisTrejo11.construction.company.modules.user.shared.domain.UserStatus;
import io.github.alexisTrejo11.construction.company.modules.user.shared.persistence.UserRepository;
import java.util.EnumSet;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BootstrapAdminInitializer implements ApplicationRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${BOOTSTRAP_ADMIN_EMAIL:}")
    private String email;

    @Value("${BOOTSTRAP_ADMIN_PASSWORD:}")
    private String password;

    @Override
    public void run(ApplicationArguments arguments) {
        if (email.isBlank() || password.isBlank() || userRepository.count() > 0) {
            return;
        }

        User admin = new User();
        admin.setEmail(email);
        admin.setFirstName("System");
        admin.setLastName("Administrator");
        admin.setPasswordHash(passwordEncoder.encode(password));
        admin.setStatus(UserStatus.ACTIVE);
        admin.setRoles(EnumSet.of(UserRole.COMPANY_ADMIN));

        userRepository.save(admin);
    }
}
