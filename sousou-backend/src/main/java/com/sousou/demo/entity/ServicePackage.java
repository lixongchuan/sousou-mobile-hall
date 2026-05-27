package com.sousou.demo.entity;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "packageType")
@JsonSubTypes({
    @JsonSubTypes.Type(value = TalkativePackage.class, name = "TALK"),
    @JsonSubTypes.Type(value = NetizenPackage.class, name = "NET"),
    @JsonSubTypes.Type(value = SupermanPackage.class, name = "SUPER") 
})
public abstract class ServicePackage {
    protected double price;

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    
    public abstract void showInfo(); 
}