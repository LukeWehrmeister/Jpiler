package com.github.lukewehrmeister.jpiler;

import java.util.*;

import org.antlr.v4.runtime.ParserRuleContext;


public class SymbolTableVisitor extends JavaSubsetBaseVisitor<SemanticType>{
    private Stack<SymbolTable> scopes = new Stack<>();
    private List<String> errors = new ArrayList<>();
    private Map<ParserRuleContext, SymbolTable> tableForContext = new HashMap<>();
    private Map<ParserRuleContext, Symbol> symbolMap = new HashMap<>();
    private Deque<SemanticType> returnTypeStack = new ArrayDeque<>();


    public SymbolTable getSymbolTableForContext(ParserRuleContext ctx) {
        return tableForContext.get(ctx); 
    }

    public Symbol getSymbolForContext(ParserRuleContext ctx) {
        return symbolMap.get(ctx);  
    }


    private void addSymbol(String name, Symbol symbol) {
        if (scopes.isEmpty()) {
            throw new IllegalStateException("No active scope to add symbol '" + name + "'.");
        }
        SymbolTable scope = scopes.peek();
        if (scope.contains(name)) {
            reportError("Symbol '" + name + "' already declared in this scope.");
        } else {
            scope.add(name, symbol);
        }
    }

    public Map<ParserRuleContext, SymbolTable> getTableForContext() { 
        return tableForContext; 
    }
    
    public Map<ParserRuleContext, Symbol> getSymbolMap() { 
        return symbolMap; 
    }

    public List<String> getErrors() { 
        return errors; 
    }

    private Symbol resolveSymbol(String name) {
        for (int i = scopes.size() - 1; i >= 0; --i) {
            Symbol sym = scopes.get(i).getSymbols().get(name);
            if (sym != null) {
                return sym;
            }
        }
        return null;
    }
    

    private void reportError(String msg) {
        errors.add(msg);
        System.err.println("Semantic Error: " + msg);
    }
    
    public void printSymbolTables() {
        int level = 0;
        for (SymbolTable table : scopes) {
            System.out.println("Scope level " + level + ":");
            for (String name : table.getSymbols().keySet()) {
                Symbol symbol = table.get(name);
                System.out.println("  " + name + " : " + symbol.getType());
            }
            level++;
        }
    }



    @Override
    public SemanticType visitCompilationUnit(JavaSubsetParser.CompilationUnitContext ctx) {
        SymbolTable global = new SymbolTable();
        scopes.push(global);
        tableForContext.put(ctx, global);

        for (JavaSubsetParser.ClassDeclarationContext classCtx : ctx.classDeclaration()) {
            visit(classCtx);
        }
        scopes.pop();
        return null;
    }
    
    @Override
    public SemanticType visitClassDeclaration(JavaSubsetParser.ClassDeclarationContext ctx) {
        String className = ctx.IDENTIFIER().getText();
        String accessModifier = ctx.accessModifier() != null
        ? ctx.accessModifier().getText()
        : "default";
        
        Symbol classSymbol = new Symbol(className, SemanticType.CLASS, SymbolKind.CLASS, accessModifier);
        addSymbol(className, classSymbol);
        symbolMap.put(ctx, classSymbol);

        SymbolTable classScope = new SymbolTable();
        scopes.push(classScope);
        tableForContext.put(ctx, classScope);

        visit(ctx.classBody());
        scopes.pop(); 

        return null;
    }


    @Override
    public SemanticType visitClassBody(JavaSubsetParser.ClassBodyContext ctx) {
        for (JavaSubsetParser.ClassBodyDeclarationContext declCtx : ctx.classBodyDeclaration()) {
            visit(declCtx);
        }
        return null;
    }
    
    @Override
    public SemanticType visitClassBodyDeclaration(JavaSubsetParser.ClassBodyDeclarationContext ctx) {
        if (ctx.methodDeclaration() != null) {
            visit(ctx.methodDeclaration());
        } else if (ctx.variableDeclaration() != null) {
            visit(ctx.variableDeclaration());
        }
        return null;
    }

