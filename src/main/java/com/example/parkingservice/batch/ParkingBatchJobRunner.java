package com.example.parkingservice.batch;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class ParkingBatchJobRunner implements CommandLineRunner {

    private final JobLauncher jobLauncher;
    private final Job importParkingJob;

    @Override
    public void run(String... args) throws Exception {
        var params = new JobParametersBuilder()
                .addString("timestamp", LocalDateTime.now().toString())
                .toJobParameters();
        jobLauncher.run(importParkingJob, params);
    }
}
