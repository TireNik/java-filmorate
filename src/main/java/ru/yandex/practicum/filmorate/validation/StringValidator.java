package ru.yandex.practicum.filmorate.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public final class StringValidator implements ConstraintValidator<WithoutSpace, String> {

    public static boolean isNullOrEmpty(String stringToValidate) {

        return stringToValidate == null || stringToValidate.isEmpty();
    }

    @Override
    public boolean isValid(String login, ConstraintValidatorContext constraintValidatorContext) {
        return login != null && !login.contains(" ");
    }
}
