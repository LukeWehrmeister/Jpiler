package com.github.lukewehrmeister.jpiler;

public enum SemanticType {
    CLASS,
    INT,
    BOOLEAN,
    CHAR,
    STRING,
    VOID,
    NULL,
    UNKNOWN; 

    public static SemanticType fromString(String text) {
        return switch (text) {
            case "class" -> CLASS;
            case "int" -> INT;
            case "boolean" -> BOOLEAN;
            case "char" -> CHAR;
            case "String" -> STRING;
            case "void" -> VOID;
            case "null" -> NULL;
            case "unknown" -> UNKNOWN;
            default -> throw new IllegalArgumentException("Unknown type: " + text);
        };
    }
}
