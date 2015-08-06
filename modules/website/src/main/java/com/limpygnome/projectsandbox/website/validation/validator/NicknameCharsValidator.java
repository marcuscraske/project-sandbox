package com.limpygnome.projectsandbox.website.validation.validator;

import com.limpygnome.projectsandbox.website.validation.annotation.NicknameChars;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

/**
 * Created by limpygnome on 23/07/15.
 */
public class NicknameCharsValidator implements ConstraintValidator<NicknameChars, String>
{
    private static final Pattern NICKNAME_REGEX_PATTERN = Pattern.compile("^([a-zA-Z0-9\\_\\-\\$]+)$");

    @Override
    public void initialize(NicknameChars username) { }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext)
    {
        // Check chars
        if (value == null || !NICKNAME_REGEX_PATTERN.matcher(value).matches())
        {
            return false;
        }

        return true;
    }
}
