package com.github.lukewehrmeister.jpiler;

import org.antlr.v4.runtime.*;

public class Main {
    public static void main(String[] args) throws Exception {
        // Read the input Java code
        String input = "public class Example { int x = 10; x = 20; }";

        // Create an ANTLRInputStream from the input
        CharStream inputStream = CharStreams.fromString(input);


        // Create a lexer and a token stream
        JavaSubsetLexer lexer = new JavaSubsetLexer(inputStream);
        CommonTokenStream tokens = new CommonTokenStream(lexer);

        // Create a parser
        JavaSubsetParser parser = new JavaSubsetParser(tokens);

        // Parse the input to get the root node of the parse tree
        JavaSubsetParser.CompilationUnitContext tree = parser.compilationUnit();

        // Create a visitor and walk the tree
        SymbolTableVisitor visitor = new SymbolTableVisitor();
        visitor.visit(tree);

        // After visiting, you can check the symbol table
        // for any semantic errors or use the collected information
    }
}
