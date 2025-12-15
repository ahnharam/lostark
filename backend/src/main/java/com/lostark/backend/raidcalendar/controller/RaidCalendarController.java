package com.lostark.backend.raidcalendar.controller;

import com.lostark.backend.auth.AppUserPrincipal;
import com.lostark.backend.raidcalendar.dto.RaidCalendarEntryRequest;
import com.lostark.backend.raidcalendar.dto.RaidCalendarEntryResponse;
import com.lostark.backend.raidcalendar.service.RaidCalendarService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/me/raid-calendar")
@RequiredArgsConstructor
public class RaidCalendarController {

    private final RaidCalendarService raidCalendarService;

    @GetMapping
    public ResponseEntity<List<RaidCalendarEntryResponse>> getMyEntries(
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        return ResponseEntity.ok(raidCalendarService.getMyEntries(principal.getAppUserId()));
    }

    @PutMapping
    public ResponseEntity<List<RaidCalendarEntryResponse>> replaceMyEntries(
            @AuthenticationPrincipal AppUserPrincipal principal,
            @RequestBody List<RaidCalendarEntryRequest> requests
    ) {
        return ResponseEntity.ok(raidCalendarService.replaceMyEntries(principal.getAppUserId(), requests));
    }
}

