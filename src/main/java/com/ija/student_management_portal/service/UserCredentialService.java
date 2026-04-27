package com.ija.student_management_portal.service;

import com.ija.student_management_portal.entity.UserCredential;
import com.ija.student_management_portal.repository.UserCredentialRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Slf4j
public class UserCredentialService {

    @Autowired
    private UserCredentialRepository userCredentialRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public Optional<UserCredential> createStudentCredentials(String username, String password, String studentId) {
        // Check if username already exists
        if (userCredentialRepository.findByUsername(username).isPresent()) {
            log.warn("Username already exists: {}", username);
            return Optional.empty();
        }

        // Check if student ID is already associated with another account
        if (userCredentialRepository.findByStudentId(studentId).isPresent()) {
            log.warn("Student ID already has an account: {}", studentId);
            return Optional.empty();
        }

        UserCredential userCredential = UserCredential.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .studentId(studentId)
                .role(UserCredential.UserRole.STUDENT)
                .enabled(true)
                .build();

        UserCredential savedCredential = userCredentialRepository.save(userCredential);
        log.info("Created credentials for student: {} with username: {}", studentId, username);
        return Optional.of(savedCredential);
    }

    @Transactional
    public Optional<UserCredential> createAdminCredentials(String username, String password) {
        // Check if username already exists
        if (userCredentialRepository.findByUsername(username).isPresent()) {
            log.warn("Username already exists: {}", username);
            return Optional.empty();
        }

        UserCredential userCredential = UserCredential.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .role(UserCredential.UserRole.ADMIN)
                .enabled(true)
                .build();

        UserCredential savedCredential = userCredentialRepository.save(userCredential);
        log.info("Created admin credentials with username: {}", username);
        return Optional.of(savedCredential);
    }

    public Optional<UserCredential> getUserByUsername(String username) {
        return userCredentialRepository.findByUsername(username);
    }

    public Optional<UserCredential> getUserByStudentId(String studentId) {
        return userCredentialRepository.findByStudentId(studentId);
    }

    @Transactional
    public boolean updatePassword(String username, String newPassword) {
        Optional<UserCredential> userOptional = userCredentialRepository.findByUsername(username);
        if (userOptional.isPresent()) {
            UserCredential user = userOptional.get();
            user.setPassword(passwordEncoder.encode(newPassword));
            userCredentialRepository.save(user);
            log.info("Password updated for user: {}", username);
            return true;
        }
        return false;
    }

    @Transactional
    public boolean disableUser(String username) {
        Optional<UserCredential> userOptional = userCredentialRepository.findByUsername(username);
        if (userOptional.isPresent()) {
            UserCredential user = userOptional.get();
            user.setEnabled(false);
            userCredentialRepository.save(user);
            log.info("User disabled: {}", username);
            return true;
        }
        return false;
    }

    @Transactional
    public boolean enableUser(String username) {
        Optional<UserCredential> userOptional = userCredentialRepository.findByUsername(username);
        if (userOptional.isPresent()) {
            UserCredential user = userOptional.get();
            user.setEnabled(true);
            userCredentialRepository.save(user);
            log.info("User enabled: {}", username);
            return true;
        }
        return false;
    }

    public long countAll() {
        return userCredentialRepository.count();
    }

    public long countByRole(String role) {
        try {
            UserCredential.UserRole userRole = UserCredential.UserRole.valueOf(role);
            return userCredentialRepository.countByRole(userRole);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid role: {}", role);
            return 0;
        }
    }
}
