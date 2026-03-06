package com.ecommerce.userservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
public class RegisterRequest {

    @NotBlank(message = "First name is required and cannot be empty or blank")
    @Size(min = 2, max = 100, message = "First name must be between 2 and 100 characters")
    @Pattern(regexp = "^(?=.*\\p{L})[\\p{L}\\s\\-']+$", message = "First name must contain at least one letter and can only include letters, spaces, hyphens, and apostrophes")
    @Schema(example = "Jane")
    private String firstName;

    @NotBlank(message = "Last name is required and cannot be empty or blank")
    @Size(min = 2, max = 100, message = "Last name must be between 2 and 100 characters")
    @Pattern(regexp = "^(?=.*\\p{L})[\\p{L}\\s\\-']+$", message = "Last name must contain at least one letter and can only include letters, spaces, hyphens, and apostrophes")
    @Schema(example = "Smith")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Must be a valid email address")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    @Schema(example = "jane.smith@example.com")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
        message = "Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character"
    )
    @Schema(example = "Password123!")
    private String password;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Must be a valid phone number format (+919876543210)")
    @Schema(example = "+919876543210")
    private String phone;
}
