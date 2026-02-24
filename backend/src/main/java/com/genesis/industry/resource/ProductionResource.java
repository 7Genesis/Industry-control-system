package com.genesis.industry.resource;

import com.genesis.industry.dto.ProductionSuggestionDTO;
import com.genesis.industry.service.ProductionService;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/production")
@Produces(MediaType.APPLICATION_JSON)
public class ProductionResource {

    @Inject
    ProductionService productionService;

    @GET
    @Path("/suggestion")
    public ProductionSuggestionDTO getProductionSuggestion() {
        return productionService.calculateProductionSuggestion();
    }
}