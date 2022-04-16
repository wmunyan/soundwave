package org.cybertron.soundwave.windows.pp

import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post
import io.micronaut.json.tree.JsonArray

@Controller("/windows/password-policy/scap")
class PasswordPolicySCAPController {
    @Post
    def pp(@Body def artifactExpressionJson) {
        Iterable artifactExpressions = ((JsonArray)artifactExpressionJson).values()
        def aes = []
        artifactExpressions.iterator().each { ae ->
            aes << ["id": ae.get("id"), "title": ae.get("title")]
        }
        return ["controller": "/windows/password-policy/scap", "AEs": aes]
    }
}
