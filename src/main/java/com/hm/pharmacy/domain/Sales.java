package com.hm.pharmacy.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A Sales.
 */
@Entity
@Table(name = "sales")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@org.springframework.data.elasticsearch.annotations.Document(indexName = "sales")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Sales implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "sale_date")
    private Instant saleDate;

    @Column(name = "invoice_number")
    @org.springframework.data.elasticsearch.annotations.Field(type = org.springframework.data.elasticsearch.annotations.FieldType.Text)
    private String invoiceNumber;

    @Column(name = "total_amount", precision = 21, scale = 2)
    private BigDecimal totalAmount;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "sales")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @org.springframework.data.annotation.Transient
    @JsonIgnoreProperties(value = { "medicines", "sales" }, allowSetters = true)
    private Set<SaleItems> saleItems = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "sales")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @org.springframework.data.annotation.Transient
    @JsonIgnoreProperties(value = { "sales" }, allowSetters = true)
    private Set<Payments> payments = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = { "prescriptions", "sales" }, allowSetters = true)
    private Customers customers;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Sales id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Instant getSaleDate() {
        return this.saleDate;
    }

    public Sales saleDate(Instant saleDate) {
        this.setSaleDate(saleDate);
        return this;
    }

    public void setSaleDate(Instant saleDate) {
        this.saleDate = saleDate;
    }

    public String getInvoiceNumber() {
        return this.invoiceNumber;
    }

    public Sales invoiceNumber(String invoiceNumber) {
        this.setInvoiceNumber(invoiceNumber);
        return this;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public BigDecimal getTotalAmount() {
        return this.totalAmount;
    }

    public Sales totalAmount(BigDecimal totalAmount) {
        this.setTotalAmount(totalAmount);
        return this;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Set<SaleItems> getSaleItems() {
        return this.saleItems;
    }

    public void setSaleItems(Set<SaleItems> saleItems) {
        if (this.saleItems != null) {
            this.saleItems.forEach(i -> i.setSales(null));
        }
        if (saleItems != null) {
            saleItems.forEach(i -> i.setSales(this));
        }
        this.saleItems = saleItems;
    }

    public Sales saleItems(Set<SaleItems> saleItems) {
        this.setSaleItems(saleItems);
        return this;
    }

    public Sales addSaleItems(SaleItems saleItems) {
        this.saleItems.add(saleItems);
        saleItems.setSales(this);
        return this;
    }

    public Sales removeSaleItems(SaleItems saleItems) {
        this.saleItems.remove(saleItems);
        saleItems.setSales(null);
        return this;
    }

    public Set<Payments> getPayments() {
        return this.payments;
    }

    public void setPayments(Set<Payments> payments) {
        if (this.payments != null) {
            this.payments.forEach(i -> i.setSales(null));
        }
        if (payments != null) {
            payments.forEach(i -> i.setSales(this));
        }
        this.payments = payments;
    }

    public Sales payments(Set<Payments> payments) {
        this.setPayments(payments);
        return this;
    }

    public Sales addPayments(Payments payments) {
        this.payments.add(payments);
        payments.setSales(this);
        return this;
    }

    public Sales removePayments(Payments payments) {
        this.payments.remove(payments);
        payments.setSales(null);
        return this;
    }

    public Customers getCustomers() {
        return this.customers;
    }

    public void setCustomers(Customers customers) {
        this.customers = customers;
    }

    public Sales customers(Customers customers) {
        this.setCustomers(customers);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Sales)) {
            return false;
        }
        return getId() != null && getId().equals(((Sales) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Sales{" +
            "id=" + getId() +
            ", saleDate='" + getSaleDate() + "'" +
            ", invoiceNumber='" + getInvoiceNumber() + "'" +
            ", totalAmount=" + getTotalAmount() +
            "}";
    }
}
