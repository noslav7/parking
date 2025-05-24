package com.example.parkingservice.batch;

import com.example.parkingservice.entity.ParkingRecord;
import org.springframework.batch.item.ItemProcessor;

public class ParkingCsvProcessor implements ItemProcessor<ParkingCsvRecord, ParkingRecord> {
    @Override
    public ParkingRecord process(ParkingCsvRecord item) {
        return ParkingRecord.builder()
                .licensePlate(item.getLicensePlate())
                .carType(item.getCarType())
                .entryTime(item.getEntryTime())
                .exitTime(item.getExitTime())
                .build();
    }
}

