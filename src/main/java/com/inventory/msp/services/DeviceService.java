package com.inventory.msp.services;

import com.inventory.msp.model.Device;
import com.inventory.msp.model.DeviceHistory;
import com.inventory.msp.repository.DeviceHistoryRepository;
import com.inventory.msp.repository.DeviceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DeviceService {

    private final DeviceRepository deviceRepository;
    private final DeviceHistoryRepository historyRepository;

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
     ✅ Updates a placeholder device with a real serial
     ✅ Creates history log using builder
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
