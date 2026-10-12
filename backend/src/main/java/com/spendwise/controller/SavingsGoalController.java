package com.spendwise.controller;

import com.spendwise.dto.SavingsGoalProgressRequest;
import com.spendwise.dto.SavingsGoalRequest;
import com.spendwise.dto.SavingsGoalResponse;
import com.spendwise.service.SavingsGoalService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/goals")
public class SavingsGoalController {
    private final SavingsGoalService savingsGoalService;

    public SavingsGoalController(SavingsGoalService savingsGoalService) {
        this.savingsGoalService = savingsGoalService;
    }

    @GetMapping
    public List<SavingsGoalResponse> getAll() {
        return savingsGoalService.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<SavingsGoalResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(savingsGoalService.getById(id));
    }

    @PostMapping
    public ResponseEntity<SavingsGoalResponse> create(@Valid @RequestBody SavingsGoalRequest request) {
        return new ResponseEntity<>(savingsGoalService.create(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SavingsGoalResponse> update(@PathVariable Long id, @Valid @RequestBody SavingsGoalRequest request) {
        return ResponseEntity.ok(savingsGoalService.update(id, request));
    }

    @PatchMapping("/{id}/progress")
    public ResponseEntity<SavingsGoalResponse> updateProgress(@PathVariable Long id, @Valid @RequestBody SavingsGoalProgressRequest request) {
        return ResponseEntity.ok(savingsGoalService.updateProgress(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        savingsGoalService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
