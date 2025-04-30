package com.github.lukewehrmeister.jpiler;

import org.antlr.v4.runtime.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class IRGeneratorVisitorTest {
    
    private String generateIRFor(String input) {
        CharStream charStream = CharStreams.fromString(input);
        JavaSubsetLexer lexer = new JavaSubsetLexer(charStream);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        JavaSubsetParser parser = new JavaSubsetParser(tokens);
    
        JavaSubsetParser.CompilationUnitContext tree = parser.compilationUnit();
    
        SymbolTableVisitor semanticVisitor = new SymbolTableVisitor();
        semanticVisitor.visit(tree);
        assertTrue(semanticVisitor.getErrors().isEmpty(), "Semantic errors: " + semanticVisitor.getErrors());
    
        IRGeneratorVisitor irGenerator = new IRGeneratorVisitor(
            semanticVisitor.getTableForContext(),
            semanticVisitor.getSymbolMap()
        );
        irGenerator.visit(tree);
    
        return irGenerator.getIR();
    }

    


    @Test
    public void testSimpleReturnIR() {
        String input = """
            class Math {
                int add() {
                    int x = 1;
                    int y = 2;
                    return x + y;
                }
            }
            """;

        String expectedIR = """
            ; === Class Math ===
            define i32 @add() {
              %x = alloca i32
              %y = alloca i32
              store i32 1, i32* %x
              store i32 2, i32* %y
              %t0 = load i32, i32* %x
              %t1 = load i32, i32* %y
              %t2 = add i32 %t0, %t1
              ret i32 %t2
            }
            """.replace("\r\n", "\n").replaceAll("[ \t]+(?=\n)", "").trim();

            String actualIR = generateIRFor(input).replace("\r\n", "\n").replaceAll("[ \t]+(?=\n)", "").trim();
        

        assertEquals(expectedIR, actualIR, "IR does not exactly match expected output.");
    }

    
    @Test
    public void testFullIRForSimpleForLoop() {
        String input = """
            class Counter {
                void count() {
                    for (int i = 0; i < 3; i++) {
                        int x = i;
                    }
                }
            }
        """;
    
        String expectedIR = """
            ; === Class Counter ===
            define void @count() {
              %i = alloca i32
              store i32 0, i32* %i
              br label %for.cond.0
            for.cond.0:
              %t0 = load i32, i32* %i
              %t1 = icmp slt i32 %t0, 3
              br i1 %t1, label %for.body.1, label %for.end.3
            for.body.1:
              %x = alloca i32
              %t2 = load i32, i32* %i
              store i32 %t2, i32* %x
              br label %for.update.2
            for.update.2:
              %t3 = load i32, i32* %i
              %t4 = add i32 %t3, 1
              store i32 %t4, i32* %i
              br label %for.cond.0
            for.end.3:
            }
            """.replace("\r\n", "\n").replaceAll("[ \t]+(?=\n)", "").trim();

        String actualIR = generateIRFor(input).replace("\r\n", "\n").replaceAll("[ \t]+(?=\n)", "").trim();
    
        assertEquals(expectedIR, actualIR, "IR does not match expected output.");
    }

    @Test
    public void testArithmeticExpressionIR() {
        String input = """
            class Calculator {
                void compute() {
                    int result = 3 + 4 * 2;
                }
            }
            """;
    
        String expectedIR = """
            ; === Class Calculator ===
            define void @compute() {
              %result = alloca i32
              %t0 = mul i32 4, 2
              %t1 = add i32 3, %t0
              store i32 %t1, i32* %result
            }
            """.replace("\r\n", "\n").replaceAll("[ \t]+(?=\n)", "").trim();
    
        String actualIR = generateIRFor(input).replace("\r\n", "\n").replaceAll("[ \t]+(?=\n)", "").trim();
    
        assertEquals(expectedIR, actualIR, "IR does not match expected output for arithmetic expression.");
    }
    
    @Test
    public void testSubAndDivIR() {
        String input = """
            class Math {
                void calculate() {
                    int a = 10 - 3;
                    int b = 8 / 2;
                }
            }
            """;
    
        String expectedIR = """
            ; === Class Math ===
            define void @calculate() {
              %a = alloca i32
              %b = alloca i32
              %t0 = sub i32 10, 3
              store i32 %t0, i32* %a
              %t1 = sdiv i32 8, 2
              store i32 %t1, i32* %b
            }
            """.trim();
    
        String actualIR = generateIRFor(input).replaceAll("[ \t]+(?=\n)", "").trim();
        assertEquals(expectedIR, actualIR, "IR does not match expected subtraction and division output.");
    }

    @Test
    public void testPostfixIncrementIR() {
        String input = """
            class Increment {
                void doIt() {
                    int n = 7;
                    n++;
                }
            }
            """;
    
        String expectedIR = """
            ; === Class Increment ===
            define void @doIt() {
              %n = alloca i32
              store i32 7, i32* %n
              %t0 = load i32, i32* %n
              %t1 = add i32 %t0, 1
              store i32 %t1, i32* %n
            }
            """.trim();
    
        String actualIR = generateIRFor(input).replaceAll("[ \t]+(?=\n)", "").trim();
        assertEquals(expectedIR, actualIR, "IR does not match expected postfix increment output.");
    }

    @Test
    public void testNestedExpressionIR() {
        String input = """
            class Nested {
                void compute() {
                    int z = (1 + 2) * (3 + 4);
                }
            }
            """;
    
        String expectedIR = """
            ; === Class Nested ===
            define void @compute() {
              %z = alloca i32
              %t0 = add i32 1, 2
              %t1 = add i32 3, 4
              %t2 = mul i32 %t0, %t1
              store i32 %t2, i32* %z
            }
            """.trim();
    
        String actualIR = generateIRFor(input).replaceAll("[ \t]+(?=\n)", "").trim();
        assertEquals(expectedIR, actualIR, "IR does not match expected nested expression output.");
    }
    
    @Test
    public void testIfElseIR() {
        String input = """
            class Branch {
                void decide() {
                    int x = 5;
                    int y;
                    if (x > 3) {
                        y = x + 1;
                    } else {
                        y = x - 1;
                    }
                }
            }
            """;
    
        String expectedIR = """
            ; === Class Branch ===
            define void @decide() {
              %x = alloca i32
              %y = alloca i32
              store i32 5, i32* %x
              %t0 = load i32, i32* %x
              %t1 = icmp sgt i32 %t0, 3
              br i1 %t1, label %then.0, label %else.1
            then.0:
              %t2 = load i32, i32* %x
              %t3 = add i32 %t2, 1
              store i32 %t3, i32* %y
              br label %endif.2
            else.1:
              %t4 = load i32, i32* %x
              %t5 = sub i32 %t4, 1
              store i32 %t5, i32* %y
              br label %endif.2
            endif.2:
            }
            """.trim();
    
        String actualIR = generateIRFor(input).replaceAll("[ \t]+(?=\n)", "").trim();
        assertEquals(expectedIR, actualIR, "IR does not match expected output for if-else block.");
    }
    
    @Test
    public void testMethodWithParametersIR() {
        String input = """
            class Math {
                void add(int a, int b) {
                    int sum = a + b;
                }
            }
            """;
    
        String expectedIR = """
            ; === Class Math ===
            define void @add(i32 %a, i32 %b) {
              %a.addr = alloca i32
              store i32 %a, i32* %a.addr
              %b.addr = alloca i32
              store i32 %b, i32* %b.addr
              %sum = alloca i32
              %t0 = load i32, i32* %a.addr
              %t1 = load i32, i32* %b.addr
              %t2 = add i32 %t0, %t1
              store i32 %t2, i32* %sum
            }
            """.replace("\r\n", "\n").replaceAll("[ \t]+(?=\n)", "").trim();
    
        String actualIR = generateIRFor(input).replace("\r\n", "\n").replaceAll("[ \t]+(?=\n)", "").trim();
        assertEquals(expectedIR, actualIR, "IR does not match expected output for method with parameters.");
    }
    

}

