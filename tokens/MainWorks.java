package Jpiler.tokens;


public class MainWorks {
    public static void main(String[] args) {
        String code = "if (x > 10) { y = x + 5; }";
        System.out.println(code);

        Lexer lexer = new Lexer(code);
        List<Token> tokens = lexer.tokenize();

        for (Token token : tokens) {
            System.out.println(token);
        }
    }
}