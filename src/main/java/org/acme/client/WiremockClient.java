package org.acme.client;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "wiremock-client")
@Path("/wiremock")
public interface WiremockClient {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    Response getCustomers();

}

