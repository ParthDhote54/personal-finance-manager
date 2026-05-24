package com.personal.finance.manager.goal.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.finance.manager.goal.dto.GoalRequest;
import com.personal.finance.manager.goal.dto.GoalResponse;
import com.personal.finance.manager.goal.service.GoalService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GoalController.class)
@AutoConfigureMockMvc(addFilters = false)
public class GoalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private GoalService goalService;

    @Test
    public void testCreateGoalSuccess() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("USER_ID", 1L);

        GoalRequest request = new GoalRequest();
        request.setGoalName("Emergency Fund");
        request.setTargetAmount(new BigDecimal("5000.00"));
        request.setStartDate(LocalDate.of(2025, 1, 1));
        request.setTargetDate(LocalDate.of(2026, 1, 1));

        GoalResponse response = GoalResponse.builder()
                .id(1L)
                .goalName("Emergency Fund")
                .targetAmount(new BigDecimal("5000.00"))
                .startDate(LocalDate.of(2025, 1, 1))
                .targetDate(LocalDate.of(2026, 1, 1))
                .currentProgress(new BigDecimal("1000.00"))
                .progressPercentage(20.0)
                .remainingAmount(new BigDecimal("4000.00"))
                .build();

        when(goalService.createGoal(eq(1L), any(GoalRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/goals")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.goalName").value("Emergency Fund"))
                .andExpect(jsonPath("$.currentProgress").value(1000.00))
                .andExpect(jsonPath("$.progressPercentage").value(20.0))
                .andExpect(jsonPath("$.remainingAmount").value(4000.00));
    }

    @Test
    public void testInvalidTargetDate() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("USER_ID", 1L);

        GoalRequest request = new GoalRequest();
        request.setGoalName("Bad Dates");
        request.setTargetAmount(new BigDecimal("5000.00"));
        request.setStartDate(LocalDate.of(2025, 1, 1));
        request.setTargetDate(LocalDate.of(2024, 1, 1)); // Invalid, before start

        mockMvc.perform(post("/api/goals")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testGetGoalsDynamicProgress() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("USER_ID", 1L);

        when(goalService.getGoals(1L)).thenReturn(List.of());

        mockMvc.perform(get("/api/goals").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    public void testUpdateGoal() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("USER_ID", 1L);

        GoalRequest request = new GoalRequest();
        request.setGoalName("Emergency Fund updated");
        request.setTargetAmount(new BigDecimal("6000.00"));
        request.setStartDate(LocalDate.of(2025, 1, 1));
        request.setTargetDate(LocalDate.of(2026, 6, 1));

        GoalResponse response = GoalResponse.builder()
                .id(1L)
                .targetAmount(new BigDecimal("6000.00"))
                .build();

        when(goalService.updateGoal(eq(1L), eq(1L), any(GoalRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/goals/1")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    public void testDeleteGoal() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("USER_ID", 1L);

        mockMvc.perform(delete("/api/goals/1").session(session))
                .andExpect(status().isNoContent());
    }
}
