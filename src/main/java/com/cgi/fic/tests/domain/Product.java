package com.cgi.fic.tests.domain;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import javax.json.bind.annotation.JsonbTransient;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.runtime.annotations.RegisterForReflection;

import javax.persistence.*;
import javax.validation.constraints.*;

import java.io.Serializable;

/**
 * A Product.
 */
@Entity
@Table(name = "product")
@RegisterForReflection
public class Product extends PanacheEntityBase implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    public Long id;

    @NotNull
    @Column(name = "product_name", nullable = false)
    public String productName;

    @NotNull
    @Column(name = "unit_price", nullable = false)
    public Float unitPrice;

    @Column(name = "ean_13_bar_code")
    public String ean13BarCode;

    @Column(name = "brand")
    public String brand;

    @Column(name = "categories")
    public String categories;

    @Column(name = "image_url")
    public String imageUrl;

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Product)) {
            return false;
        }
        return id != null && id.equals(((Product) o).id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    @Override
    public String toString() {
        return "Product{" +
            "id=" + id +
            ", productName='" + productName + "'" +
            ", unitPrice=" + unitPrice +
            ", ean13BarCode='" + ean13BarCode + "'" +
            ", brand='" + brand + "'" +
            ", categories='" + categories + "'" +
            ", imageUrl='" + imageUrl + "'" +
            "}";
    }

    public Product update() {
        return update(this);
    }

    public Product persistOrUpdate() {
        return persistOrUpdate(this);
    }

    public static Product update(Product product) {
        if (product == null) {
            throw new IllegalArgumentException("product can't be null");
        }
        var entity = Product.<Product>findById(product.id);
        if (entity != null) {
            entity.productName = product.productName;
            entity.unitPrice = product.unitPrice;
            entity.ean13BarCode = product.ean13BarCode;
            entity.brand = product.brand;
            entity.categories = product.categories;
            entity.imageUrl = product.imageUrl;
        }
        return entity;
    }

    public static Product persistOrUpdate(Product product) {
        if (product == null) {
            throw new IllegalArgumentException("product can't be null");
        }
        if (product.id == null) {
            persist(product);
            return product;
        } else {
            return update(product);
        }
    }


}
