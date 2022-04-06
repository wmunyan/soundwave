package org.cybertron;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

@Controller("/health")
public class HealthController {

    @Get
    LinkedHashMap<String, Object> health() {
        LinkedHashMap<String, Object> rez = new LinkedHashMap<>();
        rez.put("application-health", "Up and Running.");
        rez.put("transforms", Arrays.asList("t1", "t2", "t3"));

        System.out.println(rez);
        return rez;
    }
}
