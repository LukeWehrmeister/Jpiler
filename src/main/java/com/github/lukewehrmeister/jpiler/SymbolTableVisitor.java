package com.github.lukewehrmeister.jpiler;

import java.util.Stack;

public class SymbolTableVisitor extends JavaSubsetBaseVisitor<Void> {
    private Stack<SymbolTable> scopes = new Stack<>();

    public SymbolTableVisitor() {
        scopes.push(new SymbolTable()); // Global scope
    }

    private SymbolTable currentScope() {
        return scopes.peek();
    }

    @Override
    public Void visitClassDeclaration(JavaSubsetParser.ClassDeclarationContext ctx) {
        String className = ctx.IDENTIFIER().getText();
        currentScope().add(className, new Symbol(className, "class"));
        return null;
    }

    @Override
    public Void visitMethodDeclaration(JavaSubsetParser.MethodDeclarationContext ctx) {
        String methodName = ctx.IDENTIFIER().getText();
        String returnType = ctx.returnType().getText();
        
        // Add method to the current scope (class scope)
        currentScope().add(methodName, new Symbol(methodName, returnType));

        // Create a new scope for method-local variables
        scopes.push(new SymbolTable());

        // Visit the method body
        super.visitMethodDeclaration(ctx);

        // Pop the method scope after visiting
        scopes.pop();
        return null;
    }

    @Override
    public Void visitVariableDeclaration(JavaSubsetParser.VariableDeclarationContext ctx) {
        String varName = ctx.IDENTIFIER().getText();
        String varType = ctx.type().getText();
        
        if (currentScope().contains(varName)) {
            System.err.println("Error: Variable " + varName + " already declared in this scope.");
        } else {
            currentScope().add(varName, new Symbol(varName, varType));
        }

        return null;
    }

    @Override
    public Void visitAssignment(JavaSubsetParser.AssignmentContext ctx) {
        String varName = ctx.IDENTIFIER().getText();

        // Check all scopes from innermost to outermost
        boolean found = false;
        for (int i = scopes.size() - 1; i >= 0; i--) {
            if (scopes.get(i).contains(varName)) {
                found = true;
                break;
            }
        }

        if (!found) {
            System.err.println("Error: Variable " + varName + " not declared in any accessible scope.");
        }

        return null;
    }

    @Override
    public Void visitBlock(JavaSubsetParser.BlockContext ctx) {
        // Push a new scope for the block
        scopes.push(new SymbolTable());

        // Visit block contents
        super.visitBlock(ctx);

        // Pop the block scope
        scopes.pop();
        return null;
    }
}
