"""
Input Validator Module
Provides comprehensive input validation to prevent SQL injection and ensure data integrity.
"""

import re
from typing import Tuple, Optional


# Suspicious SQL patterns that might indicate injection attempts
SQL_INJECTION_PATTERNS = [
    r"(\b(SELECT|INSERT|UPDATE|DELETE|DROP|TRUNCATE|ALTER|CREATE|EXEC|EXECUTE|UNION|GRANT|REVOKE)\b)",
    r"(-{2,})",  # SQL comments
    r"(;)",      # Statement terminator
    r"(/\*|\*/)", # Block comments
    r"(\bOR\b.*=.*)",  # OR 1=1 style attacks
    r"(\bAND\b.*=.*)", # AND 1=1 style attacks
    r"('.*--)",  # Quote followed by comment
    r"(\bxp_\w+)", # Extended stored procedures
    r"(\bsp_\w+)", # Stored procedures
]

# Compile patterns for performance
COMPILED_PATTERNS = [re.compile(pattern, re.IGNORECASE) for pattern in SQL_INJECTION_PATTERNS]


class ValidationError(Exception):
    """Custom exception for validation errors"""
    pass


def contains_sql_injection(value: str) -> bool:
    """
    Check if a string contains potential SQL injection patterns.
    
    Args:
        value: The string to check
        
    Returns:
        True if suspicious patterns are found, False otherwise
    """
    if not value:
        return False
        
    for pattern in COMPILED_PATTERNS:
        if pattern.search(value):
            return True
    return False


def validate_name(name: str) -> Tuple[bool, str]:
    """
    Validate a student name.
    
    Rules:
    - Must not be empty
    - Must only contain letters, spaces, hyphens, and apostrophes
    - Must be between 2 and 100 characters
    - Must not contain SQL injection patterns
    
    Args:
        name: The name to validate
        
    Returns:
        Tuple of (is_valid, error_message)
    """
    if not name or not name.strip():
        return False, "Name cannot be empty"
    
    name = name.strip()
    
    if len(name) < 2:
        return False, "Name must be at least 2 characters long"
    
    if len(name) > 100:
        return False, "Name cannot exceed 100 characters"
    
    # Check for SQL injection patterns
    if contains_sql_injection(name):
        return False, "Invalid characters detected in name"
    
    # Allow letters (including Unicode), spaces, hyphens, apostrophes, and periods
    if not re.match(r"^[\w\s\'\-\.]+$", name, re.UNICODE):
        return False, "Name can only contain letters, spaces, hyphens, apostrophes, and periods"
    
    return True, ""


def validate_roll_number(roll_no: str) -> Tuple[bool, str, Optional[int]]:
    """
    Validate a roll number.
    
    Rules:
    - Must not be empty
    - Must be a positive integer
    - Must be within reasonable range (1 to 999999)
    
    Args:
        roll_no: The roll number to validate (as string from input)
        
    Returns:
        Tuple of (is_valid, error_message, parsed_value)
    """
    if not roll_no or not roll_no.strip():
        return False, "Roll Number cannot be empty", None
    
    roll_no = roll_no.strip()
    
    # Check for SQL injection patterns
    if contains_sql_injection(roll_no):
        return False, "Invalid characters detected in Roll Number", None
    
    try:
        value = int(roll_no)
    except ValueError:
        return False, "Roll Number must be a valid integer", None
    
    if value <= 0:
        return False, "Roll Number must be a positive number", None
    
    if value > 999999:
        return False, "Roll Number cannot exceed 999999", None
    
    return True, "", value


def validate_marks(marks: str, subject_name: str = "Subject") -> Tuple[bool, str, Optional[int]]:
    """
    Validate marks for a subject.
    
    Rules:
    - Can be empty (optional)
    - If provided, must be integer between 0 and 100
    
    Args:
        marks: The marks value to validate (as string from input)
        subject_name: Name of the subject for error messages
        
    Returns:
        Tuple of (is_valid, error_message, parsed_value)
    """
    if not marks or not marks.strip():
        return True, "", None  # Empty marks are allowed
    
    marks = marks.strip()
    
    # Check for SQL injection patterns
    if contains_sql_injection(marks):
        return False, f"Invalid characters detected in {subject_name} marks", None
    
    try:
        value = int(marks)
    except ValueError:
        return False, f"Marks for {subject_name} must be a valid integer", None
    
    if value < 0:
        return False, f"Marks for {subject_name} cannot be negative", None
    
    if value > 100:
        return False, f"Marks for {subject_name} cannot exceed 100", None
    
    return True, "", value


def sanitize_string(value: str) -> str:
    """
    Sanitize a string by removing potentially dangerous characters.
    This is an additional layer of defense, not a replacement for parameterized queries.
    
    Args:
        value: The string to sanitize
        
    Returns:
        Sanitized string
    """
    if not value:
        return ""
    
    # Remove null bytes
    value = value.replace('\x00', '')
    
    # Strip leading/trailing whitespace
    value = value.strip()
    
    return value


def validate_student_data(name: str, roll_no: str, marks_dict: dict) -> Tuple[bool, str, dict]:
    """
    Validate all student data at once.
    
    Args:
        name: Student name
        roll_no: Roll number as string
        marks_dict: Dictionary of subject -> marks (as strings)
        
    Returns:
        Tuple of (is_valid, error_message, validated_data_dict)
    """
    validated_data = {}
    
    # Validate name
    is_valid, error_msg = validate_name(name)
    if not is_valid:
        return False, error_msg, {}
    validated_data['name'] = sanitize_string(name)
    
    # Validate roll number
    is_valid, error_msg, roll_value = validate_roll_number(roll_no)
    if not is_valid:
        return False, error_msg, {}
    validated_data['roll_no'] = roll_value
    
    # Validate marks
    validated_marks = {}
    for subject, marks in marks_dict.items():
        is_valid, error_msg, marks_value = validate_marks(marks, subject)
        if not is_valid:
            return False, error_msg, {}
        if marks_value is not None:
            validated_marks[subject] = marks_value
    
    validated_data['marks'] = validated_marks
    
    return True, "", validated_data


def validate_search_term(search_term: str) -> Tuple[bool, str]:
    """
    Validate a search term.
    
    Args:
        search_term: The search term to validate
        
    Returns:
        Tuple of (is_valid, error_message)
    """
    if not search_term:
        return True, ""  # Empty search is allowed
    
    # Check for SQL injection patterns
    if contains_sql_injection(search_term):
        return False, "Invalid characters detected in search term"
    
    if len(search_term) > 100:
        return False, "Search term is too long"
    
    return True, ""
