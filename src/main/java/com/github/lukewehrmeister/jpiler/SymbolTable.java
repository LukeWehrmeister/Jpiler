package com.github.lukewehrmeister.jpiler;

import java.util.HashMap;
import java.util.Map;

public class SymbolTable {
    private Map<String, Symbol> symbols;

    public SymbolTable() {
        symbols = new HashMap<>();
    }

    public void add(String name, Symbol symbol) {
        symbols.put(name, symbol);
    }

    public Symbol get(String name) {
        return symbols.get(name);
    }

    public boolean contains(String name) {
        return symbols.containsKey(name);
    }
}
