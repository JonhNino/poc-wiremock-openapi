package org.acme.client;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "auth-client")
@Path("/auth")
public interface AuthClient {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    Response getToken();

}

