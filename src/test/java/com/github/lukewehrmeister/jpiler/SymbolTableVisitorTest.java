package com.github.lukewehrmeister.jpiler;

import static org.junit.jupiter.api.Assertions.*;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import org.junit.jupiter.api.Test;


class SymbolTableVisitorTest {

    private SymbolTableVisitor getVisitorFor(String input) {
        CharStream charStream = CharStreams.fromString(input);
        JavaSubsetLexer lexer = new JavaSubsetLexer(charStream);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        JavaSubsetParser parser = new JavaSubsetParser(tokens);

        ParseTree tree = parser.compilationUnit();
        SymbolTableVisitor visitor = new SymbolTableVisitor();
        visitor.visit(tree);
        return visitor;
    }


    
    @Test
    public void testBasicClassStructure() {
        String code = """
            class Hello {
                int x;
                void greet() {
                    int y;
                    y = 42;
                }
            }
            """;

            SymbolTableVisitor visitor = getVisitorFor(code);
            assertTrue(visitor.getErrors().isEmpty(), "Expected no semantic errors.");

    }

    @Test
    public void testValidForLoopWithDeclaration() {
        String code = """
            class LoopTest {
                void count() {
                    for (int i = 0; i < 5; i++) {
                        int x;
                        x = i;
                    }
                }
            }
            """;
        SymbolTableVisitor visitor = getVisitorFor(code);
        assertTrue(visitor.getErrors().isEmpty(), "Expected no semantic errors.");
    }

    @Test
    public void testInvalidAssignmentInForLoop() {
        String code = """
            class LoopFail {
                void broken() {
                    for (; i < 5; i = i + 1) {
                        int x;
                        x = i;
                    }
                }
            }
            """;
        SymbolTableVisitor visitor = getVisitorFor(code);
        assertFalse(visitor.getErrors().isEmpty(), "Expected error for undeclared variable 'i'.");
    }

    @Test
    public void testDuplicateVariableInMethod() {
        String code = """
            class Clash {
                void repeat() {
                    int a;
                    int a;
                }
            }
            """;
        SymbolTableVisitor visitor = getVisitorFor(code);
        assertFalse(visitor.getErrors().isEmpty(), "Expected error for duplicate variable 'a'.");
    }

    @Test
    public void testInfiniteLoop() {
        String code = """
            class Spin {
                void loop() {
                    for (;;) {
                        int x;
                        x = 1;
                    }
                }
            }
            """;
        SymbolTableVisitor visitor = getVisitorFor(code);
        assertTrue(visitor.getErrors().isEmpty(), "Expected no semantic errors in infinite loop.");
    }

    @Test
    public void testUseBeforeDeclaration() {
        String input = """
            class Test {
                void foo() {
                    x = 5;
                    int x;
                }
            }
            """;
            
        SymbolTableVisitor visitor = getVisitorFor(input);
        assertFalse(visitor.getErrors().isEmpty(), "Expected error for use of variable x before declaration.");
    }

    @Test
    public void testMultipleInitializationInForLoop() {
        String code = """
            class MultiInit {
                void loop() {
                    for (int i = 0, j = 5; i < j; i++) {
                        int x;
                        x = i + j;
                    }
                }
            }
            """;
        SymbolTableVisitor visitor = getVisitorFor(code);
        assertTrue(visitor.getErrors().isEmpty(), "Expected no semantic errors with multiple init variables.");
    }
    
    @Test
    public void testDuplicateDeclarationInForLoopInit() {
        String code = """
            class Redeclare {
                void broken() {
                    int i;
                    for (int i = 0; i < 5; i++) {
                        int x;
                        x = i;
                    }
                }
            }
            """;
        SymbolTableVisitor visitor = getVisitorFor(code);
        assertFalse(visitor.getErrors().isEmpty(), "Expected error for redeclared variable 'i' inside for-loop.");
    }

    @Test
    public void testVariableScopeAfterForLoop() {
        String code = """
            class AfterLoop {
                void check() {
                    int sum = 0;
                    for (int i = 0; i < 5; i++) {
                        sum = sum + i;
                    }
                    int x = sum;
                }
            }
            """;
        SymbolTableVisitor visitor = getVisitorFor(code);
        assertTrue(visitor.getErrors().isEmpty(), "Expected no semantic errors using variable after loop.");
    }
    
