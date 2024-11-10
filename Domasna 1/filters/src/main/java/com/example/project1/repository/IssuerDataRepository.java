package com.example.project1.repository;

import com.example.project1.entity.Issuer;
import com.example.project1.entity.IssuerData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface IssuerDataRepository extends JpaRepository<IssuerData, Long> {
    Optional<IssuerData> findByDateAndCompany(LocalDate date, Issuer company);
}
