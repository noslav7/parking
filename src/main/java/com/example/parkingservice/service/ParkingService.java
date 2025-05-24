package com.example.parkingservice.service;

import com.example.parkingservice.entity.CarType;
import com.example.parkingservice.entity.ParkingRecord;
import com.example.parkingservice.repository.ParkingRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ParkingService {

    private final ParkingRecordRepository repository;

    @Transactional
    public ParkingRecord registerEntry(String licensePlate, CarType carType) {
        ParkingRecord record = ParkingRecord.builder()
                .licensePlate(licensePlate)
                .carType(carType)
                .entryTime(LocalDateTime.now())
                .build();
        return repository.save(record);
    }

    @Transactional
    public ParkingRecord registerExit(String licensePlate) {
        ParkingRecord record = repository.findByLicensePlateAndExitTimeIsNull(licensePlate)
                .orElseThrow(() -> new IllegalArgumentException("Автомобиль не найден или уже выехал"));

        record.setExitTime(LocalDateTime.now());
        return repository.save(record);
    }

    @Transactional(readOnly = true)
    public ParkingReport getReport(LocalDateTime start, LocalDateTime end, int totalCapacity) {
        long occupied = repository.countByExitTimeIsNull();
        long freed = totalCapacity - occupied;

        Double avgSeconds = repository.findAverageParkingDurationInSeconds(start, end);
        double avgMinutes = avgSeconds != null ? avgSeconds / 60.0 : 0.0;

        return new ParkingReport(occupied, freed, avgMinutes);
    }

    public record ParkingReport(long occupied, long free, double avgDurationMinutes) {}
}

