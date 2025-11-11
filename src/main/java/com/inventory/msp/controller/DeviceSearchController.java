package com.inventory.msp.controller;

import com.inventory.msp.dto.DeviceDto;
import com.inventory.msp.model.Device;
import com.inventory.msp.model.DeviceType;
import com.inventory.msp.services.DeviceSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/devices")
@RequiredArgsConstructor
public class DeviceSearchController {

    private final DeviceSearchService service;

    @GetMapping("/by-serial/{serial}")
    public ResponseEntity<DeviceDto> getBySerial(@PathVariable String serial) {
        return ResponseEntity.ok(toDto(service.getBySerial(serial)));
    }

    @GetMapping("/by-location/{locationId}")
    public ResponseEntity<List<DeviceDto>> getByLocation(@PathVariable Long locationId) {
        List<DeviceDto> list = service.getByLocation(locationId)
                .stream().map(this::toDto).toList();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/by-approach/{roadId}")
    public ResponseEntity<List<DeviceDto>> getByApproach(@PathVariable Long roadId) {
        List<DeviceDto> list = service.getByApproachRoad(roadId)
                .stream().map(this::toDto).toList();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/by-type/{type}")
    public ResponseEntity<List<DeviceDto>> getByType(@PathVariable DeviceType type) {
        List<DeviceDto> list = service.getByType(type)
                .stream().map(this::toDto).toList();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/missing")
    public ResponseEntity<List<DeviceDto>> missingSerials() {
        List<DeviceDto> list = service.getMissingSerials()
                .stream().map(this::toDto).toList();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/installed")
    public ResponseEntity<List<DeviceDto>> installedDevices() {
        List<DeviceDto> list = service.getInstalledDevices()
                .stream().map(this::toDto).toList();
        return ResponseEntity.ok(list);
    }

//    @GetMapping("/filter")
//    public ResponseEntity<List<DeviceDto>> filter(
//            @RequestParam Long locationId,
//            @RequestParam(required = false) DeviceType type,
//            @RequestParam(required = false) Boolean missing
//    ) {
//        List<DeviceDto> list = service.getFilteredDevices(locationId, type, missing)
//                .stream().map(this::toDto).toList();
//        return ResponseEntity.ok(list);
//    }

    //  UPDATED SEARCH ENDPOINT (no proxy errors)
    @GetMapping("/search")                              //working fine, already checked
    public ResponseEntity<List<DeviceDto>> searchDevices(
            @RequestParam(required = false) String locationName,
            @RequestParam(required = false) DeviceType type,
            @RequestParam(required = false) String status) {

        List<Device> devices = service.advancedSearch(locationName, type, status);

        List<DeviceDto> result = devices.stream()
                .map(this::toDto)
                .toList();

        return ResponseEntity.ok(result);
    }

    //  Convert ENTITY → DTO (Fix for lazy loading)
    private DeviceDto toDto(Device d) {
        DeviceDto dto = new DeviceDto();
        dto.setId(d.getId());
        dto.setSerialNumber(d.getSerialNumber());
        dto.setDeviceType(d.getDeviceType());
        dto.setPoles(d.isPoles());
        dto.setEcbPresent(d.isEcbPresent());
        dto.setPlaceholder(d.isPlaceholder());
        dto.setLatitude(d.getLatitude());
        dto.setLongitude(d.getLongitude());
        dto.setStatus(d.getStatus());
        dto.setLocationName(d.getLocation() != null ? d.getLocation().getName() : null);
        dto.setApproachRoad(d.getApproachRoad() != null ? d.getApproachRoad().getRoadName() : null);
        return dto;
    }
}
