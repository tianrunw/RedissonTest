package com.tianrun;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Customer {
    private final String firstName;
    private final String lastName;

    @JsonCreator
    public Customer(@JsonProperty("firstName") String firstName,
                    @JsonProperty("lastName") String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    @Override
    public String toString() {
        return "{firstName: " + firstName + ", lastName: " + lastName + "}";
    }
}
