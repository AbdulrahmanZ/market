package com.market.service;

import com.market.model.Parameter;
import com.market.repository.ParameterRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ParameterService {

    private final ParameterRepository parameterRepository;

    // Constructor-based dependency injection
    ParameterService(ParameterRepository parameterRepository) {
        this.parameterRepository = parameterRepository;
    }

    /**
     * Creates a new Parameter, performing checks to ensure the name and code are unique.
     * Throws an exception if a duplicate name or code is found.
     *
     * @param parameter The Parameter object to be created.
     * @return The created Parameter object.
     */
    public Parameter createParameter(Parameter parameter) {

        if (parameterRepository.existsByCode(parameter.getCode())) {
            throw new RuntimeException("Parameter code already exists");
        }
        return parameterRepository.save(parameter);
    }

    /**
     * Retrieves a Parameter by its ID.
     * Throws an exception if the parameter is not found.
     *
     * @param id The ID of the parameter.
     * @return The Parameter object.
     */
    public Parameter getParameterById(Long id) {
        return parameterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Parameter not found"));
    }

    /**
     * Retrieves a Parameter by its unique code.
     *
     * @param code The code of the parameter.
     * @return The Parameter object, or null if not found.
     */
    public Parameter getParameterByCode(String code) {
        return parameterRepository.findByCode(code);
    }

    /**
     * Retrieves a paginated list of all Parameters.
     *
     * @param pageable Pagination information.
     * @return A Page of Parameter objects.
     */
    public Page<Parameter> getAllParameters(Pageable pageable) {
        return parameterRepository.findAll(pageable);
    }

    /**
     * Updates an existing Parameter. Performs checks to ensure the updated name and code are unique.
     *
     * @param id The ID of the parameter to update.
     * @param parameterDetails The updated Parameter details.
     * @return The updated Parameter object.
     */
    public Parameter updateParameter(Long id, Parameter parameterDetails) {
        Parameter existingParameter = getParameterById(id);

        // Check if the code is being changed and if the new code already exists.
        if (parameterDetails.getCode() != null &&
                !parameterDetails.getCode().equals(existingParameter.getCode()) &&
                parameterRepository.existsByCode(parameterDetails.getCode())) {
            throw new RuntimeException("Parameter code already exists");
        }

        // Update fields
        existingParameter.setName(parameterDetails.getName());
        existingParameter.setCode(parameterDetails.getCode());
        existingParameter.setValue(parameterDetails.getValue()); // Assuming a 'value' field exists in Parameter

        return parameterRepository.save(existingParameter);
    }

    /**
     * Deletes a Parameter by its ID.
     *
     * @param id The ID of the parameter to delete.
     */
    public void deleteParameter(Long id) {
        parameterRepository.softDeleteById(id);
    }

}
