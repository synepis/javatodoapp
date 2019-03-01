package io.github.synepis.todo.validation;

import java.util.regex.Pattern;

public class Constants {

    public static final Pattern USERNAME_PATTTERN = Pattern.compile("[A-Z0-9_]{3,50}", Pattern.CASE_INSENSITIVE);

    public static final Pattern PASSWORD_PATTTERN = Pattern.compile(".{8,50}");

    public static final Pattern VALID_EMAIL_PATTERN =
            Pattern.compile("^(?=.{3,50}$)[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
}
