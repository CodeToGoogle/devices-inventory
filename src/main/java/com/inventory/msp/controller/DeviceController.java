package com.inventory.msp.controller;

import com.inventory.msp.dto.DeviceDto;
import com.inventory.msp.model.Device;
import com.inventory.msp.services.DeviceService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/devices")
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceService deviceService;

    //  Get all devices
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','AGENCY','VIEWER')")
    public ResponseEntity<List<DeviceDto>> allDevices() {
        List<DeviceDto> result = deviceService.getAllDevices()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    //  Get device by ID
    @GetMapping("/{id:\\d+}")
    @PreAuthorize("hasAnyRole('ADMIN','AGENCY','VIEWER')")
    public ResponseEntity<DeviceDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(toDto(deviceService.getDevice(id)));
    }

    //  Get device by Serial
    @GetMapping("/serial/{serial}")
    @PreAuthorize("hasAnyRole('ADMIN','AGENCY','VIEWER')")
    public ResponseEntity<DeviceDto> getBySerial(@PathVariable String serial) {
        return ResponseEntity.ok(toDto(deviceService.getBySerial(serial)));
    }

    //  ADMIN adds device manually (rare)
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DeviceDto> create(@RequestBody Device device) {
        return ResponseEntity.ok(toDto(deviceService.saveDevice(device)));
    }

    //  Update placeholder → real serial
    @PutMapping("/{deviceId}/update-serial")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DeviceDto> updateSerial(
            @PathVariable Long deviceId,
            @RequestParam String newSerial) {

        Device updated = deviceService.updateSerialNumber(deviceId, newSerial);
        return ResponseEntity.ok(toDto(updated));
    }

    //  Convert ENTITY → DTO
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
