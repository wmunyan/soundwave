package org.cybertron.soundwave

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get

@Controller("/ping")
class PingController {
    @Get
    def ping() {
        return ["response": "pong"]
    }
}
