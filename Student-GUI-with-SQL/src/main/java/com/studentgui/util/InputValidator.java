package com.studentgui.util;

import java.util.regex.Pattern;

import java.util.List;
import java.util.ArrayList;

public class InputValidator {

    private static final String[] SQL_INJECTION_PATTERNS = {
            "(\\b(SELECT|INSERT|UPDATE|DELETE|DROP|TRUNCATE|ALTER|CREATE|EXEC|EXECUTE|UNION|GRANT|REVOKE)\\b)",
            "(-{2,})", // SQL comments
            "(;)", // Statement terminator
            "(/\\*|\\*/)", // Block comments
            "(\\bOR\\b.*=.*)", // OR 1=1
            "(\\bAND\\b.*=.*)", // AND 1=1
            "('.*--)", // Quote followed by comment
            "(\\bxp_\\w+)", // Extended stored procedures
            "(\\bsp_\\w+)" // Stored procedures
    };

    private static final List<Pattern> COMPILED_PATTERNS = new ArrayList<>();

    static {
        for (String regex : SQL_INJECTION_PATTERNS) {
            COMPILED_PATTERNS.add(Pattern.compile(regex, Pattern.CASE_INSENSITIVE));
        }
    }

    public static class ValidationResult {
        public final boolean isValid;
        public final String errorMessage;
        public final Object parsedValue;

        public ValidationResult(boolean isValid, String errorMessage) {
            this(isValid, errorMessage, null);
        }

        public ValidationResult(boolean isValid, String errorMessage, Object parsedValue) {
            this.isValid = isValid;
            this.errorMessage = errorMessage;
            this.parsedValue = parsedValue;
        }
    }

    public static boolean containsSqlInjection(String value) {
        if (value == null || value.isEmpty())
            return false;
        for (Pattern pattern : COMPILED_PATTERNS) {
            if (pattern.matcher(value).find()) {
                return true;
            }
        }
        return false;
    }

    public static ValidationResult validateName(String name) {
        if (name == null || name.trim().isEmpty())
            return new ValidationResult(false, "Name cannot be empty");
        name = name.trim();

        if (name.length() < 2)
            return new ValidationResult(false, "Name must be at least 2 characters long");
        if (name.length() > 100)
            return new ValidationResult(false, "Name cannot exceed 100 characters");

        if (containsSqlInjection(name))
            return new ValidationResult(false, "Invalid characters detected in name");

        if (!name.matches("^[a-zA-Z\\s]+$"))
            return new ValidationResult(false, "Name can only contain letters and spaces");

        return new ValidationResult(true, "", name);
    }

    public static ValidationResult validateRollNumber(String rollNo) {
        if (rollNo == null || rollNo.trim().isEmpty())
            return new ValidationResult(false, "Roll Number cannot be empty");
        rollNo = rollNo.trim();

        if (containsSqlInjection(rollNo))
            return new ValidationResult(false, "Invalid characters detected in Roll Number");

        try {
            int val = Integer.parseInt(rollNo);
            if (val <= 0)
                return new ValidationResult(false, "Roll Number must be a positive number");
            if (val > 999999)
                return new ValidationResult(false, "Roll Number cannot exceed 999999");
            return new ValidationResult(true, "", val);
        } catch (NumberFormatException e) {
            return new ValidationResult(false, "Roll Number must be a valid integer");
        }
    }

    public static ValidationResult validateMarks(String marks, String subjectName) {
        if (marks == null || marks.trim().isEmpty())
            return new ValidationResult(true, "", null);
        marks = marks.trim();

        if (containsSqlInjection(marks))
            return new ValidationResult(false, "Invalid characters detected in " + subjectName + " marks");

        try {
            int val = Integer.parseInt(marks);
            if (val < 0)
                return new ValidationResult(false, "Marks for " + subjectName + " cannot be negative");
            if (val > 100)
                return new ValidationResult(false, "Marks for " + subjectName + " cannot exceed 100");
            return new ValidationResult(true, "", val);
        } catch (NumberFormatException e) {
            return new ValidationResult(false, "Marks for " + subjectName + " must be a valid integer");
        }
    }

    public static ValidationResult validateSearchTerm(String searchTerm) {
        if (searchTerm == null || searchTerm.isEmpty())
            return new ValidationResult(true, "");
        if (containsSqlInjection(searchTerm))
            return new ValidationResult(false, "Invalid characters detected in search term");
        if (searchTerm.length() > 100)
            return new ValidationResult(false, "Search term is too long");
        return new ValidationResult(true, "", searchTerm);
    }

    public static String sanitizeString(String value) {
        if (value == null)
            return "";
        value = value.replace("\0", ""); // Remove null bytes
        value = value.trim();
        // HTML escaping is not strictly necessary for Swing unless rendering HTML,
        // but good for consistency. We'll skip complex HTML escaping here for
        // simplicity
        // as Swing labels handle plain text well, but let's do basic if needed.
        // For now, just trim and null byte removal is good for SQL/Display.
        return value;
    }
}
