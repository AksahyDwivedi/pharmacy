package com.hm.pharmacy.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A MedicineBatches.
 */
@Entity
@Table(name = "medicine_batches")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@org.springframework.data.elasticsearch.annotations.Document(indexName = "medicinebatches")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class MedicineBatches implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "batch_number")
    @org.springframework.data.elasticsearch.annotations.Field(type = org.springframework.data.elasticsearch.annotations.FieldType.Text)
    private String batchNumber;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "quantity")
    @org.springframework.data.elasticsearch.annotations.Field(type = org.springframework.data.elasticsearch.annotations.FieldType.Integer)
    private Integer quantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = { "purchaseItems", "medicineBatches", "supplierPayments", "suppliers" }, allowSetters = true)
    private Purchases purchases;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = { "purchaseItems", "medicineBatches", "saleItems" }, allowSetters = true)
    private Medicines medicines;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public MedicineBatches id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBatchNumber() {
        return this.batchNumber;
    }

    public MedicineBatches batchNumber(String batchNumber) {
        this.setBatchNumber(batchNumber);
        return this;
    }

    public void setBatchNumber(String batchNumber) {
        this.batchNumber = batchNumber;
    }

    public LocalDate getExpiryDate() {
        return this.expiryDate;
    }

    public MedicineBatches expiryDate(LocalDate expiryDate) {
        this.setExpiryDate(expiryDate);
        return this;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public Integer getQuantity() {
        return this.quantity;
    }

    public MedicineBatches quantity(Integer quantity) {
        this.setQuantity(quantity);
        return this;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Purchases getPurchases() {
        return this.purchases;
    }

    public void setPurchases(Purchases purchases) {
        this.purchases = purchases;
    }

    public MedicineBatches purchases(Purchases purchases) {
        this.setPurchases(purchases);
        return this;
    }

    public Medicines getMedicines() {
        return this.medicines;
    }

    public void setMedicines(Medicines medicines) {
        this.medicines = medicines;
    }

    public MedicineBatches medicines(Medicines medicines) {
        this.setMedicines(medicines);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MedicineBatches)) {
            return false;
        }
        return getId() != null && getId().equals(((MedicineBatches) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "MedicineBatches{" +
            "id=" + getId() +
            ", batchNumber='" + getBatchNumber() + "'" +
            ", expiryDate='" + getExpiryDate() + "'" +
            ", quantity=" + getQuantity() +
            "}";
    }
}