    @Override
    public SemanticType visitMethodDeclaration(JavaSubsetParser.MethodDeclarationContext ctx) {
        SemanticType expectedReturnType = SemanticType.fromString(ctx.returnType().getText());
        returnTypeStack.push(expectedReturnType);
        
        try {
            String methodName = ctx.IDENTIFIER().getText();
       
            String accessModifier = ctx.accessModifier() != null ? ctx.accessModifier().getText() : "default";

            Symbol methodSymbol = new Symbol(methodName, expectedReturnType, SymbolKind.METHOD, accessModifier);
            addSymbol(methodName, methodSymbol);
            symbolMap.put(ctx, methodSymbol);

            SymbolTable methodScope = new SymbolTable();
            scopes.push(methodScope);
            tableForContext.put(ctx, methodScope);
        
            if (ctx.parameterList() != null) {
                visit(ctx.parameterList());
            }
            if (ctx.block() != null) {
                visit(ctx.block());
            }
            scopes.pop();
        } finally {
            returnTypeStack.pop();
        }
        return null;
    }

    @Override
    public SemanticType visitParameterList(JavaSubsetParser.ParameterListContext ctx) {
        for (JavaSubsetParser.ParameterContext paramCtx : ctx.parameter()) {
            visit(paramCtx);
        }
        return null;
    }    

    @Override
    public SemanticType visitParameter(JavaSubsetParser.ParameterContext ctx) {
        String paramName = ctx.IDENTIFIER().getText();
        SemanticType paramType = SemanticType.fromString(ctx.type().getText());
        
        Symbol paramSymbol = new Symbol(paramName, paramType, SymbolKind.PARAMETER, true);
        addSymbol(paramName, paramSymbol);
        symbolMap.put(ctx, paramSymbol);

        return null;
    }    

    @Override
    public SemanticType visitVariableDeclaration(JavaSubsetParser.VariableDeclarationContext ctx) {
        String varTypeString = ctx.type().getText();
        SemanticType varType = SemanticType.fromString(varTypeString);
    
        for (JavaSubsetParser.VariableDeclaratorContext declaratorCtx : ctx.variableDeclarators().variableDeclarator()) {
            String varName = declaratorCtx.IDENTIFIER().getText(); 
            Symbol existingSymbol = resolveSymbol(varName);

            if (existingSymbol != null) {
                reportError("Variable '" + varName + "' already defined in the current scope.");
            } else {
                boolean initialized = declaratorCtx.expression() != null;
                
                Symbol varSymbol = new Symbol(varName, varType, SymbolKind.VARIABLE, initialized);
                addSymbol(varName, varSymbol);
                symbolMap.put(declaratorCtx, varSymbol);
                
                if (initialized) {
                    visit(declaratorCtx.expression());
                }
            }
        }
        return null;
    }
    
    @Override
    public SemanticType visitVariableDeclarationExpression(JavaSubsetParser.VariableDeclarationExpressionContext ctx) {
        String varTypeString = ctx.type().getText();
        SemanticType varType = SemanticType.fromString(varTypeString);

        for (JavaSubsetParser.VariableDeclaratorContext declaratorCtx : ctx.variableDeclarators().variableDeclarator()) {
            String varName = declaratorCtx.IDENTIFIER().getText(); 
            Symbol existingSymbol = resolveSymbol(varName);
    
            if (existingSymbol != null) {
                reportError("Variable '" + varName + "' already defined in the current scope.");
            } else {
                boolean initialized = declaratorCtx.expression() != null;
                
                Symbol varSymbol = new Symbol(varName, varType, SymbolKind.VARIABLE, initialized);
                addSymbol(varName, varSymbol);
                symbolMap.put(declaratorCtx, varSymbol);
                
                if (initialized) {
                    visit(declaratorCtx.expression());
                }
            }
        }
        return null;
    }

    @Override
    public SemanticType visitAssignment(JavaSubsetParser.AssignmentContext ctx) {
        String name = ctx.IDENTIFIER().getText();
        Symbol symbol = resolveSymbol(name);
    
        if (symbol == null) {
            reportError("Variable '" + name + "' not declared in any accessible scope.");
        } else {
            SemanticType rhsType = visit(ctx.expression());
            SemanticType lhsType = symbol.getType();
            
            if (!lhsType.equals(rhsType)) {
                reportError("Type mismatch in assignment to '" + name + "': cannot assign " + rhsType + " to " + lhsType + ".");
            }
            symbol.setInitialized(true);
            symbolMap.put(ctx, symbol);
        }
    
        return null;
    }

