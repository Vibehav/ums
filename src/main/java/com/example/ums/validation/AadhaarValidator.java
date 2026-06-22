package com.example.ums.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class AadhaarValidator implements ConstraintValidator<ValidAadhaar, String> {

    @Override
    public boolean isValid(String aadhaar, ConstraintValidatorContext context) {
        // 1. Do not Allow nulls
        if (aadhaar == null || aadhaar.trim().isEmpty()) {
            return false;
        }

        // 2. Check the strict format (12 digits, doesn't start with 0 or 1)
        if (!aadhaar.matches("^[2-9]{1}[0-9]{11}$")) {
            return false;
        }

        // Verhoeff Checksum Logic goes here ( In Future Implementation )


        return true;
    }
}