package com.hm.pharmacy.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A Customers.
 */
@Entity
@Table(name = "customers")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@org.springframework.data.elasticsearch.annotations.Document(indexName = "customers")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Customers implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    @org.springframework.data.elasticsearch.annotations.Field(type = org.springframework.data.elasticsearch.annotations.FieldType.Text)
    private String name;

    @Column(name = "phone")
    @org.springframework.data.elasticsearch.annotations.Field(type = org.springframework.data.elasticsearch.annotations.FieldType.Text)
    private String phone;

    @Column(name = "email")
    @org.springframework.data.elasticsearch.annotations.Field(type = org.springframework.data.elasticsearch.annotations.FieldType.Text)
    private String email;

    @Lob
    @Column(name = "address")
    @org.springframework.data.elasticsearch.annotations.Field(type = org.springframework.data.elasticsearch.annotations.FieldType.Text)
    private String address;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "customers")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @org.springframework.data.annotation.Transient
    @JsonIgnoreProperties(value = { "customers" }, allowSetters = true)
    private Set<Prescriptions> prescriptions = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "customers")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @org.springframework.data.annotation.Transient
    @JsonIgnoreProperties(value = { "saleItems", "payments", "customers" }, allowSetters = true)
    private Set<Sales> sales = new HashSet<>();

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Customers id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public Customers name(String name) {
        this.setName(name);
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return this.phone;
    }

    public Customers phone(String phone) {
        this.setPhone(phone);
        return this;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return this.email;
    }

    public Customers email(String email) {
        this.setEmail(email);
        return this;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return this.address;
    }

    public Customers address(String address) {
        this.setAddress(address);
        return this;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Set<Prescriptions> getPrescriptions() {
        return this.prescriptions;
    }

    public void setPrescriptions(Set<Prescriptions> prescriptions) {
        if (this.prescriptions != null) {
            this.prescriptions.forEach(i -> i.setCustomers(null));
        }
        if (prescriptions != null) {
            prescriptions.forEach(i -> i.setCustomers(this));
        }
        this.prescriptions = prescriptions;
    }

    public Customers prescriptions(Set<Prescriptions> prescriptions) {
        this.setPrescriptions(prescriptions);
        return this;
    }

    public Customers addPrescriptions(Prescriptions prescriptions) {
        this.prescriptions.add(prescriptions);
        prescriptions.setCustomers(this);
        return this;
    }

    public Customers removePrescriptions(Prescriptions prescriptions) {
        this.prescriptions.remove(prescriptions);
        prescriptions.setCustomers(null);
        return this;
    }

    public Set<Sales> getSales() {
        return this.sales;
    }

    public void setSales(Set<Sales> sales) {
        if (this.sales != null) {
            this.sales.forEach(i -> i.setCustomers(null));
        }
        if (sales != null) {
            sales.forEach(i -> i.setCustomers(this));
        }
        this.sales = sales;
    }

    public Customers sales(Set<Sales> sales) {
        this.setSales(sales);
        return this;
    }

    public Customers addSales(Sales sales) {
        this.sales.add(sales);
        sales.setCustomers(this);
        return this;
    }

    public Customers removeSales(Sales sales) {
        this.sales.remove(sales);
        sales.setCustomers(null);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Customers)) {
            return false;
        }
        return getId() != null && getId().equals(((Customers) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Customers{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            ", phone='" + getPhone() + "'" +
            ", email='" + getEmail() + "'" +
            ", address='" + getAddress() + "'" +
            "}";
    }
}
