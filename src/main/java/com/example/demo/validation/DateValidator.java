package com.example.demo.validation;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Objects;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class DateValidator implements ConstraintValidator<DateConstraint, Date> {

	private int min;
	
	@Override
	public boolean isValid(Date value, ConstraintValidatorContext context) {
		if(Objects.isNull(value)) return true;
		
		LocalDate inputDate = value.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		LocalDate currentDate = LocalDate.now();
		
		long years = ChronoUnit.YEARS.between(inputDate, currentDate);
		
		return years >= min;
	}
	
	@Override
	public void initialize(DateConstraint constraintAnnotation) {
		// TODO Auto-generated method stub
		ConstraintValidator.super.initialize(constraintAnnotation);
		min = constraintAnnotation.min();
	}
}
