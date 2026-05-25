package com.personal.finance.manager.auth.service;

import com.personal.finance.manager.auth.dto.LoginRequest;
import com.personal.finance.manager.auth.dto.UserRegistrationRequest;
import com.personal.finance.manager.user.entity.User;
import com.personal.finance.manager.exception.DuplicateResourceException;
import com.personal.finance.manager.exception.ResourceNotFoundException;
import com.personal.finance.manager.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.util.Base64;
import java.io.IOException;

/**
 * Service managing authentication and registration logic.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public User register(UserRegistrationRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Username already exists");
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .phoneNumber(request.getPhoneNumber())
                .isActive(true)
                .build();

        return userRepository.save(user);
    }

    /**
     * Authenticates a user and returns the verified database User entity.
     * Explictly saves the authenticated context to the HTTP Session for Spring Security 6 compatibility.
     */
    public User login(LoginRequest loginRequest, HttpServletRequest request, HttpServletResponse response) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        SecurityContextRepository repository = new HttpSessionSecurityContextRepository();
        repository.saveContext(SecurityContextHolder.getContext(), request, response);
        
        return userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found after successful authentication"));
    }

    @Transactional
    public User updateProfileImage(Long userId, MultipartFile file) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        try {
            // Validate file size (must be < 5MB to prevent database bloat)
            if (file.getSize() > 5 * 1024 * 1024) {
                throw new IllegalArgumentException("File size must be less than 5MB");
            }
            
            // Convert to Base64 string
            String base64Image = "data:" + file.getContentType() + ";base64," + 
                    Base64.getEncoder().encodeToString(file.getBytes());
            
            user.setProfileImage(base64Image);
            return userRepository.save(user);
        } catch (IOException e) {
            throw new RuntimeException("Failed to process image file", e);
        }
    }
}
