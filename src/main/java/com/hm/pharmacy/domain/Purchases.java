package com.hm.pharmacy.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A Purchases.
 */
@Entity
@Table(name = "purchases")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@org.springframework.data.elasticsearch.annotations.Document(indexName = "purchases")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Purchases implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "purchase_date")
    private LocalDate purchaseDate;

    @Column(name = "invoice_number")
    @org.springframework.data.elasticsearch.annotations.Field(type = org.springframework.data.elasticsearch.annotations.FieldType.Text)
    private String invoiceNumber;

    @Column(name = "total_amount", precision = 21, scale = 2)
    private BigDecimal totalAmount;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "purchases")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @org.springframework.data.annotation.Transient
    @JsonIgnoreProperties(value = { "purchases", "medicines" }, allowSetters = true)
    private Set<PurchaseItems> purchaseItems = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "purchases")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @org.springframework.data.annotation.Transient
    @JsonIgnoreProperties(value = { "purchases", "medicines" }, allowSetters = true)
    private Set<MedicineBatches> medicineBatches = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "purchases")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @org.springframework.data.annotation.Transient
    @JsonIgnoreProperties(value = { "suppliers", "purchases" }, allowSetters = true)
    private Set<SupplierPayments> supplierPayments = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = { "purchases", "supplierPayments" }, allowSetters = true)
    private Suppliers suppliers;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Purchases id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getPurchaseDate() {
        return this.purchaseDate;
    }

    public Purchases purchaseDate(LocalDate purchaseDate) {
        this.setPurchaseDate(purchaseDate);
        return this;
    }

    public void setPurchaseDate(LocalDate purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public String getInvoiceNumber() {
        return this.invoiceNumber;
    }

    public Purchases invoiceNumber(String invoiceNumber) {
        this.setInvoiceNumber(invoiceNumber);
        return this;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public BigDecimal getTotalAmount() {
        return this.totalAmount;
    }

    public Purchases totalAmount(BigDecimal totalAmount) {
        this.setTotalAmount(totalAmount);
        return this;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Set<PurchaseItems> getPurchaseItems() {
        return this.purchaseItems;
    }

    public void setPurchaseItems(Set<PurchaseItems> purchaseItems) {
        if (this.purchaseItems != null) {
            this.purchaseItems.forEach(i -> i.setPurchases(null));
        }
        if (purchaseItems != null) {
            purchaseItems.forEach(i -> i.setPurchases(this));
        }
        this.purchaseItems = purchaseItems;
    }

    public Purchases purchaseItems(Set<PurchaseItems> purchaseItems) {
        this.setPurchaseItems(purchaseItems);
        return this;
    }

    public Purchases addPurchaseItems(PurchaseItems purchaseItems) {
        this.purchaseItems.add(purchaseItems);
        purchaseItems.setPurchases(this);
        return this;
    }

    public Purchases removePurchaseItems(PurchaseItems purchaseItems) {
        this.purchaseItems.remove(purchaseItems);
        purchaseItems.setPurchases(null);
        return this;
    }

    public Set<MedicineBatches> getMedicineBatches() {
        return this.medicineBatches;
    }

    public void setMedicineBatches(Set<MedicineBatches> medicineBatches) {
        if (this.medicineBatches != null) {
            this.medicineBatches.forEach(i -> i.setPurchases(null));
        }
        if (medicineBatches != null) {
            medicineBatches.forEach(i -> i.setPurchases(this));
        }
        this.medicineBatches = medicineBatches;
    }

    public Purchases medicineBatches(Set<MedicineBatches> medicineBatches) {
        this.setMedicineBatches(medicineBatches);
        return this;
    }

    public Purchases addMedicineBatches(MedicineBatches medicineBatches) {
        this.medicineBatches.add(medicineBatches);
        medicineBatches.setPurchases(this);
        return this;
    }

    public Purchases removeMedicineBatches(MedicineBatches medicineBatches) {
        this.medicineBatches.remove(medicineBatches);
        medicineBatches.setPurchases(null);
        return this;
    }

    public Set<SupplierPayments> getSupplierPayments() {
        return this.supplierPayments;
    }

    public void setSupplierPayments(Set<SupplierPayments> supplierPayments) {
        if (this.supplierPayments != null) {
            this.supplierPayments.forEach(i -> i.setPurchases(null));
        }
        if (supplierPayments != null) {
            supplierPayments.forEach(i -> i.setPurchases(this));
        }
        this.supplierPayments = supplierPayments;
    }

    public Purchases supplierPayments(Set<SupplierPayments> supplierPayments) {
        this.setSupplierPayments(supplierPayments);
        return this;
    }

    public Purchases addSupplierPayments(SupplierPayments supplierPayments) {
        this.supplierPayments.add(supplierPayments);
        supplierPayments.setPurchases(this);
        return this;
    }

    public Purchases removeSupplierPayments(SupplierPayments supplierPayments) {
        this.supplierPayments.remove(supplierPayments);
        supplierPayments.setPurchases(null);
        return this;
    }

    public Suppliers getSuppliers() {
        return this.suppliers;
    }

    public void setSuppliers(Suppliers suppliers) {
        this.suppliers = suppliers;
    }

    public Purchases suppliers(Suppliers suppliers) {
        this.setSuppliers(suppliers);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Purchases)) {
            return false;
        }
        return getId() != null && getId().equals(((Purchases) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Purchases{" +
            "id=" + getId() +
            ", purchaseDate='" + getPurchaseDate() + "'" +
            ", invoiceNumber='" + getInvoiceNumber() + "'" +
            ", totalAmount=" + getTotalAmount() +
            "}";
    }
}
