package com.genesis.industry.resource;

import com.genesis.industry.entity.Product;
import com.genesis.industry.entity.RawMaterial;
import com.genesis.industry.entity.ProductMaterial;
import com.genesis.industry.dto.ProductMaterialDTO;

import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/products")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProductResource {

    // ===============================
    // CRUD PRODUCT
    // ===============================

    @GET
    public List<Product> list() {
        return Product.listAll();
    }

    @POST
    @Transactional
    public Response create(Product product) {
        product.persist();
        return Response.status(Response.Status.CREATED).entity(product).build();
    }

    @PUT
    @Path("/{id}")
    @Transactional
    public Response update(@PathParam("id") Long id, Product updatedProduct) {
        Product product = Product.findById(id);
        if (product == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        product.name = updatedProduct.name;
        product.price = updatedProduct.price;

        return Response.ok(product).build();
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    public Response delete(@PathParam("id") Long id) {
        Product product = Product.findById(id);
        if (product == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        product.delete();
        return Response.noContent().build();
    }

    // ===============================
    // RF003 - ASSOCIATE RAW MATERIAL
    // ===============================

    @POST
    @Path("/{productId}/materials")
    @Transactional
    public Response addMaterialToProduct(@PathParam("productId") Long productId,
                                         ProductMaterialDTO dto) {

        if (dto == null || dto.rawMaterialId == null || dto.quantityUsed == null || dto.quantityUsed <= 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Invalid data")
                    .build();
        }

        Product product = Product.findById(productId);
        if (product == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Product not found")
                    .build();
        }

        RawMaterial rawMaterial = RawMaterial.findById(dto.rawMaterialId);
        if (rawMaterial == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Raw material not found")
                    .build();
        }

        ProductMaterial pm = new ProductMaterial();
        pm.product = product;
        pm.rawMaterial = rawMaterial;
        pm.quantityUsed = dto.quantityUsed;

        pm.persist();

        return Response.status(Response.Status.CREATED).entity(pm).build();
    }

    @PUT
    @Path("/{productId}/materials/{pmId}")
    @Transactional
    public Response updateMaterial(@PathParam("pmId") Long pmId,
                                   ProductMaterialDTO dto) {

        ProductMaterial pm = ProductMaterial.findById(pmId);
        if (pm == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        if (dto.quantityUsed == null || dto.quantityUsed <= 0) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        pm.quantityUsed = dto.quantityUsed;

        return Response.ok(pm).build();
    }

    @DELETE
    @Path("/{productId}/materials/{pmId}")
    @Transactional
    public Response removeMaterial(@PathParam("pmId") Long pmId) {

        ProductMaterial pm = ProductMaterial.findById(pmId);
        if (pm == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        pm.delete();
        return Response.noContent().build();
    }
}