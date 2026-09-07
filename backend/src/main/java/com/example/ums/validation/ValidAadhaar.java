package com.example.ums.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = AadhaarValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidAadhaar { // Custom Annotation
    String message() default "Invalid Aadhaar number format or checksum";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}