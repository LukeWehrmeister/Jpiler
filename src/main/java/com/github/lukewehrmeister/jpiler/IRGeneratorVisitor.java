package com.github.lukewehrmeister.jpiler;

import java.util.*;

import org.antlr.v4.runtime.ParserRuleContext;

public class IRGeneratorVisitor extends JavaSubsetBaseVisitor<String> {
    private StringBuilder ir = new StringBuilder();
    private int tempVarCounter = 0;
    private int labelCounter = 0;
    private final Map<ParserRuleContext, SymbolTable> tableForContext;
    private final Map<ParserRuleContext, Symbol> symbolMap;




    // ===============================================================================================================
    //                                               Helper Functions
    // ===============================================================================================================
    
    public IRGeneratorVisitor(Map<ParserRuleContext, SymbolTable> tableForContext,
                       Map<ParserRuleContext, Symbol> symbolMap) {
        this.tableForContext = tableForContext;
        this.symbolMap = symbolMap;
    }

    //java type to LLVM type
    private static final Map<String, String> typeMapping = Map.of(
        "byte", "i8",
        "short", "i16",
        "int", "i32",
        "long", "i64",
        "float", "float",
        "double", "double",
        "boolean", "i1",
        "char", "i8",
        "String", "i8*",
        "void", "void" 
    );

    private void emitAllocasForScope(ParserRuleContext ctx) {
        SymbolTable scope = tableForContext.get(ctx);
        if (scope != null) {
            for (Symbol symbol : scope.getSymbols().values()) {
                if (symbol.getKind() == SymbolKind.VARIABLE) {
                    String llvmType = mapJavaTypeToLLVM(symbol.getType().name().toLowerCase());
                    ir.append("  %" + symbol.getName() + " = alloca " + llvmType + "\n");
                }
            }
        }
    }    

    private String mapJavaTypeToLLVM(String javaType) {
        if (!typeMapping.containsKey(javaType)) {
            throw new IllegalArgumentException("Unknown Java type: " + javaType);
        }
        return typeMapping.get(javaType);
    }

    public String getIR() {
        return ir.toString();
    }

    private String getUniqueTempVar() {
        return "%t" + (tempVarCounter++);
    }

    private String getUniqueLabel(String base) {
        return base + "." + (labelCounter++);
    }

    // ===============================================================================================================
    //                                              Visitor Functions
    // ===============================================================================================================

    @Override
    public String visitCompilationUnit(JavaSubsetParser.CompilationUnitContext ctx) {
        for (JavaSubsetParser.ClassDeclarationContext classDecl : ctx.classDeclaration()) {
            visit(classDecl);
        }
        return null;
    }

    @Override
    public String visitClassDeclaration(JavaSubsetParser.ClassDeclarationContext ctx) {
        String className = ctx.IDENTIFIER().getText();
        ir.append("; === Class " + className + " ===\n");
        
        visit(ctx.classBody()); 
        return null;
    }    
    
    @Override
    public String visitClassBody(JavaSubsetParser.ClassBodyContext ctx) {
        for (JavaSubsetParser.ClassBodyDeclarationContext decl : ctx.classBodyDeclaration()) {
            visit(decl);
        }
        return null;
    }

    @Override
    public String visitClassBodyDeclaration(JavaSubsetParser.ClassBodyDeclarationContext ctx) {
        if (ctx.methodDeclaration() != null) {
            visit(ctx.methodDeclaration());
        } else if (ctx.variableDeclaration() != null) {
            visit(ctx.variableDeclaration());
        }
        return null;
    }
    
    @Override
    public String visitStatement(JavaSubsetParser.StatementContext ctx) {
        if (ctx.variableDeclaration() != null) {
            visit(ctx.variableDeclaration());
        } else if (ctx.assignment() != null) {
            visit(ctx.assignment());
        } else if (ctx.expressionStatement() != null) {
            visit(ctx.expressionStatement());
        } else if (ctx.controlStructure() != null) {
            visit(ctx.controlStructure());
        } else if (ctx.block() != null) {
            visit(ctx.block());
        } else if (ctx.returnStatement() != null) {
            visit(ctx.returnStatement());
        }
        return null;
    }
    