    @Override
    public SemanticType visitExpression(JavaSubsetParser.ExpressionContext ctx) {
        if (ctx.assignment() != null) {
            return visit(ctx.assignment());
        } else {
            return visit(ctx.logicalOrExpression());
        }
    }

    @Override
    public SemanticType visitLogicalOrExpression(JavaSubsetParser.LogicalOrExpressionContext ctx) {
        SemanticType type = visit(ctx.logicalAndExpression(0));
    
        if (ctx.logicalAndExpression().size() == 1) {
            return type; 
        }
        for (int i = 1; i < ctx.logicalAndExpression().size(); i++) {
            SemanticType right = visit(ctx.logicalAndExpression(i));
            if (type != SemanticType.BOOLEAN || right != SemanticType.BOOLEAN) {
                reportError("Logical OR requires boolean operands.");
                return SemanticType.UNKNOWN;
            }
        }
        return SemanticType.BOOLEAN;
    }
    

    @Override
    public SemanticType visitLogicalAndExpression(JavaSubsetParser.LogicalAndExpressionContext ctx) {
        SemanticType type = visit(ctx.equalityExpression(0));
    
        if (ctx.equalityExpression().size() == 1) {
            return type; 
        }
        for (int i = 1; i < ctx.equalityExpression().size(); i++) {
            SemanticType right = visit(ctx.equalityExpression(i));
            if (type != SemanticType.BOOLEAN || right != SemanticType.BOOLEAN) {
                reportError("Logical AND requires boolean operands.");
                return SemanticType.UNKNOWN;
            }
        }
    
        return SemanticType.BOOLEAN; 
    }
    

    @Override
    public SemanticType visitEqualityExpression(JavaSubsetParser.EqualityExpressionContext ctx) {
        SemanticType type = visit(ctx.relationalExpression(0));
        System.out.println("Equality: " + ctx.getText() + " → " + type);
        if (ctx.relationalExpression().size() == 1) {
            return type; 
        }
    
        for (int i = 1; i < ctx.relationalExpression().size(); i++) {
            SemanticType right = visit(ctx.relationalExpression(i));
            if (!type.equals(right)) {
                reportError("Equality operator applied to mismatched types.");
                return SemanticType.UNKNOWN;
            }
        }
        return SemanticType.BOOLEAN;
    }
    

    @Override
    public SemanticType visitRelationalExpression(JavaSubsetParser.RelationalExpressionContext ctx) {
        SemanticType type = visit(ctx.additiveExpression(0));
        if (ctx.additiveExpression().size() == 1) {
            return type;
        }
        for (int i = 1; i < ctx.additiveExpression().size(); i++) {
            SemanticType right = visit(ctx.additiveExpression(i));
            if (type != right || (type != SemanticType.INT && type != SemanticType.CHAR && type != SemanticType.STRING)) {
                reportError("Invalid types for relational operator.");
                return SemanticType.UNKNOWN;
            }
        }
        return SemanticType.BOOLEAN;
    }
    

    @Override
    public SemanticType visitAdditiveExpression(JavaSubsetParser.AdditiveExpressionContext ctx) {
        SemanticType type = visit(ctx.multiplicativeExpression(0));
        System.out.println("ADDITIVE: " + ctx.getText() + " → " + type);
        for (int i = 1; i < ctx.multiplicativeExpression().size(); i++) {
            SemanticType right = visit(ctx.multiplicativeExpression(i));
            if (type == SemanticType.STRING || right == SemanticType.STRING) {
                type = SemanticType.STRING;
            } else if (type == SemanticType.INT && right == SemanticType.INT) {
                type = SemanticType.INT;
            } else {
                reportError("Invalid types for '+' or '-' operator.");
                return SemanticType.UNKNOWN;
            }
        }
        return type;
    }

