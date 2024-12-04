package com.example.project1.service;

import com.example.project1.entity.ProfileEntity;
import com.example.project1.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final ProfileRepository profileRepository;

    public List<ProfileEntity> findAll() {
        return profileRepository.findAll();
    }

    public ProfileEntity findById(Long id) throws Exception {
        return profileRepository.findById(id).orElseThrow(Exception::new);
    }

}
