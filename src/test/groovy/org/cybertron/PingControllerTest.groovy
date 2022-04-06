package org.cybertron

import com.fasterxml.jackson.databind.JsonNode
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Specification

@MicronautTest
class PingControllerTest extends Specification {
    @Inject
    @Client("/ping")
    HttpClient client

    def "Ping"() {
        when:
        def rez = client.toBlocking().exchange("/", JsonNode.class)
        then:
        assert rez.getStatus() == HttpStatus.OK
        assert rez.getBody().get()."response".asText() == "pong"
    }
}
