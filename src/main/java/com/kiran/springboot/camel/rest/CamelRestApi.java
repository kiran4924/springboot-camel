package com.kiran.springboot.camel.rest;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.model.rest.RestBindingMode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Component
public class CamelRestApi extends RouteBuilder {

    @Value("${camel.api.path}")
    private String camelContext;

    @Value("${server.port}")
    private Integer serverPort;

    @Override
    public void configure() throws Exception {
        CamelContext context = new DefaultCamelContext();
        restConfiguration()
                .contextPath(camelContext)
                .port(serverPort)
                .enableCORS(true)
                .apiContextPath("/api-doc")
                .apiProperty("api.title", "Spring Boot, Camel REST API")
                .apiProperty("api.version", "v1")
                .apiContextRouteId("doc-api")
                .component("servlet")
                .bindingMode(RestBindingMode.json)
                .dataFormatProperty("prettyPrint", "true");

        rest("/conveyorbelt/").description("REST Service to route luggage")
                .id("luggage-router")
                .post("/luggage")
                .produces(MediaType.APPLICATION_JSON)
                .consumes(MediaType.APPLICATION_JSON)
                .bindingMode(RestBindingMode.auto)
                .type(Luggage.class)
                .enableCORS(true)
                .to("direct:luggageDispatcher");

        from("direct:luggageDispatcher").routeId("dispatcher")
                .tracing()
                .log(">>> Luggage ID: ${body.id}")
                .log(">>> Luggage Headed to: ${body.destination}")
                .choice()
                    .when(simple("${body.destination} == 'Sydney'"))
                        .to("direct:sydneyDispatcher")
                    .when(simple("${body.destination} == 'Melbourne'"))
                        .to("direct:melbourneDispatcher")
                    .otherwise()
                        .to("direct:errorDispatcher");
//                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(Response.Status.ACCEPTED.getStatusCode()));

        from("direct:sydneyDispatcher").routeId("sydneyDispatcher")
                .log(">>> Received Luggage ID: ${body.id}")
                .log(">>> Weight: ${body.weight}")
                .log(">>> Volume: ${body.volume}")
                .log(">>> Is Heavy: ${body.heavy}")
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(Response.Status.ACCEPTED.getStatusCode()));

        from("direct:melbourneDispatcher").routeId("melbourneDispatcher")
                .log(">>> Received Luggage ID: ${body.id}")
                .log(">>> Weight: ${body.weight}")
                .log(">>> Volume: ${body.volume}")
                .log(">>> Is Heavy: ${body.heavy}")
            .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(Response.Status.ACCEPTED.getStatusCode()));

        from("direct:errorDispatcher").routeId("errorDispatcher")
                .log(">>> !!!  Failed processing luggage with Luggage ID: ${body.id}, Destination: ${body.destination}  !!!")
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(Response.Status.NOT_ACCEPTABLE.getStatusCode()));
    }
}
