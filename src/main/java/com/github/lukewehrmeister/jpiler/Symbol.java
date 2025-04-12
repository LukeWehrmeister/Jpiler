package com.github.lukewehrmeister.jpiler;

public class Symbol {
    private String name;  // Name of the symbol (e.g., variable or function name)
    private String type;  // Type of the symbol (e.g., int, float, function type)
    
    // Constructor
    public Symbol(String name, String type) {
        this.name = name;
        this.type = type;
    }

    // Getters
    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    // Setters
    public void setName(String name) {
        this.name = name;
    }

    public void setType(String type) {
        this.type = type;
    }
    
    // Override toString for easier debugging
    @Override
    public String toString() {
        return "Symbol{name='" + name + "', type='" + type + "'}";
    }
}
