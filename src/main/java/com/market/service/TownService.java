package com.market.service;

import com.market.model.Town;
import com.market.repository.TownRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class TownService {

    private final TownRepository townRepository;

    TownService(TownRepository townRepository) {
        this.townRepository = townRepository;
    }

    public Town createTown(Town town) {
        if (townRepository.existsByName(town.getName())) {
            throw new RuntimeException("Town name already exists");
        }
        if (townRepository.existsByCode(town.getCode())) {
            throw new RuntimeException("Town code already exists");
        }
        return townRepository.save(town);
    }

    public Town getTownById(Long id) {
        return townRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Town not found"));
    }

    public Town getTownByCode(String code) {
        return townRepository.findByCode(code);
    }

    public Page<Town> getAllTowns(Pageable pageable) {
        return townRepository.findAll(pageable);
    }

    public Town updateTown(Long id, Town townDetails) {
        Town town = getTownById(id);

        // Check if name is being changed and if new name already exists
        if (!town.getName().equals(townDetails.getName()) &&
                townRepository.existsByName(townDetails.getName())) {
            throw new RuntimeException("Town name already exists");
        }

        // Check if code is being changed and if new code already exists
        if (townDetails.getCode() != null &&
                !townDetails.getCode().equals(town.getCode()) &&
                townRepository.existsByCode(townDetails.getCode())) {
            throw new RuntimeException("Town code already exists");
        }

        town.setName(townDetails.getName());
        town.setCode(townDetails.getCode());
        return townRepository.save(town);
    }

    public void deleteTown(Long id) {
        townRepository.softDeleteById(id);
    }

    public Page<Town> getTownByContainingName(String name, Pageable pageable) {
        return townRepository.findByContainName(name, pageable);
    }
}
