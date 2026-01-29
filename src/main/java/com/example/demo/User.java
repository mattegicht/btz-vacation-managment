package com.example.demo;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity // This tells Hibernate to make a table out of this class
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Integer id;

    private String name;

    private String email;
    private String password;
    private String role;

    @ManyToOne
    @JoinColumn(name = "assigned_trainer_id")
    @JsonIgnore
    private User assignedTrainer;

    private Double vacationDays = 30.0;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public User getAssignedTrainer() {
        return assignedTrainer;
    }

    public void setAssignedTrainer(User assignedTrainer) {
        this.assignedTrainer = assignedTrainer;
    }

    public Double getVacationDays() {
        return vacationDays != null ? vacationDays : 30.0;
    }

    public void setVacationDays(Double vacationDays) {
        this.vacationDays = vacationDays;
    }
}