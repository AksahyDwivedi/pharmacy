package com.hm.pharmacy.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A Suppliers.
 */
@Entity
@Table(name = "suppliers")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@org.springframework.data.elasticsearch.annotations.Document(indexName = "suppliers")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Suppliers implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    @org.springframework.data.elasticsearch.annotations.Field(type = org.springframework.data.elasticsearch.annotations.FieldType.Text)
    private String name;

    @Column(name = "contact_person")
    @org.springframework.data.elasticsearch.annotations.Field(type = org.springframework.data.elasticsearch.annotations.FieldType.Text)
    private String contactPerson;

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

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "suppliers")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @org.springframework.data.annotation.Transient
    @JsonIgnoreProperties(value = { "purchaseItems", "medicineBatches", "supplierPayments", "suppliers" }, allowSetters = true)
    private Set<Purchases> purchases = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "suppliers")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @org.springframework.data.annotation.Transient
    @JsonIgnoreProperties(value = { "suppliers", "purchases" }, allowSetters = true)
    private Set<SupplierPayments> supplierPayments = new HashSet<>();

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Suppliers id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public Suppliers name(String name) {
        this.setName(name);
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContactPerson() {
        return this.contactPerson;
    }

    public Suppliers contactPerson(String contactPerson) {
        this.setContactPerson(contactPerson);
        return this;
    }

    public void setContactPerson(String contactPerson) {
        this.contactPerson = contactPerson;
    }

    public String getPhone() {
        return this.phone;
    }

    public Suppliers phone(String phone) {
        this.setPhone(phone);
        return this;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return this.email;
    }

    public Suppliers email(String email) {
        this.setEmail(email);
        return this;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return this.address;
    }

    public Suppliers address(String address) {
        this.setAddress(address);
        return this;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Set<Purchases> getPurchases() {
        return this.purchases;
    }

    public void setPurchases(Set<Purchases> purchases) {
        if (this.purchases != null) {
            this.purchases.forEach(i -> i.setSuppliers(null));
        }
        if (purchases != null) {
            purchases.forEach(i -> i.setSuppliers(this));
        }
        this.purchases = purchases;
    }

    public Suppliers purchases(Set<Purchases> purchases) {
        this.setPurchases(purchases);
        return this;
    }

    public Suppliers addPurchases(Purchases purchases) {
        this.purchases.add(purchases);
        purchases.setSuppliers(this);
        return this;
    }

    public Suppliers removePurchases(Purchases purchases) {
        this.purchases.remove(purchases);
        purchases.setSuppliers(null);
        return this;
    }

    public Set<SupplierPayments> getSupplierPayments() {
        return this.supplierPayments;
    }

    public void setSupplierPayments(Set<SupplierPayments> supplierPayments) {
        if (this.supplierPayments != null) {
            this.supplierPayments.forEach(i -> i.setSuppliers(null));
        }
        if (supplierPayments != null) {
            supplierPayments.forEach(i -> i.setSuppliers(this));
        }
        this.supplierPayments = supplierPayments;
    }

    public Suppliers supplierPayments(Set<SupplierPayments> supplierPayments) {
        this.setSupplierPayments(supplierPayments);
        return this;
    }

    public Suppliers addSupplierPayments(SupplierPayments supplierPayments) {
        this.supplierPayments.add(supplierPayments);
        supplierPayments.setSuppliers(this);
        return this;
    }

    public Suppliers removeSupplierPayments(SupplierPayments supplierPayments) {
        this.supplierPayments.remove(supplierPayments);
        supplierPayments.setSuppliers(null);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Suppliers)) {
            return false;
        }
        return getId() != null && getId().equals(((Suppliers) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Suppliers{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            ", contactPerson='" + getContactPerson() + "'" +
            ", phone='" + getPhone() + "'" +
            ", email='" + getEmail() + "'" +
            ", address='" + getAddress() + "'" +
            "}";
    }
}
