package com.personal.finance.manager.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.finance.manager.auth.dto.LoginRequest;
import com.personal.finance.manager.auth.dto.UserRegistrationRequest;
import com.personal.finance.manager.auth.service.AuthService;
import com.personal.finance.manager.exception.DuplicateResourceException;
import com.personal.finance.manager.user.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false) // Disabling security filters for plain MVC testing, or we can test with security.
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @Test
    public void testSuccessfulRegistration() throws Exception {
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setUsername("test@example.com");
        request.setPassword("Password123");
        request.setFullName("John Doe");
        request.setPhoneNumber("+123456789");

        User mockUser = new User();
        mockUser.setId(1L);

        when(authService.register(any(UserRegistrationRequest.class))).thenReturn(mockUser);

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("User registered successfully"))
                .andExpect(jsonPath("$.userId").value(1));
    }

    @Test
    public void testDuplicateRegistration() throws Exception {
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setUsername("test@example.com");
        request.setPassword("Password123");
        request.setFullName("John Doe");
        request.setPhoneNumber("+123456789");

        when(authService.register(any(UserRegistrationRequest.class)))
                .thenThrow(new DuplicateResourceException("Username already exists"));

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    public void testSuccessfulLoginAndSessionPersistence() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("test@example.com");
        request.setPassword("Password123");

        User mockUser = new User();
        mockUser.setId(1L);

        when(authService.login(any(LoginRequest.class), any(HttpServletRequest.class), any(HttpServletResponse.class))).thenReturn(mockUser);

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andReturn();

        MockHttpSession session = (MockHttpSession) result.getRequest().getSession();
        assert session != null;
        assert session.getAttribute("USER_ID").equals(1L);

        // Simulate accessing a protected route by directly validating the session logic
        // For a full persistence test we'd hit another controller, but here we verify the session was populated
    }

    @Test
    public void testInvalidLogin() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("test@example.com");
        request.setPassword("WrongPassword");

        when(authService.login(any(LoginRequest.class), any(HttpServletRequest.class), any(HttpServletResponse.class)))
                .thenThrow(new RuntimeException("Bad credentials"));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError()); // Or 401 if ExceptionHandler handles BadCredentials
    }

    @Test
    public void testLogout() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("USER_ID", 1L);

        mockMvc.perform(post("/api/auth/logout")
                .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Logout successful"));
    }
}
