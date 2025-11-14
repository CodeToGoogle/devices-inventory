package com.inventory.msp.services;

import com.inventory.msp.dto.DeviceRequest;
import com.inventory.msp.model.*;
import com.inventory.msp.repository.ApproachRoadRepository;
import com.inventory.msp.repository.DeviceHistoryRepository;
import com.inventory.msp.repository.DeviceRepository;
import com.inventory.msp.repository.LocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DeviceService {

    private final DeviceRepository deviceRepository;
    private final DeviceHistoryRepository historyRepository;
    private final LocationRepository locationRepository;
    private final ApproachRoadRepository approachRoadRepository;

    public String createDevice(DeviceRequest request) {

        // Normalize input once (avoid trim() everywhere)
        String serial = request.getSerialNumber().trim();
        String locationName = request.getLocationName().trim();
        String roadName = request.getApproachRoadName().trim();

        // 1. Check duplicate serial number
        if (deviceRepository.findBySerialNumber(serial).isPresent()) {
            throw new IllegalArgumentException(
                    "Device already exists with this serial number: " + serial
            );
        }

        // 2. Find or create Location
        Location location = locationRepository.findByNameIgnoreCase(locationName)
                .orElseGet(() -> {
                    Location newLoc = new Location();
                    newLoc.setName(locationName);
                    newLoc.setJunctionBox(JunctionBoxType.NONE);
                    return locationRepository.save(newLoc);
                });

        // 3. Find or create ApproachRoad (roadName + location must be unique)
        ApproachRoad approachRoad = approachRoadRepository
                .findByRoadNameAndLocationIgnoreCase(roadName, location)
                .orElseGet(() -> {
                    ApproachRoad newRoad = new ApproachRoad();
                    newRoad.setRoadName(roadName);
                    newRoad.setLocation(location);
                    return approachRoadRepository.save(newRoad);
                });

        // 4. Create device
        Device device = Device.builder()
                .serialNumber(serial)
                .deviceType(request.getDeviceType())
                .junctionBoxType(request.getJunctionBoxType())
                .poles(request.getPoles())
                .ecbPresent(request.getEcbPresent())
                .placeholder(request.isPlaceholder())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .status(request.getStatus())
                .location(location)
                .approachRoad(approachRoad)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        deviceRepository.save(device);

        return "✅ Device created successfully at location: " + location.getName();
    }



    public List<Device> getAllDevices() {
        return deviceRepository.findAll();
    }

    public Device getDevice(Long id) {
        return deviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Device not found"));
    }

    public Device getBySerial(String serial) {
        return deviceRepository.findBySerialNumber(serial)
                .orElseThrow(() -> new RuntimeException("Serial not found"));
    }

    public Device saveDevice(Device device) {
        return deviceRepository.save(device);
    }

    /**
      Updates a placeholder device with a real serial
      Creates history log using builder
     */
    public Device updateSerialNumber(Long deviceId, String newSerial) {

        Device device = getDevice(deviceId);

        if (!device.isPlaceholder()) {
            throw new RuntimeException("Device is not a placeholder, cannot update serial directly");
        }

        if (deviceRepository.existsBySerialNumber(newSerial)) {
            throw new RuntimeException("Serial already exists");
        }

        // Keep old serial (placeholder)
        String oldSerial = device.getSerialNumber();
        String oldLocationName = device.getLocation() != null ? device.getLocation().getName() : null;

        // ✅ Update serial
        device.setSerialNumber(newSerial);
        device.setPlaceholder(false);
        device.setUpdatedAt(LocalDateTime.now());

        Device saved = deviceRepository.save(device);

        // ✅ Create device history using BUILDER
        DeviceHistory history = DeviceHistory.builder()
                .deviceId(saved.getId())
                .action("Serial Update (placeholder replaced)")
                .oldSerial(oldSerial)
                .newSerial(newSerial)

                .oldLocation(oldLocationName)
                .newLocation(oldLocationName)  // location same — only serial changed

                .replacedDeviceSerial(null)    // no replaced device here
                .referenceId("MANUAL-UPDATE")
                .createdAt(LocalDateTime.now())
                .build();

        historyRepository.save(history);

        return saved;
    }
}