    @Override
    public SemanticType visitMultiplicativeExpression(JavaSubsetParser.MultiplicativeExpressionContext ctx) {
        SemanticType type = visit(ctx.unaryExpression(0));
    
        if (ctx.unaryExpression().size() == 1) {
            return type; 
        }
    
        for (int i = 1; i < ctx.unaryExpression().size(); i++) {
            SemanticType right = visit(ctx.unaryExpression(i));
            if (type != SemanticType.INT || right != SemanticType.INT) {
                reportError("Multiplicative operators require int operands.");
                return SemanticType.UNKNOWN;
            }
        }
    
        return SemanticType.INT; 
    }
    

    @Override
    public SemanticType visitUnaryExpression(JavaSubsetParser.UnaryExpressionContext ctx) {
        SemanticType operandType;
        if (ctx.unaryOperator() != null) {
            operandType = visit(ctx.unaryExpression());
            System.out.println("UNARY: " + ctx.getText() + " → " + operandType);
            String op = ctx.unaryOperator().getText();
            return switch (op) {
                case "-", "+", "++", "--" -> operandType == SemanticType.INT ? SemanticType.INT : SemanticType.UNKNOWN;
                case "!" -> operandType == SemanticType.BOOLEAN ? SemanticType.BOOLEAN : SemanticType.UNKNOWN;
                default -> {
                    reportError("Unknown unary operator: " + op);
                    yield SemanticType.UNKNOWN;
                }
            };
        } else {
            operandType = visit(ctx.postfixExpression());
            System.out.println("UNARY: " + ctx.getText() + " → " + operandType);
            return operandType;
        }
    }    
    
    @Override
    public SemanticType visitPostfixExpression(JavaSubsetParser.PostfixExpressionContext ctx) {
        if (!ctx.postfixOperator().isEmpty()) {
            return handleIncrementOrDecrement(ctx.primaryExpression());
        }
        return visit(ctx.primaryExpression());
    }
    

    private SemanticType handleIncrementOrDecrement(JavaSubsetParser.PrimaryExpressionContext ctx) {
        if (ctx.IDENTIFIER() != null) {
            String name = ctx.IDENTIFIER().getText();
            Symbol symbol = resolveSymbol(name);
            if (symbol == null) {
                reportError("Variable '" + name + "' is not declared.");
                return SemanticType.UNKNOWN;
            }
            if (!symbol.getInitialized()) {
                reportError("Variable '" + name + "' is used before initialization.");
                return SemanticType.UNKNOWN;
            }
    
            if (symbol.getType() != SemanticType.INT) {
                reportError("Increment/decrement is only supported on int types.");
                return SemanticType.UNKNOWN;
            }
    
            symbolMap.put(ctx, symbol);
            return SemanticType.INT;
        }
    
        return SemanticType.UNKNOWN;
    }    

    @Override
    public SemanticType visitPrimaryExpression(JavaSubsetParser.PrimaryExpressionContext ctx) {
        System.out.println("PRIMARY: " + ctx.getText());

        
        if (ctx.integerLiteral() != null) {
            System.out.println("PrimaryExpression marks as int");
            return SemanticType.INT;
        } else if (ctx.STRINGLIT() != null) {
            return SemanticType.STRING;  
        } else if (ctx.CHARACTER() != null) {
            return SemanticType.CHAR;
        } else if (ctx.booleanLiteral() != null) {
            return SemanticType.BOOLEAN;  
        } else if (ctx.NULL_LITERAL() != null) {
            return SemanticType.NULL;  
        } else if (ctx.IDENTIFIER() != null) {

            String name = ctx.IDENTIFIER().getText();
            Symbol symbol = resolveSymbol(name);

            if (symbol == null) {
                reportError("Variable '" + name + "' is not declared.");
                return SemanticType.UNKNOWN;
            } else if (!symbol.getInitialized()) {
                reportError("Variable '" + name + "' is used before being initialized.");
                return SemanticType.UNKNOWN;
            } else {
                symbolMap.put(ctx, symbol);
                return symbol.getType();
            }
        } else if (ctx.expression() != null) {
            return visit(ctx.expression());
        }
        return SemanticType.UNKNOWN;
    }

