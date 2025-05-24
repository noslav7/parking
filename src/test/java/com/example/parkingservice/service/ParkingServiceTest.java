package com.example.parkingservice.service;

import com.example.parkingservice.entity.CarType;
import com.example.parkingservice.entity.ParkingRecord;
import com.example.parkingservice.repository.ParkingRecordRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ParkingServiceTest {

    @Mock
    private ParkingRecordRepository repository;

    @InjectMocks
    private ParkingService service;

    @Test
    void givenPlateAndType_whenRegisterEntry_thenSavesRecord() {
        String plate = "A123BC";
        CarType type = CarType.SEDAN;

        ArgumentCaptor<ParkingRecord> captor = ArgumentCaptor.forClass(ParkingRecord.class);
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ParkingRecord result = service.registerEntry(plate, type);

        verify(repository).save(captor.capture());
        ParkingRecord saved = captor.getValue();

        assertEquals(plate, saved.getLicensePlate());
        assertEquals(type, saved.getCarType());
        assertNotNull(saved.getEntryTime());
        assertNull(saved.getExitTime());

        assertEquals(saved, result);
    }

    @Test
    void givenActiveCar_whenRegisterExit_thenUpdatesExitTime() {
        String plate = "A123BC";
        ParkingRecord existing = ParkingRecord.builder()
                .licensePlate(plate)
                .carType(CarType.SUV)
                .entryTime(LocalDateTime.now().minusHours(2))
                .build();

        when(repository.findByLicensePlateAndExitTimeIsNull(plate)).thenReturn(Optional.of(existing));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ParkingRecord updated = service.registerExit(plate);

        assertNotNull(updated.getExitTime());
        verify(repository).save(existing);
    }

    @Test
    void givenNoMatchingCar_whenRegisterExit_thenThrowsException() {
        String plate = "X999ZZ";
        lenient().when(repository.findByLicensePlateAndExitTimeIsNull(plate)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.registerExit(plate));
        verify(repository, never()).save(any());
    }

    @Test
    void givenMockedStats_whenGetReport_thenReturnsReport() {
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now();

        when(repository.countByExitTimeIsNull()).thenReturn(20L);
        when(repository.findAverageParkingDurationInSeconds(start, end)).thenReturn(3600.0);

        var report = service.getReport(start, end, 100);

        assertEquals(20, report.occupied());
        assertEquals(80, report.free());
        assertEquals(60.0, report.avgDurationMinutes());
    }
}
