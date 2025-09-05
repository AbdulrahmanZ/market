package com.market.controller;

import com.market.dto.ParameterRequest;
import com.market.model.Parameter;
import com.market.service.AuthenticationService;
import com.market.service.ParameterService;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/parameters")
public class ParameterController {

    private static final Logger logger = LoggerFactory.getLogger(ParameterController.class);
    private final ParameterService parameterService;
    private final AuthenticationService authenticationService;

    // Constructor-based dependency injection
    public ParameterController(ParameterService parameterService, AuthenticationService authenticationService) {
        this.parameterService = parameterService;
        this.authenticationService = authenticationService;
    }

    /**
     * Creates a new Parameter. Requires admin authentication.
     * Maps to POST /parameters
     */
    @PostMapping()
    @Transactional
    public ResponseEntity<Parameter> createParameter(@Valid @RequestBody ParameterRequest parameterRequest) {
        authenticationService.adminUserCheck();

        Parameter parameter = new Parameter();
        parameter.setName(parameterRequest.getName());
        parameter.setCode(parameterRequest.getCode());
        parameter.setValue(parameterRequest.getValue()); // Assuming a 'value' field in the request

        Parameter createdParameter = parameterService.createParameter(parameter);
        return ResponseEntity.ok(createdParameter);
    }

    /**
     * Retrieves a Parameter by its ID.
     * Maps to GET /parameters/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Parameter> getParameter(@PathVariable Long id) {
        Parameter parameter = parameterService.getParameterById(id);
        return ResponseEntity.ok(parameter);
    }

    /**
     * Retrieves a Parameter by its unique code.
     * Maps to GET /parameters/code/{code}
     */
    @GetMapping("/code/{code}")
    public ResponseEntity<Parameter> getParameterByCode(@PathVariable String code) {
        Parameter parameter = parameterService.getParameterByCode(code);
        if (parameter == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(parameter);
    }

    /**
     * Retrieves a paginated list of all Parameters.
     * Maps to GET /parameters
     */
    @GetMapping
    public ResponseEntity<Page<Parameter>> getAllParameters(Pageable pageable) {
        Page<Parameter> parameters = parameterService.getAllParameters(pageable);
        return ResponseEntity.ok(parameters);
    }


    /**
     * Updates an existing Parameter. Requires admin authentication.
     * Maps to PUT /parameters/{id}
     */
    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<Parameter> updateParameter(@PathVariable Long id,
                                                     @Valid @RequestBody Parameter request) {
        authenticationService.adminUserCheck();

        Parameter existedParameter = parameterService.getParameterById(id);
        if (request.getName() != null) existedParameter.setName(request.getName());
        if (request.getCode() != null) existedParameter.setCode(request.getCode());
        if (request.getValue() != null) existedParameter.setValue(request.getValue()); // Assuming 'value' field

        Parameter updatedParameter = parameterService.updateParameter(id, existedParameter);
        return ResponseEntity.ok(updatedParameter);
    }

    /**
     * Deletes a Parameter by its ID. Requires admin authentication.
     * Maps to DELETE /parameters/{id}
     */
    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> deleteParameter(@PathVariable Long id) {
        authenticationService.adminUserCheck();
        parameterService.deleteParameter(id);
        return ResponseEntity.noContent().build();
    }
}
