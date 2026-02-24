package com.genesis.industry.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
public class ProductMaterial extends PanacheEntity {

    @ManyToOne
@JsonIgnore
public Product product;

    @ManyToOne(optional = false)
    @JoinColumn(name = "raw_material_id")
    public RawMaterial rawMaterial;

    @Column(nullable = false)
    public Integer quantityUsed;
}