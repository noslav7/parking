package com.example.parkingservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "parking_record",
        indexes = {
                @Index(name = "idx_license_plate", columnList = "license_plate"),
                @Index(name = "idx_entry_time", columnList = "entry_time"),
                @Index(name = "idx_exit_time", columnList = "exit_time")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParkingRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "license_plate", nullable = false)
    private String licensePlate;

    @Enumerated(EnumType.STRING)
    @Column(name = "car_type", nullable = false)
    private CarType carType;

    @Column(name = "entry_time", nullable = false)
    private LocalDateTime entryTime;

    @Column(name = "exit_time")
    private LocalDateTime exitTime;

    public boolean isActive() {
        return this.exitTime == null;
    }
}

