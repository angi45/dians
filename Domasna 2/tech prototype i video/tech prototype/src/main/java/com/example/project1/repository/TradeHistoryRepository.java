package com.example.project1.repository;

import com.example.project1.entity.ProfileEntity;
import com.example.project1.entity.TradeHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TradeHistoryRepository extends JpaRepository<TradeHistoryEntity, Long> {
    Optional<TradeHistoryEntity> findByDateAndCompany(LocalDate date, ProfileEntity company);
    List<TradeHistoryEntity> findByCompanyIdAndDateBetween(Long companyId, LocalDate from, LocalDate to);
}
