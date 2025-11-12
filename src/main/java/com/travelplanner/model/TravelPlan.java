package com.travelplanner.model;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "travel_plans")
public class TravelPlan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "username", nullable = false)
    private String username;
    
    @Column(name = "travel_plan", columnDefinition = "TEXT", nullable = false)
    private String travelPlan;
    
    @Column(name = "preferences", columnDefinition = "TEXT")
    private String preferences;
    
    @Column(name = "expenses", columnDefinition = "TEXT")
    private String expenses;
    
    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = new Date();
    }
    
    // Constructors
    public TravelPlan() {}
    
    public TravelPlan(String username, String travelPlan) {
        this.username = username;
        this.travelPlan = travelPlan;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getTravelPlan() {
        return travelPlan;
    }
    
    public void setTravelPlan(String travelPlan) {
        this.travelPlan = travelPlan;
    }
    
    public String getPreferences() {
        return preferences;
    }
    
    public void setPreferences(String preferences) {
        this.preferences = preferences;
    }
    
    public String getExpenses() {
        return expenses;
    }
    
    public void setExpenses(String expenses) {
        this.expenses = expenses;
    }
    
    public Date getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}