package com.sid.rickmorty.controller;

import com.sid.rickmorty.model.dto.LocationDto;
import com.sid.rickmorty.service.LocationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/locations")
public class LocationController {

    private final LocationService locationService;

    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    @GetMapping
    public List<LocationDto> list() {
        return locationService.listLocations();
    }

    @PostMapping("/refresh")
    public ResponseEntity<Void> refresh() {
        locationService.refreshFromSource();
        return ResponseEntity.accepted().build();
    }
}
