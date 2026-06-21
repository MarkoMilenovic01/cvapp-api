package com.best.cvapp.company.history;

import com.best.cvapp.company.history.dto.CVViewResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/company/history")
@RequiredArgsConstructor
@PreAuthorize("hasRole('COMPANY')")
public class CVHistoryController {

    private final CVHistoryService cvHistoryService;

    @GetMapping
    public ResponseEntity<List<CVViewResponse>> getViewHistory() {
        return ResponseEntity.ok(cvHistoryService.getViewHistory());
    }
}