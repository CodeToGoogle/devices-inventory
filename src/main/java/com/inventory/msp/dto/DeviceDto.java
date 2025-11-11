package com.inventory.msp.dto;


import com.inventory.msp.model.DeviceType;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class DeviceDto {

    private Long id;
    private String serialNumber;

    private DeviceType deviceType;
    private boolean poles;
    private boolean ecbPresent;
    private boolean placeholder;

    private String latitude;
    private String longitude;

    private String status;
    private String locationName;
    private String approachRoad;

}
