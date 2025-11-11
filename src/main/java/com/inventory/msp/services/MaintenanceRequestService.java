package com.inventory.msp.services;

import com.inventory.msp.dto.CreateMaintenanceRequestRequest;
import com.inventory.msp.model.*;
import com.inventory.msp.repository.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MaintenanceRequestService {

    private final MaintenanceRequestRepository requestRepo;
    private final DeviceRepository deviceRepo;
    private final DeviceHistoryRepository historyRepo;
    private final LocationRepository locationRepo;
    private final ApproachRoadRepository roadRepo;

    private static final Logger log= LoggerFactory.getLogger(MaintenanceRequestService.class);

    //  STEP 1 — AGENCY CREATES REQUEST
    public MaintenanceRequest createRequest(CreateMaintenanceRequestRequest req) {

        //  1. Find device using SERIAL NUMBER (agency never knows deviceId)
        Device device = deviceRepo.findBySerialNumber(req.getDeviceSerial())
                .orElseThrow(() -> new RuntimeException("Device not found for serial: " + req.getDeviceSerial()));

        //  2. Resolve new location (if provided)
        Long newLocationId = null;
        if (req.getNewLocationName() != null && !req.getNewLocationName().isEmpty()) {
            Location loc = locationRepo.findByName(req.getNewLocationName())
                    .orElseThrow(() -> new RuntimeException("Location not found: " + req.getNewLocationName()));
            log.info("Current Location name from maintenance request: {}" ,req.getNewLocationName());
            newLocationId = loc.getId();
            log.info("Current Location Id from maintenance request: {}" ,newLocationId);
        }

        //  3. Resolve new approach road (if provided)
        Long newApproachRoadId = null;
        if (req.getNewApproachRoadName() != null && !req.getNewApproachRoadName().isEmpty()) {

            if (newLocationId == null) {
                throw new RuntimeException("Approach road requires newLocationName");
            }

            Location loc = locationRepo.findById(newLocationId)
                    .orElseThrow(() -> new RuntimeException("Location missing unexpectedly"));

            ApproachRoad road = roadRepo.findByRoadNameAndLocation(req.getNewApproachRoadName(), loc)
                    .orElseThrow(() -> new RuntimeException(
                            "Approach road not found: " + req.getNewApproachRoadName()
                    ));

            newApproachRoadId = road.getId();
        }

        // ✅ 4. Generate reference ID
        String refId = "REQ-" + UUID.randomUUID().toString().substring(0, 8);

        // ✅ 5. CREATE REQUEST
        MaintenanceRequest r = new MaintenanceRequest();
        r.setReferenceId(refId);

        r.setDeviceId(device.getId());
        r.setOldSerial(req.getOldSerial());
        r.setNewSerial(req.getNewSerial());

        r.setRequestType(req.getRequestType());
        r.setStatus(MaintenanceRequestStatus.PENDING);

        r.setCreatedBy(req.getCreatedBy());
        r.setRemarks(req.getRemarks());
        r.setCreatedAt(LocalDateTime.now());

        r.setNewLocationId(newLocationId);
        r.setNewApproachRoadId(newApproachRoadId);

        return requestRepo.save(r);
    }


    // ✅ STEP 2 — ADMIN APPROVES
    public MaintenanceRequest approveRequest(Long id, String admin, boolean approve, String remarks) {

        MaintenanceRequest req = requestRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        req.setApprovedBy(admin);
        req.setRemarks(remarks);

        if (!approve) {
            req.setStatus(MaintenanceRequestStatus.REJECTED);
            return requestRepo.save(req);
        }

        req.setCreatedAt(LocalDateTime.now());
        req.setStatus(MaintenanceRequestStatus.APPROVED);

        // ✅ APPLY ACTION TO DEVICE
        processRequest(req);

        return requestRepo.save(req);
    }


    // ✅ APPLY THE APPROVED MAINTENANCE REQUEST
    private void processRequest(MaintenanceRequest req) {

        Device device = deviceRepo.findById(req.getDeviceId())
                .orElseThrow(() -> new RuntimeException("Device not found"));

        String oldSerial = device.getSerialNumber();
        String oldLocationName = device.getLocation() != null ? device.getLocation().getName() : null;

        String newLocationName = null;

        // ✅ CASE A: Serial Update or Placeholder Replacement
        if (req.getNewSerial() != null &&
                req.getRequestType() == MaintenanceRequestType.SERIAL_UPDATE) {

            if (deviceRepo.existsBySerialNumber(req.getNewSerial()))
                throw new RuntimeException("Serial already exists");

            device.setSerialNumber(req.getNewSerial());
            device.setPlaceholder(false);
        }

        // ✅ CASE B: Move device to another location
        if (req.getRequestType() == MaintenanceRequestType.MOVE &&
                req.getNewLocationId() != null) {

            Location newLoc = locationRepo.findById(req.getNewLocationId())
                    .orElseThrow(() -> new RuntimeException("Location not found"));

            newLocationName = newLoc.getName();

            ApproachRoad newRoad = null;
            if (req.getNewApproachRoadId() != null) {
                newRoad = roadRepo.findById(req.getNewApproachRoadId()).orElse(null);
            }

            device.setLocation(newLoc);
            device.setApproachRoad(newRoad);
        }

        // ✅ CASE C: Replace faulty device with another device (new serial)
        if (req.getRequestType() == MaintenanceRequestType.REPLACE &&
                req.getNewSerial() != null) {

            if (deviceRepo.existsBySerialNumber(req.getNewSerial()))
                throw new RuntimeException("Replacement serial already exists");

            device.setSerialNumber(req.getNewSerial());
            device.setPlaceholder(false);
        }

        device.setUpdatedAt(LocalDateTime.now());
        deviceRepo.save(device);

        // ✅ LOG HISTORY
        DeviceHistory hist = DeviceHistory.builder()
                .deviceId(device.getId())
                .action("Maintenance Action: " + req.getRequestType())

                .oldSerial(oldSerial)
                .newSerial(device.getSerialNumber())

                .oldLocation(oldLocationName)
                .newLocation(newLocationName)

                .replacedDeviceSerial(req.getNewSerial()) // only relevant in replacement
                .referenceId(req.getReferenceId())

                .createdAt(LocalDateTime.now())
                .build();

        historyRepo.save(hist);
    }

    // ✅ GET BY ID
    public MaintenanceRequest getById(Long id) {
        return requestRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Request not found"));
    }

    // ✅ GET ALL
    public List<MaintenanceRequest> getAll() {
        return requestRepo.findAll();
    }
}
