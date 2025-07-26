package com.market.service;

import com.market.model.Town;
import com.market.repository.TownRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class TownService {

    @Autowired
    TownRepository townRepository;


    public Town createTown(Town town) {
        if (townRepository.existsByName(town.getName())) {
            throw new RuntimeException("Town name already exists");
        }
        return townRepository.save(town);
    }

    public Town getTownById(Long id) {
        return townRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Town not found"));
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

        town.setName(townDetails.getName());
        return townRepository.save(town);
    }

    public void deleteTown(Long id) {
        Town town = getTownById(id);
        townRepository.delete(town);
    }

    public Page<Town> getTownByContainingName(String name, Pageable pageable) {
        return townRepository.findByContainName(name, pageable);
    }
}
