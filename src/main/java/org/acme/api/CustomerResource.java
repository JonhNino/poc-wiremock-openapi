package org.acme.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import org.acme.client.WiremockClient;
import org.acme.model.CustomerResponse;
import org.acme.model.ErrorResponse;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

import java.util.List;

@Path("/customers")
public class CustomerResource implements DefaultApi {

    private static final Logger LOG = Logger.getLogger(CustomerResource.class);

    @Inject
    @RestClient
    WiremockClient wiremockClient;

    @Inject
    ObjectMapper objectMapper;

    @Override
    public List<CustomerResponse> getCustomers(
            String authorization,
            String sessionId,
            String originDevice,
            String name,
            String status,
            Integer minAge) {

        // ── Log de todos los headers recibidos ──────────────────────────────
        LOG.infof("=== REQUEST RECEIVED ===");
        LOG.infof("[HEADER] Authorization  : %s", authorization);
        LOG.infof("[HEADER] session-id     : %s", sessionId);
        LOG.infof("[HEADER] origin-device  : %s", originDevice);

        // ── Log de todos los query params recibidos ─────────────────────────
        LOG.infof("[PARAM]  name           : %s", name);
        LOG.infof("[PARAM]  status         : %s", status);
        LOG.infof("[PARAM]  minAge         : %s", minAge);
        LOG.infof("========================");

        // ── Llamada al WireMock en localhost:3000/wiremock ──────────────────
        LOG.infof("[CLIENT] Llamando a WireMock -> GET /wiremock");
        Response wiremockResponse = wiremockClient.getCustomers();
        int httpStatus = wiremockResponse.getStatus();
        LOG.infof("[CLIENT] Status recibido: %d", httpStatus);

        // ── Si el cliente devuelve error, propagar la misma estructura ───────
        if (httpStatus >= 400) {
            String body = wiremockResponse.readEntity(String.class);
            LOG.errorf("[CLIENT] Error recibido del WireMock [%d]: %s", httpStatus, body);
            try {
                ErrorResponse error = objectMapper.readValue(body, ErrorResponse.class);
                throw new WebApplicationException(
                        Response.status(httpStatus).entity(error).type("application/json").build()
                );
            } catch (WebApplicationException e) {
                throw e;
            } catch (Exception e) {
                LOG.errorf("[CLIENT] No se pudo deserializar el error: %s", e.getMessage());
                throw new WebApplicationException(
                        Response.status(httpStatus).entity(body).type("application/json").build()
                );
            }
        }

        // ── Deserializar la lista de CustomerResponse ────────────────────────
        try {
            String body = wiremockResponse.readEntity(String.class);
            List<CustomerResponse> result = objectMapper.readValue(
                    body,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, CustomerResponse.class)
            );
            LOG.infof("[CLIENT] Respuesta recibida: %d clientes", result.size());
            return result;
        } catch (Exception e) {
            LOG.errorf("[CLIENT] Error deserializando respuesta: %s", e.getMessage());
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }
    }
}

