package com.genesis.industry.resource;

import com.genesis.industry.entity.RawMaterial;
import com.genesis.industry.dto.RawMaterialDTO;

import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.util.List;

@Path("/raw-materials")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RawMaterialResource {

    @GET
    public List<RawMaterial> list() {
        return RawMaterial.listAll();
    }

    @GET
    @Path("/{id}")
    public RawMaterial get(@PathParam("id") Long id) {
        return RawMaterial.findById(id);
    }

    @POST
    @Transactional
    public RawMaterial create(RawMaterialDTO dto) {
        RawMaterial material = new RawMaterial();
        material.name = dto.name;
        material.stockQuantity = dto.stockQuantity;
        material.persist();
        return material;
    }

    @PUT
    @Path("/{id}")
    @Transactional
    public RawMaterial update(@PathParam("id") Long id, RawMaterialDTO dto) {
        RawMaterial material = RawMaterial.findById(id);
        if (material == null) {
            throw new NotFoundException();
        }
        material.name = dto.name;
        material.stockQuantity = dto.stockQuantity;
        return material;
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    public void delete(@PathParam("id") Long id) {
        RawMaterial.deleteById(id);
    }
}