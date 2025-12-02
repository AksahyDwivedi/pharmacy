package com.hm.pharmacy.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A Prescriptions.
 */
@Entity
@Table(name = "prescriptions")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@org.springframework.data.elasticsearch.annotations.Document(indexName = "prescriptions")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Prescriptions implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "doctor_name")
    @org.springframework.data.elasticsearch.annotations.Field(type = org.springframework.data.elasticsearch.annotations.FieldType.Text)
    private String doctorName;

    @Column(name = "prescription_date")
    private LocalDate prescriptionDate;

    @Lob
    @Column(name = "notes")
    @org.springframework.data.elasticsearch.annotations.Field(type = org.springframework.data.elasticsearch.annotations.FieldType.Text)
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = { "prescriptions", "sales" }, allowSetters = true)
    private Customers customers;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Prescriptions id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDoctorName() {
        return this.doctorName;
    }

    public Prescriptions doctorName(String doctorName) {
        this.setDoctorName(doctorName);
        return this;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public LocalDate getPrescriptionDate() {
        return this.prescriptionDate;
    }

    public Prescriptions prescriptionDate(LocalDate prescriptionDate) {
        this.setPrescriptionDate(prescriptionDate);
        return this;
    }

    public void setPrescriptionDate(LocalDate prescriptionDate) {
        this.prescriptionDate = prescriptionDate;
    }

    public String getNotes() {
        return this.notes;
    }

    public Prescriptions notes(String notes) {
        this.setNotes(notes);
        return this;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Customers getCustomers() {
        return this.customers;
    }

    public void setCustomers(Customers customers) {
        this.customers = customers;
    }

    public Prescriptions customers(Customers customers) {
        this.setCustomers(customers);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Prescriptions)) {
            return false;
        }
        return getId() != null && getId().equals(((Prescriptions) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Prescriptions{" +
            "id=" + getId() +
            ", doctorName='" + getDoctorName() + "'" +
            ", prescriptionDate='" + getPrescriptionDate() + "'" +
            ", notes='" + getNotes() + "'" +
            "}";
    }
}
