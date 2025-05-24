package com.example.parkingservice.controller;

import com.example.parkingservice.controller.v1.ParkingController;
import com.example.parkingservice.entity.CarType;
import com.example.parkingservice.service.ParkingService;
import com.example.parkingservice.service.ParkingService.ParkingReport;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ParkingController.class)
class ParkingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ParkingService parkingService;

    @Test
    void givenValidEntryRequest_whenRegisterEntry_thenReturnsEntryTime() throws Exception {
        var now = LocalDateTime.now();
        var record = com.example.parkingservice.entity.ParkingRecord.builder()
                .licensePlate("A123BC")
                .carType(CarType.SEDAN)
                .entryTime(now)
                .build();

        Mockito.when(parkingService.registerEntry(eq("A123BC"), eq(CarType.SEDAN)))
                .thenReturn(record);

        mockMvc.perform(post("/api/v1/parking/entry")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "licensePlate": "A123BC",
                                    "carType": "SEDAN"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.entryTime").exists());
    }

    @Test
    void givenValidExitRequest_whenRegisterExit_thenReturnsExitTime() throws Exception {
        var now = LocalDateTime.now();
        var record = com.example.parkingservice.entity.ParkingRecord.builder()
                .licensePlate("A123BC")
                .carType(CarType.SEDAN)
                .entryTime(now.minusHours(1))
                .exitTime(now)
                .build();

        Mockito.when(parkingService.registerExit("A123BC")).thenReturn(record);

        mockMvc.perform(post("/api/v1/parking/exit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "licensePlate": "A123BC"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exitTime").exists());
    }

    @Test
    void givenReportParams_whenGetReport_thenReturnsReportData() throws Exception {
        var now = LocalDateTime.now();
        var yesterday = now.minusDays(1);
        var report = new ParkingReport(10, 90, 45.5);

        Mockito.when(parkingService.getReport(any(), any(), eq(100)))
                .thenReturn(report);

        mockMvc.perform(get("/api/v1/parking/report")
                        .param("start_date", yesterday.toString())
                        .param("end_date", now.toString())
                        .param("totalCapacity", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.occupied").value(10))
                .andExpect(jsonPath("$.free").value(90))
                .andExpect(jsonPath("$.avgDurationMinutes").value(45.5));
    }

    @Test
    void givenMissingLicensePlate_whenRegisterEntry_thenReturns400() throws Exception {
        mockMvc.perform(post("/api/v1/parking/entry")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                        "carType": "SUV"
                    }
                    """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void givenUnknownPlate_whenRegisterExit_thenReturns400() throws Exception {
        Mockito.when(parkingService.registerExit("Z999XX"))
                .thenThrow(new IllegalArgumentException("Автомобиль не найден или уже выехал"));

        mockMvc.perform(post("/api/v1/parking/exit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "licensePlate": "Z999XX"
                            }
                            """))
                .andExpect(status().isBadRequest());
    }

}
