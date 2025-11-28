package com.example.demo.validation;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Size.List;

@Target({FIELD})
@Retention(RUNTIME)
@Constraint(validatedBy = {DateValidator.class })
public @interface DateConstraint {

	String message() default "Invalid date of birth";
	
	int min();

	Class<?>[] groups() default { };

	Class<? extends Payload>[] payload() default { };

}
