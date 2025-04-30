package com.github.lukewehrmeister.jpiler;

public class Symbol {
    private final String name;  
    private final SemanticType type;  
    private final SymbolKind kind;
    private final String accessModifier; 
    private boolean initialized;
    
    // Constructor
    public Symbol(String name, SemanticType type, SymbolKind kind, String accessModifier, boolean initialized) {
        this.name = name;
        this.type = type;
        this.kind = kind;
        this.accessModifier = accessModifier;
        this.initialized = initialized;
    }

    //For methods and classes
    public Symbol(String name, SemanticType type, SymbolKind kind, String accessModifier) {
        this(name, type, kind, accessModifier, false);
    }

    //For variables and parameters
    public Symbol(String name, SemanticType type, SymbolKind kind, boolean initialized) {
        this(name, type, kind, "default", initialized);
    }

    // Getters
    public String getName() {
        return name;
    }

    public SemanticType getType() {
        return type;
    }

    public SymbolKind getKind() {
        return kind;
    }

    public String getAccessModifier() {
        return accessModifier;
    }


    public boolean getInitialized() {
        return initialized;
    }

    public void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }
    
    // Override toString for easier debugging
    @Override
    public String toString() {
        return "Symbol{name='" + name + "', type='" + type + "'}";
    }
}