    @Test
    public void testUsingLoopDeclaredVariableOutside() {
        String code = """
            class OutOfScope {

                void fail() {
                    for (int i = 0; i < 5; i++) {}
                    int x = i; // i should be out of scope
                }
            }
            """;
        SymbolTableVisitor visitor = getVisitorFor(code);
        assertFalse(visitor.getErrors().isEmpty(), "Expected error for use of 'i' outside for-loop.");
    }
    
    @Test
    public void testNestedForLoops() {
        String code = """
            class Nest {
                void loops() {
                    for (int i = 0; i < 5; i++) {
                        for (int j = 0; j < 5; j++) {
                            int x;
                            x = i + j;
                        }
                    }
                }
            }
            """;
        SymbolTableVisitor visitor = getVisitorFor(code);
        assertTrue(visitor.getErrors().isEmpty(), "Expected no semantic errors with nested for-loops.");
    }
    
    @Test
    public void testDuplicateMethodNames() {
        String code = """
            class MethodClash {
                void foo() {}
                void foo() {}
            }
            """;
        SymbolTableVisitor visitor = getVisitorFor(code);
        assertFalse(visitor.getErrors().isEmpty(), "Expected error for duplicate method 'foo'.");
    }
    
    @Test
    public void testEmptyMethod() {
        String code = """
            class EmptyMethod {
                void doNothing() {}
            }
            """;
        SymbolTableVisitor visitor = getVisitorFor(code);
        assertTrue(visitor.getErrors().isEmpty(), "Expected no semantic errors in empty method.");
    }

    @Test
    public void testAssignToUndeclaredVariable() {
        String code = """
            class UndeclaredAssign {
                void bad() {
                    y = 10;
                }
            }
            """;
        SymbolTableVisitor visitor = getVisitorFor(code);
        assertFalse(visitor.getErrors().isEmpty(), "Expected error for assignment to undeclared 'y'.");
    }
    
    // @Test
    // public void testBlockScopedShadowing() {
    //     String code = """
    //         class Shadowing {
    //             void example() {
    //                 int x = 1;
    //                 {
    //                     int x = 2; // allowed: shadows outer x
    //                 }
    //             }
    //         }
    //         """;
    //     SymbolTableVisitor visitor = getVisitorFor(code);
    //     assertTrue(visitor.getErrors().isEmpty(), "Expected no semantic errors with block shadowing.");
    // }



    @Test
    public void testTypeMismatch() {
        String code = """
            class TypeMismatch {
                void uhOh() {
                    int y;
                    String x = "hello";
                    y = x;
                }
            }
            """;
            System.out.println();


            System.out.println("TEST CASE I AM LOOKING FOR");
            SymbolTableVisitor visitor = getVisitorFor(code);
            System.out.println("FIN");
        assertFalse(visitor.getErrors().isEmpty(), "Expected error for type mismatch between x and y.");
    }
    
    @Test
    public void testReturnTypeMismatch() {
        String code = """
            class ReturnTypeMismatch {
                int uhOh() {
                    String x = "hello";
                    return x;
                }
            }
            """;
            System.out.println();


            System.out.println("TEST CASE I AM LOOKING FOR");
            SymbolTableVisitor visitor = getVisitorFor(code);
            System.out.println("FIN");
        assertFalse(visitor.getErrors().isEmpty(), "Expected error for type mismatch between expected return type and x.");
    }
    
    @Test
    public void testUseBeforeInitialization() {
        String code = """
            class Uninit {
                void fail() {
                    int z;
                    int x = z + 1; // z is declared but not initialized
                }
            }
            """;
        SymbolTableVisitor visitor = getVisitorFor(code);
        assertFalse(visitor.getErrors().isEmpty(), "Expected error for using variable before initialization.");
    }
    
    @Test
    public void testMultipleParameters() {
        String code = """
            class ParamTest {
                void add(int a, int b) {
                    int sum = a + b;
                }
            }
            """;
        SymbolTableVisitor visitor = getVisitorFor(code);
        assertTrue(visitor.getErrors().isEmpty(), "Expected no error with multiple parameters.");
    }
    
}

