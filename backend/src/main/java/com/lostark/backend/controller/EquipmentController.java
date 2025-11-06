package com.lostark.backend.controller;

import com.lostark.backend.dto.EquipmentDto;
import com.lostark.backend.service.EquipmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/equipment")
@RequiredArgsConstructor
public class EquipmentController {
    
    private final EquipmentService equipmentService;
    
    @GetMapping("/{characterName}")
    public ResponseEntity<List<EquipmentDto>> getEquipment(@PathVariable String characterName) {
        try {
            List<EquipmentDto> equipment = equipmentService.getCharacterEquipment(characterName);
            return ResponseEntity.ok(equipment);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
