package com.market.controller;

import com.market.model.Town;
import com.market.service.AuthenticationService;
import com.market.service.TownService;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/towns")
public class TownController {

    @Autowired
    TownService townService;

    @Autowired
    AuthenticationService authenticationService;

    @PostMapping
    @Transactional
    public ResponseEntity<Town> createTown(@Valid @RequestBody Town town) {
        if (!authenticationService.getCurrentUser().getAdmin())
            throw new RuntimeException("Unauthorized - Non-admin user");
        Town createdTown = townService.createTown(town);
        return ResponseEntity.ok(createdTown);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Town> getTown(@PathVariable Long id) {
        Town town = townService.getTownById(id);
        return ResponseEntity.ok(town);
    }

    @GetMapping
    public ResponseEntity<Page<Town>> getAllTowns(Pageable pageable) {
        Page<Town> towns = townService.getAllTowns(pageable);
        return ResponseEntity.ok(towns);
    }


    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<Town> updateTown(@PathVariable Long id,
                                           @Valid @RequestBody Town request) {
        if (!authenticationService.getCurrentUser().getAdmin())
            throw new RuntimeException("Unauthorized - Non-admin user");
        Town townDetails = new Town();
        townDetails.setName(request.getName());

        Town updatedTown = townService.updateTown(id, townDetails);
        return ResponseEntity.ok(updatedTown);
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> deleteTown(@PathVariable Long id) {
        if (!authenticationService.getCurrentUser().getAdmin())
            throw new RuntimeException("Unauthorized - Non-admin user");
        townService.deleteTown(id);
        return ResponseEntity.noContent().build();
    }

}
