package com.inventory.msp.model;

import jakarta.persistence.Embeddable;
import lombok.Data;

import java.io.Serializable;

@Data
@Embeddable
public class WeighBridgeKey implements Serializable {
    private Integer slipno;
    private String wbId;
}
