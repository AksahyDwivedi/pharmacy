package com.hm.pharmacy.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A Medicines.
 */
@Entity
@Table(name = "medicines")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@org.springframework.data.elasticsearch.annotations.Document(indexName = "medicines")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Medicines implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    @org.springframework.data.elasticsearch.annotations.Field(type = org.springframework.data.elasticsearch.annotations.FieldType.Text)
    private String name;

    @Column(name = "manufacturer")
    @org.springframework.data.elasticsearch.annotations.Field(type = org.springframework.data.elasticsearch.annotations.FieldType.Text)
    private String manufacturer;

    @Column(name = "category")
    @org.springframework.data.elasticsearch.annotations.Field(type = org.springframework.data.elasticsearch.annotations.FieldType.Text)
    private String category;

    @Column(name = "price", precision = 21, scale = 2)
    private BigDecimal price;

    @Column(name = "stock")
    @org.springframework.data.elasticsearch.annotations.Field(type = org.springframework.data.elasticsearch.annotations.FieldType.Integer)
    private Integer stock;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "medicines")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @org.springframework.data.annotation.Transient
    @JsonIgnoreProperties(value = { "purchases", "medicines" }, allowSetters = true)
    private Set<PurchaseItems> purchaseItems = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "medicines")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @org.springframework.data.annotation.Transient
    @JsonIgnoreProperties(value = { "purchases", "medicines" }, allowSetters = true)
    private Set<MedicineBatches> medicineBatches = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "medicines")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @org.springframework.data.annotation.Transient
    @JsonIgnoreProperties(value = { "medicines", "sales" }, allowSetters = true)
    private Set<SaleItems> saleItems = new HashSet<>();

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Medicines id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public Medicines name(String name) {
        this.setName(name);
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getManufacturer() {
        return this.manufacturer;
    }

    public Medicines manufacturer(String manufacturer) {
        this.setManufacturer(manufacturer);
        return this;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getCategory() {
        return this.category;
    }

    public Medicines category(String category) {
        this.setCategory(category);
        return this;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public BigDecimal getPrice() {
        return this.price;
    }

    public Medicines price(BigDecimal price) {
        this.setPrice(price);
        return this;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getStock() {
        return this.stock;
    }

    public Medicines stock(Integer stock) {
        this.setStock(stock);
        return this;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public Set<PurchaseItems> getPurchaseItems() {
        return this.purchaseItems;
    }

    public void setPurchaseItems(Set<PurchaseItems> purchaseItems) {
        if (this.purchaseItems != null) {
            this.purchaseItems.forEach(i -> i.setMedicines(null));
        }
        if (purchaseItems != null) {
            purchaseItems.forEach(i -> i.setMedicines(this));
        }
        this.purchaseItems = purchaseItems;
    }

    public Medicines purchaseItems(Set<PurchaseItems> purchaseItems) {
        this.setPurchaseItems(purchaseItems);
        return this;
    }

    public Medicines addPurchaseItems(PurchaseItems purchaseItems) {
        this.purchaseItems.add(purchaseItems);
        purchaseItems.setMedicines(this);
        return this;
    }

    public Medicines removePurchaseItems(PurchaseItems purchaseItems) {
        this.purchaseItems.remove(purchaseItems);
        purchaseItems.setMedicines(null);
        return this;
    }

    public Set<MedicineBatches> getMedicineBatches() {
        return this.medicineBatches;
    }

    public void setMedicineBatches(Set<MedicineBatches> medicineBatches) {
        if (this.medicineBatches != null) {
            this.medicineBatches.forEach(i -> i.setMedicines(null));
        }
        if (medicineBatches != null) {
            medicineBatches.forEach(i -> i.setMedicines(this));
        }
        this.medicineBatches = medicineBatches;
    }

    public Medicines medicineBatches(Set<MedicineBatches> medicineBatches) {
        this.setMedicineBatches(medicineBatches);
        return this;
    }

    public Medicines addMedicineBatches(MedicineBatches medicineBatches) {
        this.medicineBatches.add(medicineBatches);
        medicineBatches.setMedicines(this);
        return this;
    }

    public Medicines removeMedicineBatches(MedicineBatches medicineBatches) {
        this.medicineBatches.remove(medicineBatches);
        medicineBatches.setMedicines(null);
        return this;
    }

    public Set<SaleItems> getSaleItems() {
        return this.saleItems;
    }

    public void setSaleItems(Set<SaleItems> saleItems) {
        if (this.saleItems != null) {
            this.saleItems.forEach(i -> i.setMedicines(null));
        }
        if (saleItems != null) {
            saleItems.forEach(i -> i.setMedicines(this));
        }
        this.saleItems = saleItems;
    }

    public Medicines saleItems(Set<SaleItems> saleItems) {
        this.setSaleItems(saleItems);
        return this;
    }

    public Medicines addSaleItems(SaleItems saleItems) {
        this.saleItems.add(saleItems);
        saleItems.setMedicines(this);
        return this;
    }

    public Medicines removeSaleItems(SaleItems saleItems) {
        this.saleItems.remove(saleItems);
        saleItems.setMedicines(null);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Medicines)) {
            return false;
        }
        return getId() != null && getId().equals(((Medicines) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Medicines{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            ", manufacturer='" + getManufacturer() + "'" +
            ", category='" + getCategory() + "'" +
            ", price=" + getPrice() +
            ", stock=" + getStock() +
            "}";
    }
}
