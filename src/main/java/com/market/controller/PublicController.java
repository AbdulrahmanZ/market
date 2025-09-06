package com.market.controller;

import com.market.repository.ParameterRepository;
import com.market.setup.Setup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
@RequestMapping("/public")
public class PublicController {

    @Autowired
    ParameterRepository parameterRepository;

    @GetMapping("/initial-info")
    public HashMap<String, Object> getInitInfo() {
        HashMap<String, Object> info = new HashMap<>();
        parameterRepository.findByCodeIn(Arrays.asList(Setup.Parameter.BASE_URL,
                        Setup.Parameter.IMAGE_BASE_URL,
                        Setup.Parameter.CHECK_AFTER,
                        Setup.Parameter.NOTIFICATION_ALARM,
                        Setup.Parameter.ITEM_IMAGE_NUMBER,
                        Setup.Parameter.WHATSAPP_ICON_DISPLAY))
                .stream()
                .filter(Objects::nonNull)
                .filter(p -> StringUtils.hasText(p.getValue()))
                .forEach(p -> info.put(p.getCode(), p.getValue()));

        return info;
    }

}
