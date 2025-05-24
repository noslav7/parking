package com.example.parkingservice.repository;

import com.example.parkingservice.entity.CarType;
import com.example.parkingservice.entity.ParkingRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ParkingRecordRepositoryTest {

    @Autowired
    private ParkingRecordRepository repository;

    @Test
    @DisplayName("Найти запись по номеру машины и null-выезду")
    void givenActiveCar_whenFindByPlate_thenReturnsRecord() {
        var record = ParkingRecord.builder()
                .licensePlate("A123BC")
                .carType(CarType.SUV)
                .entryTime(LocalDateTime.now().minusHours(1))
                .build();
        repository.save(record);

        Optional<ParkingRecord> found = repository.findByLicensePlateAndExitTimeIsNull("A123BC");
        assertThat(found).isPresent();
        assertThat(found.get().getLicensePlate()).isEqualTo("A123BC");
    }

    @Test
    @DisplayName("Получить все записи за указанный период")
    void givenRangeWithEntries_whenFindBetween_thenReturnsMatchingRecords() {
        var now = LocalDateTime.now();
        var earlier = now.minusDays(1);
        var later = now.plusDays(1);

        repository.save(ParkingRecord.builder()
                .licensePlate("B456DE")
                .carType(CarType.SEDAN)
                .entryTime(now)
                .build());

        List<ParkingRecord> results = repository.findAllByEntryTimeBetween(earlier, later);
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getLicensePlate()).isEqualTo("B456DE");
    }

    @Test
    @DisplayName("Подсчитать количество машин на парковке (exitTime is null)")
    void givenCarWithoutExit_whenCountByExitTimeIsNull_thenReturnsCorrectCount() {
        repository.save(ParkingRecord.builder()
                .licensePlate("C789FG")
                .carType(CarType.TRUCK)
                .entryTime(LocalDateTime.now())
                .build());

        long count = repository.countByExitTimeIsNull();
        assertThat(count).isEqualTo(1);
    }

    @Test
    @DisplayName("Подсчитать количество машин уже выехавших (exitTime is not null)")
    void givenCarWithExit_whenCountByExitTimeIsNotNull_thenReturnsCorrectCount() {
        var record = ParkingRecord.builder()
                .licensePlate("D000HI")
                .carType(CarType.SEDAN)
                .entryTime(LocalDateTime.now().minusHours(2))
                .exitTime(LocalDateTime.now().minusHours(1))
                .build();

        repository.save(record);

        long count = repository.countByExitTimeIsNotNull();
        assertThat(count).isEqualTo(1);
    }

    @Test
    @DisplayName("Вычислить среднюю длительность парковки в секундах")
    void givenMultipleExitedCars_whenCalculateAverage_thenReturnsNonZero() {
        var now = LocalDateTime.now();
        var record1 = ParkingRecord.builder()
                .licensePlate("X001AA")
                .carType(CarType.SEDAN)
                .entryTime(now.minusHours(2))
                .exitTime(now.minusHours(1))
                .build();
        var record2 = ParkingRecord.builder()
                .licensePlate("X002BB")
                .carType(CarType.SUV)
                .entryTime(now.minusHours(3))
                .exitTime(now.minusHours(1))
                .build();

        repository.saveAll(List.of(record1, record2));

        Double avg = repository.findAverageParkingDurationInSeconds(now.minusDays(1), now);
        assertThat(avg).isNotNull();
        assertThat(avg).isGreaterThan(0.0);
    }
}
