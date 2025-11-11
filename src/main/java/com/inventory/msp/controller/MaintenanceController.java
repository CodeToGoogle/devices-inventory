package com.inventory.msp.controller;

import com.inventory.msp.dto.*;
import com.inventory.msp.model.Device;
import com.inventory.msp.model.MaintenanceRequest;
import com.inventory.msp.services.DeviceService;
import com.inventory.msp.services.MaintenanceRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/maintenance")
@RequiredArgsConstructor
public class MaintenanceController {

    private final MaintenanceRequestService maintenanceService;
    private final DeviceService deviceService;

    //  AGENCY CREATES REQUEST
    @PostMapping("/requests")
    public ResponseEntity<MaintenanceRequestDto> create(
            @RequestBody CreateMaintenanceRequestRequest req,
            Authentication auth) {

        //  Set createdBy from JWT token username
        req.setCreatedBy(auth.getName());

        //  Auto-fetch device using serial (agency never sends ID)
        if (req.getDeviceSerial() != null) {
            Device device = deviceService.getBySerial(req.getDeviceSerial());
            req.setOldSerial(device.getSerialNumber());  //  overwrite unsafe oldSerial from request
        }

        MaintenanceRequest created = maintenanceService.createRequest(req);
        return ResponseEntity.ok(toDto(created));
    }


    //  GET SINGLE REQUEST
    @GetMapping("/requests/{id}")
    public ResponseEntity<MaintenanceRequestDto> get(@PathVariable Long id) {
        return ResponseEntity.ok(toDto(maintenanceService.getById(id)));
    }

    //  LIST ALL
    @GetMapping("/requests")
    public ResponseEntity<List<MaintenanceRequestDto>> list() {
        List<MaintenanceRequestDto> list = maintenanceService.getAll()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(list);
    }

    //  ADMIN APPROVAL
    @PostMapping("/requests/{id}/approve")
    public ResponseEntity<MaintenanceRequestDto> approve(@PathVariable Long id,
                                                         @RequestBody ApproveRequestDto dto,
                                                         Authentication auth) {

        //  Always take approvedBy from JWT user — not from request payload
        String approvedBy = auth.getName();

        MaintenanceRequest out =
                maintenanceService.approveRequest(id, approvedBy, dto.isApproved(), dto.getRemarks());

        return ResponseEntity.ok(toDto(out));
    }

    //  Convert Entity → DTO
    private MaintenanceRequestDto toDto(MaintenanceRequest r) {
        MaintenanceRequestDto d = new MaintenanceRequestDto();
        d.setId(r.getId());
        d.setDeviceId(r.getDeviceId());
        d.setOldSerial(r.getOldSerial());
        d.setNewSerial(r.getNewSerial());
        d.setRequestType(r.getRequestType());
        d.setStatus(r.getStatus());
        d.setNewLocationId(r.getNewLocationId());
        d.setNewApproachRoadId(r.getNewApproachRoadId());
        d.setCreatedBy(r.getCreatedBy());
        d.setApprovedBy(r.getApprovedBy());
        d.setReferenceId(r.getReferenceId());
        d.setRemarks(r.getRemarks());

        return d;
    }
}
