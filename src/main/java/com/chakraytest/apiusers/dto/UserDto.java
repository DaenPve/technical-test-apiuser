package com.chakraytest.apiusers.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;

public class UserDto {
    
    @Email(message = "Invalid email format")
    @NotBlank
    private String email;

    @NotBlank
    private String name;

    @NotBlank
    @Pattern(regexp = "^(\\+?\\d{1,3})?\\d{10}$",
            message = "Must be 10 digits, format not valid")
            private String phone;

    @NotBlank
    private String password;

    @NotBlank
    @Pattern(regexp = "^[A-Z&Ññ]{3,4}\\d{6}[A-Z0-9]{3}$",
            message = "Tax ID must be have RFC format")
            private String tax_id;

    @Valid
    @NotEmpty
    private List<AddressDto> addresses;
    

    public UserDto() {
    }

    public UserDto(String email, String name, String phone, String password, String tax_id, List<AddressDto> addresses) {
        this.email = email;
        this.name = name;
        this.phone = phone;
        this.password = password;
        this.tax_id = tax_id;
        this.addresses = addresses;
    }


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getTax_id() {
        return tax_id;
    }

    public void setTax_id(String tax_id) {
        this.tax_id = tax_id;
    }

    public List<AddressDto> getAddresses() {
        return addresses;
    }

    public void setAddresses(List<AddressDto> addresses) {
        this.addresses = addresses;
    }


    
    
}
