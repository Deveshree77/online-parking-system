package com.smartpark.config;

import com.smartpark.model.User;
import com.smartpark.repository.UserRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Ensures the admin user has a correctly encoded password on startup.
 * The Flyway seed data uses a placeholder hash that may not match the intended password.
 */
@Component
public class AdminInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        userRepository.findByEmail("admin@smartpark.com").ifPresent(admin -> {
            // Re-encode the admin password to ensure it matches "admin123"
            if (!passwordEncoder.matches("admin123", admin.getPasswordHash())) {
                admin.setPasswordHash(passwordEncoder.encode("admin123"));
                userRepository.save(admin);
                System.out.println("[AdminInitializer] Admin password hash updated successfully.");
            }
        });
    }
}
