package com.github.lukewehrmeister.jpiler;

import java.util.HashMap;
import java.util.Map;

public class SymbolTable {
    private Map<String, Symbol> symbols;

    public SymbolTable() {
        symbols = new HashMap<>();
    }

    public void add(String name, Symbol symbol) {
        if (symbols.containsKey(name)) {
            System.err.println("Error: Variable '" + name + "' already declared in this scope.");
        } else {
            symbols.put(name, symbol);
        }
    }

    public Symbol get(String name) {
        return symbols.get(name);
    }

    public boolean contains(String name) {
        return symbols.containsKey(name);
    }

    public Map<String, Symbol> getSymbols() {
        return symbols;
    }
    
}
