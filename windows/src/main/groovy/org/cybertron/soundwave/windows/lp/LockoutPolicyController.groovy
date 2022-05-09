package org.cybertron.soundwave.windows.lp

import groovy.json.JsonOutput
import groovy.json.StreamingJsonBuilder
import groovy.xml.StreamingMarkupBuilder
import groovy.xml.XmlParser
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.scheduling.TaskExecutors
import io.micronaut.scheduling.annotation.ExecuteOn

@Controller("/windows/lockout-policy")
class LockoutPolicyController {
    @Get("/info")
    def info() {
        return ["family": "windows", "type": "lockout-policy", "methods": ["scap", "xml", "json"]]
    }

    /**
     * Mirror Optimus SCAP content generation
     * @param artifactExpressionXml
     * @return
     * @throws IOException
     */
    @Post(value = "/scap", produces = MediaType.TEXT_PLAIN, processes = MediaType.APPLICATION_XML)
    @ExecuteOn(TaskExecutors.IO) //
    String scap(@Body InputStream artifactExpressionXml) throws IOException { //
        def xmlNode = new XmlParser(false, false).parse(artifactExpressionXml)
        def artifactMap = build(xmlNode)

        def xml = new StreamingMarkupBuilder().bind {
            "oval-content" {
                "under-construction"
            }
        }
        // ALWAYS return a base64 encoded string...
        return xml.toString().bytes.encodeBase64().toString()
    }

    /**
     * Create the custom XML representation
     * @param inputStream
     * @return
     * @throws IOException
     */
    @Post(value = "/xml", produces = MediaType.APPLICATION_XML, consumes = MediaType.APPLICATION_XML)
    @ExecuteOn(TaskExecutors.IO) //
    def xml(@Body InputStream artifactExpressionXml) {
        def xmlNode = new XmlParser(false, false).parse(artifactExpressionXml)
        def artifactMap = build(xmlNode)

        def xml = new StreamingMarkupBuilder().bind {
            "assessment" (id: artifactMap["id"], family: artifactMap["family"], type: artifactMap["type"]) {
                "title" artifactMap["title"]
                "collection" ("existence_check": "all")
                "evaluation" ("item_check": "all") {
                    "evaluation_entity" (
                            name: artifactMap["entity_name"],
                            datatype: artifactMap["entity_type"],
                            operator: artifactMap["entity_operator"],
                            artifactMap["entity_value"])
                }
            }
        }
        //def n = new XmlParser(false, false).parseText(xml.toString())
        //XmlUtil.serialize(n)
        xml.toString()
    }

    /**
     * Create the custom JSON representation
     * @param artifactExpressionXml
     * @return
     */
    @Post(value = "/json", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_XML)
    @ExecuteOn(TaskExecutors.IO) //
    def json(@Body InputStream artifactExpressionXml) {
        def xmlNode = new XmlParser(false, false).parse(artifactExpressionXml)
        def artifactMap = build(xmlNode)

        StringWriter writer = new StringWriter()
        StreamingJsonBuilder builder = new StreamingJsonBuilder(writer)
        builder {
            "id" artifactMap["id"]
            "family" artifactMap["family"]
            "type" artifactMap["type"]
            "title" artifactMap["title"]
            "collection" {
                "existence_check" "all"
            }
            "evaluation" {
                "item_check" "all"
                "evaluation_entity" {
                    "name" artifactMap["entity_name"]
                    "datatype" artifactMap["entity_type"]
                    "operator" artifactMap["entity_operator"]
                    "value" artifactMap["entity_value"]
                }
            }
        }
        JsonOutput.prettyPrint(writer.toString())
    }

    /**
     * Build a map of the necessary/dynamic password policy information
     * @param xmlNode
     * @return a map
     */
    private def build(def xmlNode) {
        def artifactType = xmlNode."ae:artifact"[0]
        def artifactParameter = artifactType."ae:parameters"[0].children()[0]

        def testType = xmlNode."ae:test"[0]
        def valueParameter = testType."ae:parameters"[0].children().find { n ->
            n instanceof Node && n.@"name".toString() == "value"
        }
        def datatypeParameter = testType."ae:parameters"[0].children().find { n ->
            n instanceof Node && n.@"name".toString() == "data_type"
        }

        return [
            "id": xmlNode."ae:artifact_oval_id".text(),
            "family": "windows",
            "type": "lockoutpolicy",
            "title": xmlNode."ae:title".text(),
            "entity_name": "lockout_${artifactParameter.text().toLowerCase().replace(" ", "_")}",
            "entity_type": datatypeParameter.text(),
            "entity_operator": xmlNode."ae:test"[0].@"type".toString(),
            "entity_value": valueParameter.text()
        ]
    }
}

