package org.cybertron.soundwave.windows

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get

@Controller("/windows")
class WindowsController {

    @Get
    def ping() {
        return ["response": "windows controller"]
    }

    @Get("/health")
    def health() {
        return ["health": "up and running"]
    }
}