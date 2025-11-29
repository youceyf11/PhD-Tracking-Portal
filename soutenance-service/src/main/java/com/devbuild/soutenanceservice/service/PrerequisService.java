package com.devbuild.soutenanceservice.service;

import com.devbuild.soutenanceservice.domain.entity.Prerequis;

import com.devbuild.soutenanceservice.exception.PrerequisNotMetException;
import com.devbuild.soutenanceservice.repository.PrerequisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PrerequisService {

    private final PrerequisRepository repository;

    public void validatePrerequis(Prerequis prerequis) {
        if (prerequis.getNbArticlesQ1Q2() < 2 || prerequis.getNbConferences() < 2 || prerequis.getHeuresFormation() < 200) {
            throw new PrerequisNotMetException("Prérequis non remplis");
        }
        prerequis.setValide(true);
        repository.save(prerequis);
    }
}
