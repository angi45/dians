package com.example.project1.repository;

import com.example.project1.entity.ProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProfileRepository extends JpaRepository<ProfileEntity, Long> {
    Optional<ProfileEntity> findByCompanyCode(String companyCode);
}
