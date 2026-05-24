package com.personal.finance.manager.auth.controller;

import java.util.Map;
import java.util.HashMap;
import com.personal.finance.manager.auth.dto.LoginRequest;
import com.personal.finance.manager.auth.dto.UserRegistrationRequest;
import com.personal.finance.manager.user.entity.User;
import com.personal.finance.manager.auth.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller handling user registration and login.
 * Manages the HTTP session to isolate user data using USER_ID.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody UserRegistrationRequest request) {
        User user = authService.register(request);
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("message", "User registered successfully");
        responseMap.put("userId", user.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(responseMap);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginRequest loginRequest, 
                                                     HttpSession session, 
                                                     HttpServletRequest request, 
                                                     HttpServletResponse response) {
        User user = authService.login(loginRequest, request, response);
        session.setAttribute("USER_ID", user.getId());
        
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("message", "Login successful");
        return ResponseEntity.ok(responseMap);
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(HttpSession session) {
        session.invalidate();
        SecurityContextHolder.clearContext();
        
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("message", "Logout successful");
        return ResponseEntity.ok(responseMap);
    }
}
