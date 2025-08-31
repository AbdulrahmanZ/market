package com.market.controller;

import com.market.model.Town;
import com.market.service.AuthenticationService;
import com.market.service.TownService;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/towns")
public class TownController {

    private static final Logger logger = LoggerFactory.getLogger(TownController.class);
    private final TownService townService;
    private final AuthenticationService authenticationService;

    public TownController(TownService townService, AuthenticationService authenticationService) {
        this.townService = townService;
        this.authenticationService = authenticationService;
    }

    @PostMapping
    @Transactional
    public ResponseEntity<Town> createTown(@Valid @RequestBody Town town) {
        authenticationService.adminUserCheck();
        Town createdTown = townService.createTown(town);
        return ResponseEntity.ok(createdTown);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Town> getTown(@PathVariable Long id) {
        Town town = townService.getTownById(id);
        return ResponseEntity.ok(town);
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<Town> getTownByCode(@PathVariable String code) {
        Town town = townService.getTownByCode(code);
        if (town == null) {
            return ResponseEntity.notFound().build();
        }
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
        authenticationService.adminUserCheck();
        Town townDetails = new Town();
        townDetails.setName(request.getName());
        townDetails.setCode(request.getCode());

        Town updatedTown = townService.updateTown(id, townDetails);
        return ResponseEntity.ok(updatedTown);
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> deleteTown(@PathVariable Long id) {
        authenticationService.adminUserCheck();
        townService.deleteTown(id);
        return ResponseEntity.noContent().build();
    }

}
