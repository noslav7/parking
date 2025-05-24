package com.example.parkingservice.batch;

import com.example.parkingservice.entity.ParkingRecord;
import com.example.parkingservice.repository.ParkingRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.PlatformTransactionManager;

import java.beans.PropertyEditorSupport;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class ParkingBatchConfig {

    private final ParkingRecordRepository repository;
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    @Bean
    public FlatFileItemReader<ParkingCsvRecord> parkingReader() {
        BeanWrapperFieldSetMapper<ParkingCsvRecord> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(ParkingCsvRecord.class);

        DefaultConversionService conversionService = new DefaultConversionService();
        conversionService.addConverter(new StringToLocalDateTimeConverter());
        fieldSetMapper.setConversionService(conversionService);

        fieldSetMapper.setCustomEditors(Map.of(LocalDateTime.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) {
                if (text == null || text.trim().isEmpty()) {
                    setValue(null);
                } else {
                    setValue(LocalDateTime.parse(text, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                }
            }
        }));

        return new FlatFileItemReaderBuilder<ParkingCsvRecord>()
                .name("parkingCsvReader")
                .resource(new ClassPathResource("data.csv"))
                .delimited()
                .names("licensePlate", "carType", "entryTime", "exitTime")
                .fieldSetMapper(fieldSetMapper)
                .linesToSkip(1)
                .build();
    }


    @Bean
    public ParkingCsvProcessor parkingProcessor() {
        return new ParkingCsvProcessor();
    }

    @Bean
    public RepositoryItemWriter<ParkingRecord> parkingWriter() {
        RepositoryItemWriter<ParkingRecord> writer = new RepositoryItemWriter<>();
        writer.setRepository(repository);
        writer.setMethodName("save");
        return writer;
    }

    @Bean
    public Step importParkingStep() {
        return new StepBuilder("importParkingStep", jobRepository)
                .<ParkingCsvRecord, ParkingRecord>chunk(10, transactionManager)
                .reader(parkingReader())
                .processor(parkingProcessor())
                .writer(parkingWriter())
                .build();
    }

    @Bean
    public Job importParkingJob() {
        return new JobBuilder("importParkingJob", jobRepository)
                .start(importParkingStep())
                .build();
    }

}



