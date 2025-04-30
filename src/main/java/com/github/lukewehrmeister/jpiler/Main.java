package com.github.lukewehrmeister.jpiler;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Usage: java -jar Jpiler.jar <input-file.java> <output-ir.txt>");
            System.exit(1);
        }

        String inputPath = args[0];
        String outputPath = args[1];

        try {
            String code = new String(Files.readAllBytes(Paths.get(inputPath)));

            CharStream input = CharStreams.fromString(code);
            JavaSubsetLexer lexer = new JavaSubsetLexer(input);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            JavaSubsetParser parser = new JavaSubsetParser(tokens);
            ParseTree tree = parser.compilationUnit();

            SymbolTableVisitor semanticVisitor = new SymbolTableVisitor();
            semanticVisitor.visit(tree);
            if (!semanticVisitor.getErrors().isEmpty()) {
                System.err.println("Semantic Errors:");
                for (String err : semanticVisitor.getErrors()) {
                    System.err.println("  " + err);
                }
                System.exit(2);
            }

            IRGeneratorVisitor irVisitor = new IRGeneratorVisitor(
                semanticVisitor.getTableForContext(),
                semanticVisitor.getSymbolMap()
            );
            irVisitor.visit(tree);

            Files.writeString(Paths.get(outputPath), irVisitor.getIR());
            System.out.println("IR written to " + outputPath);
        } catch (IOException e) {
            System.err.println("IO Error: " + e.getMessage());
            System.exit(3);
        } catch (Exception e) {
            System.err.println("Unexpected Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(4);
        }
    }
}
