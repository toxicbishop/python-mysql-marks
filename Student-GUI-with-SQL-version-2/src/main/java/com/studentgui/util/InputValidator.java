package com.studentgui.util;

import org.apache.commons.text.StringEscapeUtils;
import java.util.List;
import java.util.regex.Pattern;

/**
 * InputValidator v2 — enhanced with SQL injection detection and HTML
 * sanitization.
 * Mirrors Python v2's input_validator.py logic.
 */
public class InputValidator {

    // ── SQL injection patterns (ported from Python v2) ──────────────────────────
    private static final List<Pattern> SQL_INJECTION_PATTERNS = List.of(
            Pattern.compile(
                    "(\\b(SELECT|INSERT|UPDATE|DELETE|DROP|TRUNCATE|ALTER|CREATE|EXEC|EXECUTE|UNION|GRANT|REVOKE)\\b)",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("(-{2,})"), // SQL line comments
            Pattern.compile("(;)"), // Statement terminator
            Pattern.compile("(/\\*|\\*/)"), // Block comments
            Pattern.compile("(\\bOR\\b.*=.*)", Pattern.CASE_INSENSITIVE), // OR 1=1
            Pattern.compile("(\\bAND\\b.*=.*)", Pattern.CASE_INSENSITIVE), // AND 1=1
            Pattern.compile("('.*--)"), // Quote + comment
            Pattern.compile("(\\bxp_\\w+)", Pattern.CASE_INSENSITIVE), // Extended stored procs
            Pattern.compile("(\\bsp_\\w+)", Pattern.CASE_INSENSITIVE) // Stored procs
    );

    public record ValidationResult(boolean isValid, String errorMessage, Object parsedValue) {
        public static ValidationResult ok(Object value) {
            return new ValidationResult(true, "", value);
        }

        public static ValidationResult err(String msg) {
            return new ValidationResult(false, msg, null);
        }
    }

    // ── SQL injection check ──────────────────────────────────────────────────────
    public static boolean containsSqlInjection(String value) {
        if (value == null || value.isEmpty())
            return false;
        for (Pattern p : SQL_INJECTION_PATTERNS) {
            if (p.matcher(value).find())
                return true;
        }
        return false;
    }

    // ── Sanitize: strip null bytes, trim, HTML-escape ────────────────────────────
    public static String sanitize(String value) {
        if (value == null)
            return "";
        value = value.replace("\0", "").strip();
        return StringEscapeUtils.escapeHtml4(value);
    }

    // ── Name validation ──────────────────────────────────────────────────────────
    public static ValidationResult validateName(String name) {
        if (name == null || name.isBlank())
            return ValidationResult.err("Name cannot be empty");
        name = name.strip();
        if (name.length() < 2)
            return ValidationResult.err("Name must be at least 2 characters");
        if (name.length() > 100)
            return ValidationResult.err("Name cannot exceed 100 characters");
        if (containsSqlInjection(name))
            return ValidationResult.err("Invalid characters detected in name");
        if (!name.matches("^[a-zA-Z\\s]+$"))
            return ValidationResult.err("Name can only contain letters and spaces");
        return ValidationResult.ok(sanitize(name));
    }

    // ── Roll number validation ───────────────────────────────────────────────────
    public static ValidationResult validateRollNumber(String roll) {
        if (roll == null || roll.isBlank())
            return ValidationResult.err("Roll Number cannot be empty");
        roll = roll.strip();
        if (containsSqlInjection(roll))
            return ValidationResult.err("Invalid characters in Roll Number");
        try {
            int value = Integer.parseInt(roll);
            if (value <= 0)
                return ValidationResult.err("Roll Number must be positive");
            if (value > 999999)
                return ValidationResult.err("Roll Number cannot exceed 999999");
            return ValidationResult.ok(value);
        } catch (NumberFormatException e) {
            return ValidationResult.err("Roll Number must be a valid integer");
        }
    }

    // ── Marks validation ─────────────────────────────────────────────────────────
    public static ValidationResult validateMarks(String marks, String subjectName) {
        if (marks == null || marks.isBlank())
            return ValidationResult.ok(null); // optional
        marks = marks.strip();
        if (containsSqlInjection(marks))
            return ValidationResult.err("Invalid characters in " + subjectName + " marks");
        try {
            int value = Integer.parseInt(marks);
            if (value < 0)
                return ValidationResult.err(subjectName + " marks cannot be negative");
            if (value > 100)
                return ValidationResult.err(subjectName + " marks cannot exceed 100");
            return ValidationResult.ok(value);
        } catch (NumberFormatException e) {
            return ValidationResult.err("Marks for " + subjectName + " must be a valid integer");
        }
    }

    // ── Search term validation ───────────────────────────────────────────────────
    public static ValidationResult validateSearchTerm(String term) {
        if (term == null || term.isBlank())
            return ValidationResult.ok(null);
        if (containsSqlInjection(term))
            return ValidationResult.err("Invalid characters in search term");
        if (term.length() > 100)
            return ValidationResult.err("Search term is too long");
        return ValidationResult.ok(term);
    }
}
