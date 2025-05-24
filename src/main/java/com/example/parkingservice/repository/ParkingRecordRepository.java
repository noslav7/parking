package com.example.parkingservice.repository;

import com.example.parkingservice.entity.ParkingRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

public interface ParkingRecordRepository extends JpaRepository<ParkingRecord, Long> {

    Optional<ParkingRecord> findByLicensePlateAndExitTimeIsNull(String licensePlate);

    List<ParkingRecord> findAllByEntryTimeBetween(LocalDateTime start, LocalDateTime end);

    long countByExitTimeIsNull();

    long countByExitTimeIsNotNull();

    @Query(value = """
    SELECT AVG(EXTRACT(EPOCH FROM exit_time) - EXTRACT(EPOCH FROM entry_time))
    FROM parking_record
    WHERE exit_time IS NOT NULL AND entry_time BETWEEN :start AND :end
    """, nativeQuery = true)
    Double findAverageParkingDurationInSeconds(LocalDateTime start, LocalDateTime end);

}