    @Override
    public String visitMethodDeclaration(JavaSubsetParser.MethodDeclarationContext ctx) {
        Symbol methodSymbol = symbolMap.get(ctx);
        String methodName = methodSymbol.getName();
        String returnType = mapJavaTypeToLLVM(methodSymbol.getType().name().toLowerCase());

        List<String> paramList = new ArrayList<>();
        if (ctx.parameterList() != null) {
            for (JavaSubsetParser.ParameterContext paramCtx : ctx.parameterList().parameter()) {
                String paramName = paramCtx.IDENTIFIER().getText();
                SemanticType paramType = SemanticType.fromString(paramCtx.type().getText());
                String llvmType = mapJavaTypeToLLVM(paramType.name().toLowerCase());
                paramList.add(llvmType + " %" + paramName);
            }
        }
    
        ir.append("define " + returnType + " @" + methodName + "(" + String.join(", ", paramList) + ") {\n");
    
        if (ctx.parameterList() != null) {
            for (JavaSubsetParser.ParameterContext paramCtx : ctx.parameterList().parameter()) {
                Symbol paramSymbol = symbolMap.get(paramCtx);
                if (paramSymbol == null) continue;
    
                String llvmType = mapJavaTypeToLLVM(paramSymbol.getType().name().toLowerCase());
                String name = paramSymbol.getName();
                ir.append("  %" + name + ".addr = alloca " + llvmType + "\n");
                ir.append("  store " + llvmType + " %" + name + ", " + llvmType + "* %" + name + ".addr\n");
            }
        }
    
        visit(ctx.block());
        ir.append("}\n\n");
        return null;
    }    
    
    @Override
    public String visitVariableDeclarationExpression(JavaSubsetParser.VariableDeclarationExpressionContext ctx) {
        for (JavaSubsetParser.VariableDeclaratorContext declCtx : ctx.variableDeclarators().variableDeclarator()) {
            Symbol symbol = symbolMap.get(declCtx);
            if (symbol == null) continue;
    
            if (declCtx.expression() != null) {
                String rhs = visit(declCtx.expression());
                String llvmType = mapJavaTypeToLLVM(symbol.getType().name().toLowerCase());
                ir.append("  store " + llvmType + " " + rhs + ", " + llvmType + "* %" + symbol.getName() + "\n");
            }
        }
    
        return null;
    }    
    
    @Override
    public String visitVariableDeclaration(JavaSubsetParser.VariableDeclarationContext ctx) {
        for (JavaSubsetParser.VariableDeclaratorContext declCtx : ctx.variableDeclarators().variableDeclarator()) {
            Symbol symbol = symbolMap.get(declCtx);
            if (symbol == null) continue;
    
            if (declCtx.expression() != null) {
                String rhs = visit(declCtx.expression());
                String llvmType = mapJavaTypeToLLVM(symbol.getType().name().toLowerCase());
                ir.append("  store " + llvmType + " " + rhs + ", " + llvmType + "* %" + symbol.getName() + "\n");
            }
        }
    
        return null;
    }
     
    @Override
    public String visitAssignment(JavaSubsetParser.AssignmentContext ctx) {
        String varName = ctx.IDENTIFIER().getText();
        Symbol symbol = symbolMap.get(ctx);
    
        if (symbol == null) {
            System.err.println("Undeclared variable: " + varName);
            return "0";
        }
    
        String llvmType = mapJavaTypeToLLVM(symbol.getType().name().toLowerCase());
        String rhsValue = visit(ctx.expression());
    
        ir.append("  store " + llvmType + " " + rhsValue + ", " + llvmType + "* %" + varName + "\n");
    
        return rhsValue;
    }
    
    @Override
    public String visitExpression(JavaSubsetParser.ExpressionContext ctx) {
        if (ctx.assignment() != null) {
            return visit(ctx.assignment());
        } else {
            return visit(ctx.logicalOrExpression());
        }
    }
    
    @Override
    public String visitLogicalOrExpression(JavaSubsetParser.LogicalOrExpressionContext ctx) {
        String result = visit(ctx.logicalAndExpression(0));
        for (int i = 1; i < ctx.logicalAndExpression().size(); i++) {
            String right = visit(ctx.logicalAndExpression(i));
            String temp = getUniqueTempVar();
            ir.append("  " + temp + " = or i1 " + result + ", " + right + "\n");
            result = temp;
        }
        return result;
    }
    
    @Override
    public String visitLogicalAndExpression(JavaSubsetParser.LogicalAndExpressionContext ctx) {
        String result = visit(ctx.equalityExpression(0));
        for (int i = 1; i < ctx.equalityExpression().size(); i++) {
            String right = visit(ctx.equalityExpression(i));
            String temp = getUniqueTempVar();
            ir.append("  " + temp + " = and i1 " + result + ", " + right + "\n");
            result = temp;
        }
        return result;
    }
    
