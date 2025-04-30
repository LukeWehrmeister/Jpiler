package com.github.lukewehrmeister.jpiler;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class JavaSubsetParserTest {

    private JavaSubsetParser parse(String input) {
        CharStream charStream = CharStreams.fromString(input);
        JavaSubsetLexer lexer = new JavaSubsetLexer(charStream);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        return new JavaSubsetParser(tokens);
    }




    @Test
    void testValidClass() {
        String input = "class MyClass { public static void main(String[] args) { int x = 5; } }";
        JavaSubsetParser parser = parse(input);
        ParseTree tree = parser.compilationUnit();

        assertNotNull(tree);
        assertEquals(0, parser.getNumberOfSyntaxErrors(), "Should have no syntax errors.");
    }


    @Test
    void testVariableDeclaration() {
        String input = "int x = 5;";
        JavaSubsetParser parser = parse(input);
        ParseTree tree = parser.statement();

        assertNotNull(tree);
        assertEquals(0, parser.getNumberOfSyntaxErrors(), "Should have no syntax errors.");
    }


    @Test
    void testVariableAssignment() {
        String input = "x = 10;";
        JavaSubsetParser parser = parse(input);
        ParseTree tree = parser.statement();

        assertNotNull(tree);
        assertEquals(0, parser.getNumberOfSyntaxErrors(), "Should have no syntax errors.");
    }


    @Test
    void testValidIfStatement() {
        String input = "if (x > 0) { x = x - 1; } else { x = x + 1; }";
        JavaSubsetParser parser = parse(input);
        ParseTree tree = parser.ifStatement();

        assertNotNull(tree);
        assertEquals(0, parser.getNumberOfSyntaxErrors(), "Should have no syntax errors.");
    }


    @Test
    void testValidIfElseIfElseStatement() {
        String input = "if(x == true){y = z + 5;} else if(y== false){y++;}else{z = z/ 3;}";
        JavaSubsetParser parser = parse(input);
        ParseTree tree = parser.ifStatement();

        assertNotNull(tree);
        assertEquals(0, parser.getNumberOfSyntaxErrors(), "Should have no syntax errors.");
    }


    @Test
    void testIncrement() {
        String input = "x++;";
        JavaSubsetParser parser = parse(input);
        ParseTree tree = parser.statement();

        assertNotNull(tree);
        assertEquals(0, parser.getNumberOfSyntaxErrors(), "Should have no syntax errors.");
    }

    

    @Test
    void testInvalidCharacterAmpersand() {
        String input = "int x = 5 & 10;";
        JavaSubsetParser parser = parse(input);
        ParseTree tree = parser.statement();

        assertNotNull(tree);
        assertNotEquals(0, parser.getNumberOfSyntaxErrors(), "Should have at least 1 syntax error due to unrecognized '&' character.");
    }


    @Test
    void testInvalidSyntaxMisspelledKeyword() {
        String input = "fore (int i = 0; i < 10; i++) { }";
        JavaSubsetParser parser = parse(input);
        ParseTree tree = parser.compilationUnit();

        assertNotNull(tree);
        assertNotEquals(0, parser.getNumberOfSyntaxErrors(), "Should have at least 1 syntax error due to unrecognized 'fore' keyword.");
    }


    @Test
    void testInvalidSyntaxMissingClassName() {
        String input = "class { int x; }"; 
        JavaSubsetParser parser = parse(input);
        ParseTree tree = parser.compilationUnit();

        assertNotNull(tree);
        assertNotEquals(0, parser.getNumberOfSyntaxErrors(), "Should have at least 1 syntax error due to missing class name.");
    }


    @Test
    void testVariableDeclarationInvalidMissingEquals() {
        String input = "int x 5;";
        JavaSubsetParser parser = parse(input);
        ParseTree tree = parser.statement();

        assertNotNull(tree);
        assertNotEquals(0, parser.getNumberOfSyntaxErrors(), "Should have at least 1 syntax error due to missing equal sign.");
    }


    @Test
    void testValidAssignmentExpression() {
        String input = "x = 10";
        JavaSubsetParser parser = parse(input);
        ParseTree tree = parser.statement();

        assertNotNull(tree);
        assertEquals(0, parser.getNumberOfSyntaxErrors(), "Should have no syntax error because it evaluates to an expression.");
    }


    @Test
    void testUnclosedString() {
        String input = "String s = \"Hello world;";
        JavaSubsetParser parser = parse(input);
        ParseTree tree = parser.statement();

        assertNotNull(tree);
        assertNotEquals(0, parser.getNumberOfSyntaxErrors(), "Should have at least 1 syntax error due to unclosed string.");
    }

    @Test
    void testValidForLoop() {
        String input = """
            class LoopTest {
                void count() {
                    for (int i = 0; i < 5; i++) {
                        int x;
                        x = i;
                    }
                }
            }
            """;
        JavaSubsetParser parser = parse(input);
        ParseTree tree = parser.compilationUnit();

        assertNotNull(tree);
        System.out.println(tree);
        assertEquals(0, parser.getNumberOfSyntaxErrors(), "Should have no syntax errors.");
    }

    @Test
    void testValidInfiniteForLoop() {
        String input = """
            class Spin {
                void loop() {
                    for (;;) {
                        int x;
                        x = 1;
                        break;
                    }
                }
            }
            """;
        JavaSubsetParser parser = parse(input);
        ParseTree tree = parser.compilationUnit();

        assertNotNull(tree);
        assertEquals(0, parser.getNumberOfSyntaxErrors(), "Should have no syntax errors.");
    }
}
