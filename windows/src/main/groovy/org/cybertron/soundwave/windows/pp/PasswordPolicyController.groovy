package org.cybertron.soundwave.windows.pp

import groovy.json.JsonOutput
import groovy.json.StreamingJsonBuilder
import groovy.xml.StreamingMarkupBuilder
import groovy.xml.XmlParser
import groovy.xml.XmlParserFactory
import groovy.xml.XmlSlurper
import groovy.xml.XmlSlurperFactory
import groovy.xml.XmlUtil
import io.micronaut.core.io.IOUtils
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Consumes
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Produces
import io.micronaut.json.tree.JsonArray
import io.micronaut.json.tree.JsonNode
import io.micronaut.scheduling.TaskExecutors
import io.micronaut.scheduling.annotation.ExecuteOn

@Controller("/windows/password-policy")
class PasswordPolicyController {
    @Get
    def get() {
        return ["family": "windows", "controller": "password-policy"]
    }

    @Post(value = "/read", produces = MediaType.APPLICATION_XML, processes = MediaType.APPLICATION_XML)
    @ExecuteOn(TaskExecutors.IO) //
    String read(@Body InputStream inputStream) throws IOException { //
        IOUtils.readText(new BufferedReader(new InputStreamReader(inputStream))) //
    }

    @Post(value = "/scap", produces = MediaType.APPLICATION_XML, consumes = MediaType.APPLICATION_XML)
    @ExecuteOn(TaskExecutors.IO) //
    def scap(@Body InputStream artifactExpressionXml) {
        def bodyText = IOUtils.readText(new BufferedReader(new InputStreamReader(artifactExpressionXml)))

        /*
        <passwordpolicy_test id=[artifact_oval_id]>
            <passwordpolicy_object/>
            <passwordpolicy_state>
                <[artifact_parameter_value]
            </passwordpolicy_state>
        </passwordpolicy_test>
        */
        def namespaceMap = ["ae": "http://namespace.for.ae"]
        def xml = new StreamingMarkupBuilder().bind {
            namespaces << namespaceMap

            // We need a "wrapper" root element to inject the namespace mappings
            "ae:artifact_expression_root" {
                mkp.yieldUnescaped bodyText
            }
        }
        def n = new XmlParser(false, false).parseText(xml.toString())
        //xml.toString()
        XmlUtil.serialize(n)
    }

    @Post(value = "/json", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_XML)
    @ExecuteOn(TaskExecutors.IO) //
    def json(@Body InputStream artifactExpressionXml) {
        def n = new XmlParser(false, false).parse(artifactExpressionXml)
        def nid = n.@"id".toString()
        def aoid = n."ae:artifact_oval_id".text()
        def aet = n."ae:title".text()

        def art = [:]
        def artifactType = n."ae:artifact"[0]
        art["artifact-type"] = artifactType.@type.toString()

        def params = []
        artifactType."ae:parameters"[0].children().each { c ->
            params << ["name": c.@"name", "datatype": c.@"dt", "value": c.text()]
        }
        art["parameters"] = params
        def tst = [:]
        def testType = n."ae:test"[0]
        tst["test-type"] = testType.@"type".toString()
        def tparams = []
        testType."ae:parameters"[0].children().each { c ->
            tparams << ["name": c.@"name", "datatype": c.@"dt", "value": c.text()]
        }
        tst["parameters"] = tparams

        StringWriter writer = new StringWriter()
        StreamingJsonBuilder builder = new StreamingJsonBuilder(writer)
        builder.expression {
            "id" nid
            "oval_id" aoid
            "title" aet
            "artifact" art
            "test" tst
        }
        JsonOutput.prettyPrint(writer.toString())
    }
}