    @Override
    public String visitEqualityExpression(JavaSubsetParser.EqualityExpressionContext ctx) {
        String left = visit(ctx.relationalExpression(0));
        for (int i = 1; i < ctx.relationalExpression().size(); i++) {
            String right = visit(ctx.relationalExpression(i));
            String op = ctx.equalityOperator(i - 1).getText();
            String temp = getUniqueTempVar();
            String llvmOp = op.equals("==") ? "icmp eq" : "icmp ne";
            ir.append("  " + temp + " = " + llvmOp + " i32 " + left + ", " + right + "\n");
            left = temp;
        }
        return left;
    }
    
    @Override
    public String visitRelationalExpression(JavaSubsetParser.RelationalExpressionContext ctx) {
        String left = visit(ctx.additiveExpression(0));
        for (int i = 1; i < ctx.additiveExpression().size(); i++) {
            String right = visit(ctx.additiveExpression(i));
            String op = ctx.relationalOperator(i - 1).getText();
            String llvmOp = switch (op) {
                case "<" -> "icmp slt";
                case "<=" -> "icmp sle";
                case ">" -> "icmp sgt";
                case ">=" -> "icmp sge";
                default -> throw new RuntimeException("Unknown relational operator: " + op);
            };
            String temp = getUniqueTempVar();
            ir.append("  " + temp + " = " + llvmOp + " i32 " + left + ", " + right + "\n");
            left = temp;
        }
        return left;
    }
    
    @Override
    public String visitAdditiveExpression(JavaSubsetParser.AdditiveExpressionContext ctx) {
        String result = visit(ctx.multiplicativeExpression(0));
        for (int i = 1; i < ctx.multiplicativeExpression().size(); i++) {
            String right = visit(ctx.multiplicativeExpression(i));
            String op = ctx.additiveOperator(i - 1).getText();
            String llvmOp = op.equals("+") ? "add" : "sub";
            String temp = getUniqueTempVar();
            ir.append("  " + temp + " = " + llvmOp + " i32 " + result + ", " + right + "\n");
            result = temp;
        }
        return result;
    }
    
    @Override
    public String visitMultiplicativeExpression(JavaSubsetParser.MultiplicativeExpressionContext ctx) {
        String result = visit(ctx.unaryExpression(0));
        for (int i = 1; i < ctx.unaryExpression().size(); i++) {
            String right = visit(ctx.unaryExpression(i));
            String op = ctx.multiplicativeOperator(i - 1).getText();
            String llvmOp = switch (op) {
                case "*" -> "mul";
                case "/" -> "sdiv";
                case "%" -> "srem";
                default -> throw new RuntimeException("Unknown operator: " + op);
            };
            String temp = getUniqueTempVar();
            ir.append("  " + temp + " = " + llvmOp + " i32 " + result + ", " + right + "\n");
            result = temp;
        }
        return result;
    }
    
    @Override
    public String visitUnaryExpression(JavaSubsetParser.UnaryExpressionContext ctx) {
        if (ctx.unaryOperator() != null) {
            String operand = visit(ctx.unaryExpression());
            String op = ctx.unaryOperator().getText();
            String temp = getUniqueTempVar();
            switch (op) {
                case "-" -> ir.append("  " + temp + " = sub i32 0, " + operand + "\n");
                case "+" -> {
                    return operand; 
                }
                case "!" -> ir.append("  " + temp + " = xor i1 " + operand + ", true" + "\n");
                case "++" -> {
                    ir.append("  " + temp + " = add i32 " + operand + ", 1" + "\n");
                    return temp;
                }
                case "--" -> {
                    ir.append("  " + temp + " = sub i32 " + operand + ", 1" + "\n");
                    return temp;
                }
                default -> throw new RuntimeException("Unknown unary op: " + op);
            }
            return temp;
        } else {
            return visit(ctx.postfixExpression());
        }
    }
    
    
    @Override
    public String visitPostfixExpression(JavaSubsetParser.PostfixExpressionContext ctx) {
        if (ctx.postfixOperator().isEmpty()) {
            return visit(ctx.primaryExpression());
        }
    
        if (ctx.primaryExpression().IDENTIFIER() == null) {
            throw new RuntimeException("Postfix operators can only be applied to variables.");
        }
    
        String varName = ctx.primaryExpression().IDENTIFIER().getText();
        String varPtr = "%" + varName;
    
        String original = getUniqueTempVar();
        ir.append("  " + original + " = load i32, i32* " + varPtr + "\n");
    
        String currentValue = original;
        for (JavaSubsetParser.PostfixOperatorContext opCtx : ctx.postfixOperator()) {
            String op = opCtx.getText();
            String updated = getUniqueTempVar();
    
            switch (op) {
                case "++" -> {
                    ir.append("  " + updated + " = add i32 " + currentValue + ", 1\n");
                    ir.append("  store i32 " + updated + ", i32* " + varPtr + "\n");
                    currentValue = updated;
                }
                case "--" -> {
                    ir.append("  " + updated + " = sub i32 " + currentValue + ", 1\n");
                    ir.append("  store i32 " + updated + ", i32* " + varPtr + "\n");
                    currentValue = updated;
                }
                default -> throw new RuntimeException("Unknown postfix operator: " + op);
            }
        }
    
        return original; 
    }
    
    
    

