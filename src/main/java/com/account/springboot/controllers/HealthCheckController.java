package com.account.springboot.controllers;

import com.account.springboot.services.RatesService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URISyntaxException;

@RestController
@Slf4j
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class HealthCheckController {

    private final RatesService conversionRatesService;

    @GetMapping("/")
    public String index() {
        return "Ping";
    }

}
