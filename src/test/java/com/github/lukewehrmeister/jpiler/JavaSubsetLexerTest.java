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
    void testValidDecimalIntegerDeclarationTokens() {
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
        assertEquals(JavaSubsetLexer.DECIMAL_LITERAL, tokens.get(3).getType());
        assertEquals(JavaSubsetLexer.SEMI, tokens.get(4).getType());
    }


    @Test
    void testValidHexIntegerDeclarationTokens() {
        String input = "int x = 0x1A;";
        CommonTokenStream tokens = getLexerTokenStream(input);
        tokens.fill();

        assertEquals("int", tokens.get(0).getText());
        assertEquals("x", tokens.get(1).getText());
        assertEquals("=", tokens.get(2).getText());
        assertEquals("0x1A", tokens.get(3).getText());
        assertEquals(";", tokens.get(4).getText());

        assertEquals(JavaSubsetLexer.INT, tokens.get(0).getType());
        assertEquals(JavaSubsetLexer.IDENTIFIER, tokens.get(1).getType());
        assertEquals(JavaSubsetLexer.ASSIGN, tokens.get(2).getType());
        assertEquals(JavaSubsetLexer.HEX_LITERAL, tokens.get(3).getType());
        assertEquals(JavaSubsetLexer.SEMI, tokens.get(4).getType());
    }


    @Test
    void testValidBinaryIntegerDeclarationTokens() {
        String input = "int x = 0b0110;";
        CommonTokenStream tokens = getLexerTokenStream(input);
        tokens.fill();

        assertEquals("int", tokens.get(0).getText());
        assertEquals("x", tokens.get(1).getText());
        assertEquals("=", tokens.get(2).getText());
        assertEquals("0b0110", tokens.get(3).getText());
        assertEquals(";", tokens.get(4).getText());

        assertEquals(JavaSubsetLexer.INT, tokens.get(0).getType());
        assertEquals(JavaSubsetLexer.IDENTIFIER, tokens.get(1).getType());
        assertEquals(JavaSubsetLexer.ASSIGN, tokens.get(2).getType());
        assertEquals(JavaSubsetLexer.BINARY_LITERAL, tokens.get(3).getType());
        assertEquals(JavaSubsetLexer.SEMI, tokens.get(4).getType());
    }


    @Test
    void testValidOctalIntegerDeclarationTokens() {
        String input = "int x = 040;";
        CommonTokenStream tokens = getLexerTokenStream(input);
        tokens.fill();

        assertEquals("int", tokens.get(0).getText());
        assertEquals("x", tokens.get(1).getText());
        assertEquals("=", tokens.get(2).getText());
        assertEquals("040", tokens.get(3).getText());
        assertEquals(";", tokens.get(4).getText());

        assertEquals(JavaSubsetLexer.INT, tokens.get(0).getType());
        assertEquals(JavaSubsetLexer.IDENTIFIER, tokens.get(1).getType());
        assertEquals(JavaSubsetLexer.ASSIGN, tokens.get(2).getType());
        assertEquals(JavaSubsetLexer.OCTAL_LITERAL, tokens.get(3).getType());
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


    @Test
    public void testFloatDeclerationTokens() {
        String input = "float number = 5.5;";
        CommonTokenStream tokens = getLexerTokenStream(input);
        tokens.fill();

        assertEquals("float", tokens.get(0).getText());
        assertEquals("number", tokens.get(1).getText());
        assertEquals("=", tokens.get(2).getText());
        assertEquals("5.5", tokens.get(3).getText());
        assertEquals(";", tokens.get(4).getText());

        assertEquals(JavaSubsetLexer.FLOAT, tokens.get(0).getType());
        assertEquals(JavaSubsetLexer.IDENTIFIER, tokens.get(1).getType());
        assertEquals(JavaSubsetLexer.ASSIGN, tokens.get(2).getType());
        assertEquals(JavaSubsetLexer.FLOAT_LITERAL, tokens.get(3).getType());
        assertEquals(JavaSubsetLexer.SEMI, tokens.get(4).getType());
    }


    @Test
    public void testIfElseIfElseStatement() {
        String input = "if(x == true){y = z + 5;} else if(y== false){y++;}else{z /= 3;}";
        CommonTokenStream tokens = getLexerTokenStream(input);
        tokens.fill();

        assertEquals("if", tokens.get(0).getText());
        assertEquals("(", tokens.get(1).getText());
        assertEquals("x", tokens.get(2).getText());
        assertEquals("==", tokens.get(3).getText());
        assertEquals("true", tokens.get(4).getText());
        assertEquals(")", tokens.get(5).getText());
        assertEquals("{", tokens.get(6).getText());
        assertEquals("y", tokens.get(7).getText());
        assertEquals("=", tokens.get(8).getText());
        assertEquals("z", tokens.get(9).getText());
        assertEquals("+", tokens.get(10).getText());
        assertEquals("5", tokens.get(11).getText());
        assertEquals(";", tokens.get(12).getText());
        assertEquals("}", tokens.get(13).getText());
        assertEquals("else", tokens.get(14).getText());
        assertEquals("if", tokens.get(15).getText());
        assertEquals("(", tokens.get(16).getText());
        assertEquals("y", tokens.get(17).getText());
        assertEquals("==", tokens.get(18).getText());
        assertEquals("false", tokens.get(19).getText());
        assertEquals(")", tokens.get(20).getText());
        assertEquals("{", tokens.get(21).getText());
        assertEquals("y", tokens.get(22).getText());
        assertEquals("++", tokens.get(23).getText());
        assertEquals(";", tokens.get(24).getText());
        assertEquals("}", tokens.get(25).getText());
        assertEquals("else", tokens.get(26).getText());
        assertEquals("{", tokens.get(27).getText());
        assertEquals("z", tokens.get(28).getText());
        assertEquals("/=", tokens.get(29).getText());
        assertEquals("3", tokens.get(30).getText());
        assertEquals(";", tokens.get(31).getText());
        assertEquals("}", tokens.get(32).getText());


        assertEquals(JavaSubsetLexer.IF, tokens.get(0).getType());
        assertEquals(JavaSubsetLexer.LPAREN, tokens.get(1).getType());
        assertEquals(JavaSubsetLexer.IDENTIFIER, tokens.get(2).getType());
        assertEquals(JavaSubsetLexer.EQ, tokens.get(3).getType());
        assertEquals(JavaSubsetLexer.TRUE, tokens.get(4).getType());
        assertEquals(JavaSubsetLexer.RPAREN, tokens.get(5).getType());
        assertEquals(JavaSubsetLexer.LBRACE, tokens.get(6).getType());
        assertEquals(JavaSubsetLexer.IDENTIFIER, tokens.get(7).getType());
        assertEquals(JavaSubsetLexer.ASSIGN, tokens.get(8).getType());
        assertEquals(JavaSubsetLexer.IDENTIFIER, tokens.get(9).getType());
        assertEquals(JavaSubsetLexer.PLUS, tokens.get(10).getType());
        assertEquals(JavaSubsetLexer.DECIMAL_LITERAL, tokens.get(11).getType());
        assertEquals(JavaSubsetLexer.SEMI, tokens.get(12).getType());
        assertEquals(JavaSubsetLexer.RBRACE, tokens.get(13).getType());
        assertEquals(JavaSubsetLexer.ELSE, tokens.get(14).getType());
        assertEquals(JavaSubsetLexer.IF, tokens.get(15).getType());
        assertEquals(JavaSubsetLexer.LPAREN, tokens.get(16).getType());
        assertEquals(JavaSubsetLexer.IDENTIFIER, tokens.get(17).getType());
        assertEquals(JavaSubsetLexer.EQ, tokens.get(18).getType());
        assertEquals(JavaSubsetLexer.FALSE, tokens.get(19).getType());
        assertEquals(JavaSubsetLexer.RPAREN, tokens.get(20).getType());
        assertEquals(JavaSubsetLexer.LBRACE, tokens.get(21).getType());
        assertEquals(JavaSubsetLexer.IDENTIFIER, tokens.get(22).getType());
        assertEquals(JavaSubsetLexer.INCREMENT, tokens.get(23).getType());
        assertEquals(JavaSubsetLexer.SEMI, tokens.get(24).getType());
        assertEquals(JavaSubsetLexer.RBRACE, tokens.get(25).getType());
        assertEquals(JavaSubsetLexer.ELSE, tokens.get(26).getType());
        assertEquals(JavaSubsetLexer.LBRACE, tokens.get(27).getType());
        assertEquals(JavaSubsetLexer.IDENTIFIER, tokens.get(28).getType());
        assertEquals(JavaSubsetLexer.DIVEQ, tokens.get(29).getType());
        assertEquals(JavaSubsetLexer.DECIMAL_LITERAL, tokens.get(30).getType());
        assertEquals(JavaSubsetLexer.SEMI, tokens.get(31).getType());
        assertEquals(JavaSubsetLexer.RBRACE, tokens.get(32).getType());
    }





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
        assertEquals(JavaSubsetLexer.DECIMAL_LITERAL, tokens.get(3).getType());
        assertEquals(JavaSubsetLexer.INVALID_CHAR, tokens.get(4).getType(), "Lexer should recognize unrecognized characters");
    }
    

    @Test
    public void testInvalidIdentifier() {
        String input = "5employees";  
        CommonTokenStream tokens = getLexerTokenStream(input);
        tokens.fill();

        // Print all tokens in the token stream
        // for (Token token : tokens.getTokens()) {
        //     System.out.println("Token: " + token.getText() + " Type: " + token.getType());
        // }

        assertEquals("5employees", tokens.get(0).getText());

        assertEquals(JavaSubsetLexer.INVALID_IDENTIFIER, tokens.get(0).getType());
    }


    
}

