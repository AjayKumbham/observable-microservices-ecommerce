package com.ecommerce.userservice.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProfileRequest {

    @Size(min = 2, max = 100, message = "First name must be between 2 and 100 characters")
    @Pattern(regexp = "^(?=.*\\p{L})[\\p{L}\\s\\-']+$", message = "First name must contain at least one letter and can only include letters, spaces, hyphens, and apostrophes")
    private String firstName;

    @Size(min = 2, max = 100, message = "Last name must be between 2 and 100 characters")
    @Pattern(regexp = "^(?=.*\\p{L})[\\p{L}\\s\\-']+$", message = "Last name must contain at least one letter and can only include letters, spaces, hyphens, and apostrophes")
    private String lastName;

    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Must be a valid phone number")
    private String phone;
}
