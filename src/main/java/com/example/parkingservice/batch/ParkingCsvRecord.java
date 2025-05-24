package com.example.parkingservice.batch;

import com.example.parkingservice.entity.CarType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ParkingCsvRecord {

    private String licensePlate;

    private CarType carType;

    private LocalDateTime entryTime;

    private LocalDateTime exitTime;
}

