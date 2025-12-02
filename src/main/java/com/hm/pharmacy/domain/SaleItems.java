package com.hm.pharmacy.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A SaleItems.
 */
@Entity
@Table(name = "sale_items")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@org.springframework.data.elasticsearch.annotations.Document(indexName = "saleitems")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class SaleItems implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "quantity")
    @org.springframework.data.elasticsearch.annotations.Field(type = org.springframework.data.elasticsearch.annotations.FieldType.Integer)
    private Integer quantity;

    @Column(name = "price", precision = 21, scale = 2)
    private BigDecimal price;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = { "purchaseItems", "medicineBatches", "saleItems" }, allowSetters = true)
    private Medicines medicines;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = { "saleItems", "payments", "customers" }, allowSetters = true)
    private Sales sales;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public SaleItems id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getQuantity() {
        return this.quantity;
    }

    public SaleItems quantity(Integer quantity) {
        this.setQuantity(quantity);
        return this;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPrice() {
        return this.price;
    }

    public SaleItems price(BigDecimal price) {
        this.setPrice(price);
        return this;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Medicines getMedicines() {
        return this.medicines;
    }

    public void setMedicines(Medicines medicines) {
        this.medicines = medicines;
    }

    public SaleItems medicines(Medicines medicines) {
        this.setMedicines(medicines);
        return this;
    }

    public Sales getSales() {
        return this.sales;
    }

    public void setSales(Sales sales) {
        this.sales = sales;
    }

    public SaleItems sales(Sales sales) {
        this.setSales(sales);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SaleItems)) {
            return false;
        }
        return getId() != null && getId().equals(((SaleItems) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "SaleItems{" +
            "id=" + getId() +
            ", quantity=" + getQuantity() +
            ", price=" + getPrice() +
            "}";
    }
}
