package Jpiler.tokens;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Lexer{
    private String input;
    private int currPosition;

    public Lexer(String input) {
        this.input = input;
        this.currPosition = 0;
    }

    public List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();

        while (currPosition < input.length()) {
            char currentChar = input.charAt(currPosition);

            if (Character.isWhitespace(currentChar)) {
                currPosition++;
                continue;
            }

            Token token = nextToken();
            if (token != null) {
                tokens.add(token);
            } else {
                throw new RuntimeException("Unknown character: " + currentChar);
            }
        }

        return tokens;
    }

    private Token nextToken() {
        if (currPosition >= input.length()) {
            return null;
        }

        String[] tokenPatterns = {
            "if|else|while|for|import",             // Keywords
            "[a-zA-Z_][a-zA-Z0-9_]*(\\.[a-zA-Z_][a-zA-Z0-9_]*)*",        // Identifiers
            "\\d+",                          // Literals
            "[+-/*%=<>!]",                   // Operators
            "[.,;(){}]",                     // Punctuation
            "\\\"(\\\\\\\\.|[^\\\"])*\\\"",   //String Literal
        };

        TokenType[] tokenTypes = {
            TokenType.KEYWORD,
            TokenType.IDENTIFIER,
            TokenType.LITERAL,
            TokenType.OPERATOR,
            TokenType.PUNCTUATION,
            TokenType.STRINGLITERAL
        };

        for (int i = 0; i < tokenPatterns.length; i++) {
            Pattern pattern = Pattern.compile("^" + tokenPatterns[i]);
            Matcher matcher = pattern.matcher(input.substring(currPosition));

            if (matcher.find()) {
                String value = matcher.group();
                currPosition += value.length();
                return new Token(tokenTypes[i], value);
            }
        }

        return null;
    }
}