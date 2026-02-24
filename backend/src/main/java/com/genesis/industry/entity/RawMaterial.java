package com.genesis.industry.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Column;
import jakarta.persistence.OneToMany;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
public class RawMaterial extends PanacheEntity {

    @Column(nullable = false)
    public String name;

    @Column(nullable = false)
    public Integer stockQuantity;

    @OneToMany(mappedBy = "rawMaterial")
@JsonIgnore
public List<ProductMaterial> products;
}