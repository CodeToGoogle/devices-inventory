package com.inventory.msp.dto;

import com.inventory.msp.model.DeviceType;
import lombok.Data;

@Data
public class DeviceRequest {
    private String serialNumber;
    private DeviceType deviceType;
    private Boolean ecbPresent;
    private Boolean poles;
    private String latitude;
    private String longitude;
    private Long locationId;
    private Long approachRoadId;
    private String status;
}
