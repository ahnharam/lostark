package com.lostark.backend.controller;

import com.lostark.backend.dto.EquipmentDto;
import com.lostark.backend.service.EquipmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/equipment")
@RequiredArgsConstructor
public class EquipmentController {
    
    private final EquipmentService equipmentService;
    
    @GetMapping("/{characterName}")
    public ResponseEntity<List<EquipmentDto>> getEquipment(
            @PathVariable String characterName,
            @RequestParam(defaultValue = "false") boolean force) {
        try {
            log.info("Fetching equipment. characterName={} force={}", characterName, force);
            List<EquipmentDto> equipment = equipmentService.getCharacterEquipment(characterName, force);
            return ResponseEntity.ok(equipment);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
