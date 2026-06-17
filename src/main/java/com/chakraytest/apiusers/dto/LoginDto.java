package com.chakraytest.apiusers.dto;

import jakarta.validation.constraints.NotBlank;

public class LoginDto {
    @NotBlank
    private String tax_id;

    @NotBlank
    private String password;

    public LoginDto() {
    }

    public LoginDto(@NotBlank String tax_id, @NotBlank String password) {
        this.tax_id = tax_id;
        this.password = password;
    }

    public String getTax_id() {
        return tax_id;
    }

    public void setTax_id(String tax_id) {
        this.tax_id = tax_id;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    
}