    @Override
    public String visitPrimaryExpression(JavaSubsetParser.PrimaryExpressionContext ctx) {
        System.out.println("IR Primary: " + ctx.getText());
        System.out.println("  integerLiteral: " + ctx.integerLiteral());
        System.out.println("  IDENTIFIER: " + ctx.IDENTIFIER());
        
        if (ctx.integerLiteral() != null) {
            return ctx.integerLiteral().getText();
        }
    
        if (ctx.STRINGLIT() != null || ctx.CHARACTER() != null || ctx.NULL_LITERAL() != null) {
            return "0";
        }
    
        if (ctx.booleanLiteral() != null) {
            return ctx.booleanLiteral().getText().equals("true") ? "1" : "0";
        }
    
        if (ctx.IDENTIFIER() != null) {
            String varName = ctx.IDENTIFIER().getText();
            Symbol symbol = symbolMap.get(ctx);
            if (symbol == null) {
                System.err.println("Undeclared variable " + varName);
                return "0";
            }
    
            String result = getUniqueTempVar();
            String llvmType = mapJavaTypeToLLVM(symbol.getType().name().toLowerCase());String varPtr = "%" + varName;
            if (symbol.getKind() == SymbolKind.PARAMETER) {
                varPtr += ".addr";
            }
            ir.append("  " + result + " = load " + llvmType + ", " + llvmType + "* " + varPtr + "\n");
            return result;
        }
    
        if (ctx.expression() != null) {
            return visit(ctx.expression());
        }
    
        return "0"; 
    }
    
    @Override
    public String visitControlStructure(JavaSubsetParser.ControlStructureContext ctx) {
        if (ctx.ifStatement() != null) return visit(ctx.ifStatement());
        if (ctx.whileLoop() != null) return visit(ctx.whileLoop());
        if (ctx.forLoop() != null) return visit(ctx.forLoop());
        if (ctx.doWhileLoop() != null) return visit(ctx.doWhileLoop());
        return null;
    }

    @Override
    public String visitIfStatement(JavaSubsetParser.IfStatementContext ctx) {
        String cond = visit(ctx.expression()); 
        String thenLabel = getUniqueLabel("then");
        String elseLabel = ctx.ELSE() != null ? getUniqueLabel("else") : null;
        String endLabel = getUniqueLabel("endif");
    
        if (elseLabel != null) {
            ir.append("  br i1 " + cond + ", label %" + thenLabel + ", label %" + elseLabel + "\n");
        } else {
            ir.append("  br i1 " + cond + ", label %" + thenLabel + ", label %" + endLabel + "\n");
        }
    
        ir.append(thenLabel + ":\n");
        visit(ctx.block(0));
        ir.append("  br label %" + endLabel + "\n");
    
        if (ctx.ELSE() != null) {
            ir.append(elseLabel + ":\n");
            if (ctx.ifStatement() != null) {
                visit(ctx.ifStatement());
            } else {
                visit(ctx.block(1));
            }
            ir.append("  br label %" + endLabel + "\n");
        }
    
        ir.append(endLabel + ":\n");
        return null;
    }
    
    @Override
    public String visitWhileLoop(JavaSubsetParser.WhileLoopContext ctx) {
        String condLabel = getUniqueLabel("while.cond");
        String bodyLabel = getUniqueLabel("while.body");
        String endLabel = getUniqueLabel("while.end");
    
        ir.append("  br label %" + condLabel + "\n");
    
        ir.append(condLabel + ":\n");
        String cond = visit(ctx.expression());
        ir.append("  br i1 " + cond + ", label %" + bodyLabel + ", label %" + endLabel + "\n");
    
        ir.append(bodyLabel + ":\n");
        visit(ctx.block());
        ir.append("  br label %" + condLabel + "\n");
    
        ir.append(endLabel + ":\n");
        return null;
    }
    