    @Override
    public SemanticType visitUpdateExpressionList(JavaSubsetParser.UpdateExpressionListContext ctx) {
        for (JavaSubsetParser.UpdateExpressionContext exprCtx : ctx.updateExpression()) {
            visit(exprCtx);
        }
        return null;
    }
    
    @Override
    public SemanticType visitUpdateExpression(JavaSubsetParser.UpdateExpressionContext ctx) {
        if (ctx.assignment() != null) {
            return visit(ctx.assignment());
        } else if (ctx.unaryExpression() != null) {
            return visit(ctx.unaryExpression());
        } else if (ctx.postfixExpression() != null) {
            return visit(ctx.postfixExpression());
        }
        return null;
    }

    

    @Override
    public SemanticType visitIfStatement(JavaSubsetParser.IfStatementContext ctx) {
        SemanticType conditionType = visit(ctx.expression());
        if (conditionType != SemanticType.BOOLEAN) {
            reportError("Condition in 'if' statement must be of type boolean, but found " + conditionType);
        }
    
        visit(ctx.block(0)); 
    
        if (ctx.ELSE() != null) {
            if (ctx.ifStatement() != null) {
                visit(ctx.ifStatement()); 
            } else {
                visit(ctx.block(1));
            }
        }
    
        return null;
    }
    
    
    @Override
    public SemanticType visitWhileLoop(JavaSubsetParser.WhileLoopContext ctx) {
        SemanticType conditionType = visit(ctx.expression());
        if (conditionType != SemanticType.BOOLEAN) {
            reportError("Condition in 'while' loop must be of type boolean, but found " + conditionType);
        }
        visit(ctx.block());
        return null;
    }
    
    
    @Override
    public SemanticType visitDoWhileLoop(JavaSubsetParser.DoWhileLoopContext ctx) {
        visit(ctx.block());
        SemanticType cond = visit(ctx.expression());
        if (cond != SemanticType.BOOLEAN) {
            reportError("Condition in 'do-while' must be boolean, but found " + cond);
        }
        return null;
    }

    @Override
    public SemanticType visitForLoop(JavaSubsetParser.ForLoopContext ctx) {
        SymbolTable forScope = new SymbolTable();
        scopes.push(forScope);
        tableForContext.put(ctx, forScope);
    
        if (ctx.variableDeclarationExpression() != null) {
            visit(ctx.variableDeclarationExpression());
        }
        if (ctx.expression() != null) {
            visit(ctx.expression());
        }
        if (ctx.updateExpressionList() != null) {
            visit(ctx.updateExpressionList());
        }
    
        visit(ctx.block());
        scopes.pop();
    
        return null;
    }

    @Override
    public SemanticType visitAssignmentExpressionList(JavaSubsetParser.AssignmentExpressionListContext ctx) {
        for (JavaSubsetParser.AssignmentContext assignmentCtx : ctx.assignment()) {
            visit(assignmentCtx);
        }
        return null;
    }

    @Override
    public SemanticType visitBlock(JavaSubsetParser.BlockContext ctx) {
        SymbolTable blockScope = new SymbolTable();
        scopes.push(blockScope);
        tableForContext.put(ctx, blockScope);

        for (JavaSubsetParser.StatementContext stmtCtx : ctx.statement()) {
            visit(stmtCtx);
        }

        scopes.pop();
        return null;
    }


    @Override
    public SemanticType visitReturnStatement(JavaSubsetParser.ReturnStatementContext ctx) {
        SemanticType expected = returnTypeStack.peek();
        if (expected == null) {
            reportError("‘return’ not inside a method");
            return SemanticType.UNKNOWN;
        }

        if (ctx.expression() != null) {
            SemanticType actual = visit(ctx.expression());

            if (expected == SemanticType.VOID) {
                reportError("Void method cannot return a value.");
                return SemanticType.UNKNOWN;
            } else if (actual != expected) {
                reportError("Method return type does not match declared return type.");
                return SemanticType.UNKNOWN;
            }
        } else {
            if (expected != SemanticType.VOID) {
                reportError("Non-void method must return a value.");
                return SemanticType.UNKNOWN;
            }
        }
    
        return expected;
    }
    
}
