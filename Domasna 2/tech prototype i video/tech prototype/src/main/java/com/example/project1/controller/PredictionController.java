package com.example.project1.controller;

import com.example.project1.service.PredictionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PredictionController {

    private final PredictionService predictionService;

    @PostMapping("/predict")
    public ResponseEntity<Double> predictPrice(@RequestParam(name = "companyId") Long companyId) {
        double predictedPrice = predictionService.predictNextMonth(companyId);
        return ResponseEntity.ok(predictedPrice);
    }
}