    @Override
    public String visitDoWhileLoop(JavaSubsetParser.DoWhileLoopContext ctx) {
        String bodyLabel = getUniqueLabel("do.body");
        String condLabel = getUniqueLabel("do.cond");
        String endLabel = getUniqueLabel("do.end");
    
        ir.append("  br label %" + bodyLabel + "\n");
    
        ir.append(bodyLabel + ":\n");
        visit(ctx.block());
        ir.append("  br label %" + condLabel + "\n");
    
        ir.append(condLabel + ":\n");
        String cond = visit(ctx.expression());
        ir.append("  br i1 " + cond + ", label %" + bodyLabel + ", label %" + endLabel + "\n");
    
        ir.append(endLabel + ":\n");
        return null;
    }
    
    @Override
    public String visitForLoop(JavaSubsetParser.ForLoopContext ctx) {
        emitAllocasForScope(ctx);
        if (ctx.variableDeclarationExpression() != null) {
            visit(ctx.variableDeclarationExpression());
        } else if (ctx.assignmentExpressionList() != null) {
            visit(ctx.assignmentExpressionList());
        }
    
        String condLabel = getUniqueLabel("for.cond");
        String bodyLabel = getUniqueLabel("for.body");
        String updateLabel = getUniqueLabel("for.update");
        String endLabel = getUniqueLabel("for.end");
    
        ir.append("  br label %" + condLabel + "\n");
    
        ir.append(condLabel + ":\n");
        if (ctx.expression() != null) {
            String cond = visit(ctx.expression());
            ir.append("  br i1 " + cond + ", label %" + bodyLabel + ", label %" + endLabel + "\n");
        } else {
            ir.append("  br label %" + bodyLabel + "\n");
        }
    
        ir.append(bodyLabel + ":\n");
        visit(ctx.block());
        ir.append("  br label %" + updateLabel + "\n");
    
        ir.append(updateLabel + ":\n");
        if (ctx.updateExpressionList() != null) {
            visit(ctx.updateExpressionList());
        }
        ir.append("  br label %" + condLabel + "\n");
    
        ir.append(endLabel + ":\n");
        return null;
    }

    @Override
    public String visitAssignmentExpressionList(JavaSubsetParser.AssignmentExpressionListContext ctx) {
        for (JavaSubsetParser.AssignmentContext a : ctx.assignment()) {
            visit(a);
        }
        return null;
    }
    
    @Override
    public String visitUpdateExpressionList(JavaSubsetParser.UpdateExpressionListContext ctx) {
        for (JavaSubsetParser.UpdateExpressionContext e : ctx.updateExpression()) {
            visit(e);
        }
        return null;
    }

    @Override
    public String visitUpdateExpression(JavaSubsetParser.UpdateExpressionContext ctx) {
        if (ctx.assignment() != null) return visit(ctx.assignment());
        if (ctx.unaryExpression() != null) return visit(ctx.unaryExpression());
        if (ctx.postfixExpression() != null) return visit(ctx.postfixExpression());
        return null;
    }
    
    @Override
    public String visitBlock(JavaSubsetParser.BlockContext ctx) {
        emitAllocasForScope(ctx);
    
        for (JavaSubsetParser.StatementContext stmtCtx : ctx.statement()) {
            visit(stmtCtx);
        }
    
        return null;
    }
    
    
    @Override
    public String visitReturnStatement(JavaSubsetParser.ReturnStatementContext ctx) {
        if (ctx.expression() != null) {
            String returnValue = visit(ctx.expression());
    
            ParserRuleContext methodCtx = ctx;
            while (methodCtx != null && !(methodCtx instanceof JavaSubsetParser.MethodDeclarationContext)) {
                methodCtx = methodCtx.getParent();
            }
    
            if (methodCtx != null) {
                Symbol methodSymbol = symbolMap.get(methodCtx);
                if (methodSymbol != null) {
                    String llvmType = mapJavaTypeToLLVM(methodSymbol.getType().name().toLowerCase());
                    ir.append("  ret " + llvmType + " " + returnValue + "\n");
                    return null;
                }
            }
    
            ir.append("  ret i32 " + returnValue + "\n");
        } else {
            ir.append("  ret void\n");
        }
    
        return null;
    }
    

}