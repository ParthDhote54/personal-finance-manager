package com.personal.finance.manager.goal.controller;

import com.personal.finance.manager.goal.dto.GoalListResponse;
import com.personal.finance.manager.goal.dto.GoalRequest;
import com.personal.finance.manager.goal.dto.GoalResponse;
import com.personal.finance.manager.goal.service.GoalService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.HashMap;
import java.util.List;

/**
 * Controller handling user financial savings goal management.
 * Provides endpoints for creating, retrieving, updating, and deleting savings goals.
 */
@RestController
@RequestMapping("/api/goals")
@RequiredArgsConstructor
public class GoalController {

    private final GoalService goalService;

    /**
     * Creates a new savings goal for the authenticated user.
     *
     * @param request goal creation payload
     * @param session authenticated HTTP session
     * @return created goal details
     */
    @PostMapping
    public ResponseEntity<GoalResponse> createGoal(@Valid @RequestBody GoalRequest request, HttpSession session) {
        Long userId = (Long) session.getAttribute("USER_ID");
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        GoalResponse response = goalService.createGoal(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Retrieves all savings goals with calculated progress for the authenticated user.
     *
     * @param session authenticated HTTP session
     * @return wrapped list of savings goals
     */
    @GetMapping
    public ResponseEntity<GoalListResponse> getGoals(HttpSession session) {
        Long userId = (Long) session.getAttribute("USER_ID");
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        List<GoalResponse> response = goalService.getGoals(userId);
        return ResponseEntity.ok(new GoalListResponse(response));
    }

    /**
     * Retrieves a specific savings goal by ID for the authenticated user.
     *
     * @param id      goal ID
     * @param session authenticated HTTP session
     * @return goal details
     */
    @GetMapping("/{id}")
    public ResponseEntity<GoalResponse> getGoal(@PathVariable Long id, HttpSession session) {
        Long userId = (Long) session.getAttribute("USER_ID");
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        GoalResponse response = goalService.getGoal(userId, id);
        return ResponseEntity.ok(response);
    }

    /**
     * Updates target amount and target date for an existing savings goal.
     *
     * @param id      goal ID
     * @param request goal update payload
     * @param session authenticated HTTP session
     * @return updated goal details
     */
    @PutMapping("/{id}")
    public ResponseEntity<GoalResponse> updateGoal(@PathVariable Long id, @Valid @RequestBody GoalRequest request, HttpSession session) {
        Long userId = (Long) session.getAttribute("USER_ID");
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        GoalResponse response = goalService.updateGoal(userId, id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Deletes a savings goal owned by the authenticated user.
     *
     * @param id      goal ID
     * @param session authenticated HTTP session
     * @return success message response map
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteGoal(@PathVariable Long id, HttpSession session) {
        Long userId = (Long) session.getAttribute("USER_ID");
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        goalService.deleteGoal(userId, id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Goal deleted successfully");
        return ResponseEntity.ok(response);
    }
}
