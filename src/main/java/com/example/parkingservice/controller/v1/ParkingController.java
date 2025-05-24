package com.example.parkingservice.controller.v1;

import com.example.parkingservice.entity.CarType;
import com.example.parkingservice.service.ParkingService;
import com.example.parkingservice.service.ParkingService.ParkingReport;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/parking")
@RequiredArgsConstructor
@Tag(name = "Парковка", description = "Управление въездами, выездами и отчетами парковки")
public class ParkingController {

    private final ParkingService parkingService;

    @Operation(
            summary = "Регистрация въезда автомобиля",
            description = "Сохраняет данные о въезде автомобиля по номеру и типу"
    )
    @PostMapping("/entry")
    public EntryResponse registerEntry(@RequestBody EntryRequest request) {
        var record = parkingService.registerEntry(request.getLicensePlate(), request.getCarType());
        if (record == null) {
            throw new IllegalArgumentException("Не удалось зарегистрировать въезд: автомобиль не принят.");
        }
        return new EntryResponse(record.getEntryTime());
    }

    @Operation(
            summary = "Регистрация выезда автомобиля",
            description = "Обновляет запись о машине, проставляя время выезда"
    )
    @PostMapping("/exit")
    public ExitResponse registerExit(@RequestBody ExitRequest request) {
        var record = parkingService.registerExit(request.getLicensePlate());
        return new ExitResponse(record.getExitTime());
    }

    @Operation(
            summary = "Получение отчета по парковке",
            description = "Возвращает количество занятых и свободных мест, а также среднюю продолжительность пребывания на парковке за заданный период"
    )
    @GetMapping("/report")
    public ParkingReport getReport(
            @RequestParam("start_date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam("end_date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @RequestParam(defaultValue = "100") int totalCapacity
    ) {
        return parkingService.getReport(start, end, totalCapacity);
    }

    @Data
    public static class EntryRequest {
        @NotBlank
        private String licensePlate;

        @NotNull
        private CarType carType;
    }

    @Data
    public static class EntryResponse {
        private final LocalDateTime entryTime;
    }

    @Data
    public static class ExitRequest {
        @NotBlank
        private String licensePlate;
    }

    @Data
    public static class ExitResponse {
        private final LocalDateTime exitTime;
    }
}

