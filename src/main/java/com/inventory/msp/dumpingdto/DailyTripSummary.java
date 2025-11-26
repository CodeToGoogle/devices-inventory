package com.inventory.msp.dumpingdto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DailyTripSummary {
    private long totalTrips;
    private long totalNetWeight;
}
