package com.chakraytest.apiusers.dto;

import jakarta.validation.constraints.NotBlank;

public class AddressDto {

    @NotBlank
    private String name;
    @NotBlank
    private String street;
    @NotBlank
    private String country_code;
    
    public AddressDto() {
    }

    public AddressDto(String name, String street, String country_code) {

        this.name = name;
        this.street = street;
        this.country_code = country_code;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getCountry_code() {
        return country_code;
    }

    public void setCountry_code(String country_code) {
        this.country_code = country_code;
    }

}
