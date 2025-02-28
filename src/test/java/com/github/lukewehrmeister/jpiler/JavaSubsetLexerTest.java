package com.github.lukewehrmeister.jpiler;

import org.antlr.v4.runtime.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


class JavaSubsetLexerTest {

    private CommonTokenStream getLexerTokenStream(String input) {
        CharStream charStream = CharStreams.fromString(input);
        JavaSubsetLexer lexer = new JavaSubsetLexer(charStream);
        return new CommonTokenStream(lexer);
    }
    

    

    @Test
    void testValidIntDeclarationTokens() {
        String input = "int x = 5;";
        CommonTokenStream tokens = getLexerTokenStream(input);
        tokens.fill();

        assertEquals("int", tokens.get(0).getText());
        assertEquals("x", tokens.get(1).getText());
        assertEquals("=", tokens.get(2).getText());
        assertEquals("5", tokens.get(3).getText());
        assertEquals(";", tokens.get(4).getText());

        assertEquals(JavaSubsetLexer.INT, tokens.get(0).getType());
        assertEquals(JavaSubsetLexer.IDENTIFIER, tokens.get(1).getType());
        assertEquals(JavaSubsetLexer.ASSIGN, tokens.get(2).getType());
        assertEquals(JavaSubsetLexer.INTEGER, tokens.get(3).getType());
        assertEquals(JavaSubsetLexer.SEMI, tokens.get(4).getType());
    }


    @Test
    public void testAnotherTokenization() {
        String input = "public static void main(String[] args) { }";
        CommonTokenStream tokens = getLexerTokenStream(input);
        tokens.fill();

        assertEquals("public", tokens.get(0).getText());
        assertEquals("static", tokens.get(1).getText());
        assertEquals("void", tokens.get(2).getText());
        assertEquals("main", tokens.get(3).getText());
        assertEquals("(", tokens.get(4).getText());
        assertEquals("String", tokens.get(5).getText());
        assertEquals("[", tokens.get(6).getText());
        assertEquals("]", tokens.get(7).getText());
        assertEquals("args", tokens.get(8).getText());
        assertEquals(")", tokens.get(9).getText());
        assertEquals("{", tokens.get(10).getText());
        assertEquals("}", tokens.get(11).getText());

        assertEquals(JavaSubsetLexer.PUBLIC, tokens.get(0).getType());
        assertEquals(JavaSubsetLexer.STATIC, tokens.get(1).getType());
        assertEquals(JavaSubsetLexer.VOID, tokens.get(2).getType());
        assertEquals(JavaSubsetLexer.IDENTIFIER, tokens.get(3).getType());
        assertEquals(JavaSubsetLexer.LPAREN, tokens.get(4).getType());
        assertEquals(JavaSubsetLexer.STRING, tokens.get(5).getType());
        assertEquals(JavaSubsetLexer.INVALID_CHAR, tokens.get(6).getType());
        assertEquals(JavaSubsetLexer.INVALID_CHAR, tokens.get(7).getType());
        assertEquals(JavaSubsetLexer.IDENTIFIER, tokens.get(8).getType());
        assertEquals(JavaSubsetLexer.RPAREN, tokens.get(9).getType());
        assertEquals(JavaSubsetLexer.LBRACE, tokens.get(10).getType());
        assertEquals(JavaSubsetLexer.RBRACE, tokens.get(11).getType());
    }


   /*  Test
    public void testFloatDeclerationTokens() {
        String input = "float number = 5.5;";
        CommonTokenStream tokens = getLexerTokenStream(input);
        tokens.fill();

        assertEquals("float", tokens.get(0).getText());
        //assertEquals("float", tokens.get(0).getType());
        assertEquals("number", tokens.get(1).getText());
        assertEquals("=", tokens.get(2).getText());
        assertEquals("5.5", tokens.get(3).getText());
        assertEquals(";", tokens.get(4).getText());
        
    } */





    @Test
    public void testInvalidCharacter() {
        String input = "int x = 5 #";
        CommonTokenStream tokens = getLexerTokenStream(input);
        tokens.fill();
    
        assertEquals("int", tokens.get(0).getText());
        assertEquals("x", tokens.get(1).getText());
        assertEquals("=", tokens.get(2).getText());
        assertEquals("5", tokens.get(3).getText());
        assertEquals("#", tokens.get(4).getText());

        assertEquals(JavaSubsetLexer.INT, tokens.get(0).getType());
        assertEquals(JavaSubsetLexer.IDENTIFIER, tokens.get(1).getType());
        assertEquals(JavaSubsetLexer.ASSIGN, tokens.get(2).getType());
        assertEquals(JavaSubsetLexer.INTEGER, tokens.get(3).getType());
        assertEquals(JavaSubsetLexer.INVALID_CHAR, tokens.get(4).getType(), "Lexer should recognize unrecognized characters");
    }
    

    /* @Test
    public void testInvalidIdentifier() {
        String input = "5employees";  
        CommonTokenStream tokens = getLexerTokenStream(input);
        tokens.fill();

        // Print all tokens in the token stream
        for (Token token : tokens.getTokens()) {
            System.out.println("Token: " + token.getText() + " Type: " + token.getType());
        }

        assertEquals("5employees", tokens.get(0).getText());
        //assertEquals("5employees", tokens.get(0).getType());
    } */


    
}

