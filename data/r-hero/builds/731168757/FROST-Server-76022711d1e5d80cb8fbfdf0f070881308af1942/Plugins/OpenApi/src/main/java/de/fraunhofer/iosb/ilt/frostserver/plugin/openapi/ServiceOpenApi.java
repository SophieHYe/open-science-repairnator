/*
 * Copyright (C) 2020 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
 * Karlsruhe, Germany.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.fraunhofer.iosb.ilt.frostserver.plugin.openapi;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.fraunhofer.iosb.ilt.frostserver.plugin.openapi.spec.GeneratorContext;
import de.fraunhofer.iosb.ilt.frostserver.plugin.openapi.spec.OADoc;
import de.fraunhofer.iosb.ilt.frostserver.plugin.openapi.spec.OpenApiGenerator;
import de.fraunhofer.iosb.ilt.frostserver.service.Service;
import de.fraunhofer.iosb.ilt.frostserver.service.ServiceRequest;
import de.fraunhofer.iosb.ilt.frostserver.service.ServiceResponse;
import de.fraunhofer.iosb.ilt.frostserver.util.SimpleJsonMapper;
import org.slf4j.LoggerFactory;

/**
 * Handles the service requests for the DataArray plugin. This is the request to
 * /api.
 *
 * @author scf
 */
public class ServiceOpenApi {

    /**
     * The path for the OpenApi specification.
     */
    public static final String PATH_GET_OPENAPI_SPEC = "/api";

    /**
     * The RequestType definition for the CreateObservations request type.
     */
    public static final String REQUEST_TYPE_GET_OPENAPI_SPEC = "openApi";

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ServiceOpenApi.class.getName());

    public ServiceResponse<String> executeRequest(final ServiceRequest request) {
        GeneratorContext context = new GeneratorContext()
                .initFromRequest(request);
        OADoc oaDoc = OpenApiGenerator.generateOpenApiDocument(context);
        final ServiceResponse response = new ServiceResponse<>();
        try {
            response.setResultFormatted(SimpleJsonMapper.getSimpleObjectMapper().writeValueAsString(oaDoc));
            response.setCode(200);
            response.setContentType("application/json");
            return response;
        } catch (JsonProcessingException ex) {
            LOGGER.error("Failed to encode OA Document.", ex);
            return Service.errorResponse(response, 500, "Failed to encode document");
        }
    }

    public static boolean paramValueAsBool(ServiceRequest request, String name, boolean dflt) {
        String[] values = request.getParameterMap().get(name);
        if (values == null || values.length == 0) {
            return dflt;
        }
        String value = values[0];
        if (value == null) {
            return dflt;
        }
        return value.equalsIgnoreCase("true");
    }

    public static int paramValueAsInt(ServiceRequest request, String name, int dflt) {
        String[] values = request.getParameterMap().get(name);
        if (values == null || values.length == 0) {
            return dflt;
        }
        String value = values[0];
        if (value == null) {
            return dflt;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return dflt;
        }
    }
}
