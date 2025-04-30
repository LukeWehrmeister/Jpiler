grammar JavaSubset;

// =======================================================================================================================================
//                                                             Lexer Rules
// =======================================================================================================================================


STRINGLIT     : '"' (~["\\] | '\\' .)*? '"' ;
CHARACTER     : '\'' (~['\\] | '\\' .) '\'' ;

//Data Type Keywords
BYTE : 'byte' ;
SHORT : 'short' ;
INT : 'int' ;
LONG : 'long' ;
FLOAT : 'float' ;
DOUBLE : 'double' ;
BOOLEAN : 'boolean' ;
CHAR : 'char' ;
STRINGARGS : 'String[] args' ; //done short-term for simplicity
STRING : 'String' ;

//Conditional Keywords
IF : 'if' ;
ELSE : 'else' ;
FOR : 'for' ;
WHILE : 'while' ;
DO : 'do' ;

//More Keywords
RETURN : 'return' ;
CLASS : 'class' ;
PUBLIC : 'public' ;
PRIVATE : 'private' ;
PROTECTED : 'protected' ;
STATIC : 'static' ;
VOID : 'void' ;
IMPORT : 'import' ;
NEW : 'new' ;

//Two-Character
INCREMENT  : '++' ;
DECREMENT  : '--' ;
LE         : '<=' ;
GE         : '>=' ;
EQ         : '==' ;
NEQ        : '!=' ;
PLUSEQ     : '+=' ;
MINEQ      : '-=' ;
TIMESEQ     : '*=' ;
DIVEQ      : '/=' ;
MODULEQ    : '%=' ;
AND        : '&&' ;
OR         : '||' ;

// One Character
PLUS       : '+' ;
MINUS      : '-' ;
TIMES      : '*' ;
DIVIDE     : '/' ;
MODULUS    : '%' ;
LT         : '<' ;
GT         : '>' ;
NOT        : '!' ;
ASSIGN     : '=' ;


//Eventually change this, these are all classes that are basically acting as keywords for the time being
SIMPLE_KEYWORDS : 'Integer' | 'Double' | 'Float' | 'Character' | 'Boolean' | 'Byte' | 'Long' | 'Object' | 'Void' ;  //SIMPLE_KEYWORDS means keywords for simplicity's sake

//Integer Literals Involving Letters
HEX_LITERAL : '0' [xX] [0-9a-fA-F]+;
BINARY_LITERAL : '0' [bB] [01]+;

// Boolean Literals
TRUE : 'true';
FALSE : 'false';

// Null Literal
NULL_LITERAL : 'null';

INVALID_IDENTIFIER: [0-9]+[a-zA-Z_][a-zA-Z0-9_]* ; 
IDENTIFIER     : [a-zA-Z_][a-zA-Z0-9_]*;

// Integer Literals No Letters
DECIMAL_LITERAL : [1-9] [0-9]* | '0';
OCTAL_LITERAL : '0' [0-7]+;

// Floating-Point Literals
FLOAT_LITERAL : [0-9]+ '.' [0-9]* ([eE] [+-]? [0-9]+)? [fFdD]?;
EXPONENT_PART : [eE] [+-]? [0-9]+;




DOT           : '.' ;
LPAREN        : '(' ;
RPAREN        : ')' ;
LBRACE        : '{' ;
RBRACE        : '}' ;
SEMI          : ';' ;
COMMA         : ',' ;


COMMENT        : '//' ~[\r\n]* -> skip ;
MULTICOMMENT   : '/*' .*? '*/' -> skip ;
WHITESPACE     : [ \t\r\n]+ -> skip;

INVALID_CHAR : . ;








// =======================================================================================================================================
//                                                             Parser Rules
// =======================================================================================================================================

compilationUnit
    : classDeclaration* EOF
    ;

classDeclaration
    : accessModifier? CLASS IDENTIFIER classBody
    ;

classBody
    : LBRACE classBodyDeclaration* RBRACE
    ;

classBodyDeclaration
    : methodDeclaration
    | variableDeclaration
    ;

statement
    : variableDeclaration
    | assignment
    | expressionStatement
    | controlStructure
    | block
    | returnStatement
    | SEMI //empty statement
    ;

methodDeclaration
    : accessModifier? STATIC? returnType IDENTIFIER LPAREN parameterList? RPAREN block
    ;

accessModifier
    : PUBLIC
    | PRIVATE
    | PROTECTED
    ;

returnType
    : VOID
    | type
    ;

parameterList
    : parameter (COMMA parameter)*
    | STRINGARGS //done for simplicity
    ;

parameter
    : type IDENTIFIER
    ;

type
    : BYTE
    | SHORT
    | INT
    | LONG
    | FLOAT
    | DOUBLE
    | BOOLEAN
    | CHAR
    | STRING
    //| IDENTIFIER // For user-defined types
    ;

variableDeclarationExpression
    : type variableDeclarators
    ;

variableDeclarators
    : variableDeclarator (COMMA variableDeclarator)*
    ;

variableDeclarator
    : IDENTIFIER (ASSIGN expression)?
    ;

variableDeclaration
    : type variableDeclarators SEMI
    ;

assignment
    : IDENTIFIER ASSIGN expression
    ;

expressionStatement
    : expression SEMI
    ;

expression
    : assignment
    | logicalOrExpression
    ;

logicalOrExpression
    : logicalAndExpression (OR logicalAndExpression)*
    ;

logicalAndExpression
    : equalityExpression (AND equalityExpression)*
    ;

equalityExpression
    : relationalExpression (equalityOperator relationalExpression)*
    ;

relationalExpression
    : additiveExpression (relationalOperator additiveExpression)*
    ;

additiveExpression
    : multiplicativeExpression (additiveOperator multiplicativeExpression)*
    ;

multiplicativeExpression
    : unaryExpression (multiplicativeOperator unaryExpression)*
    ;

unaryExpression
    : unaryOperator unaryExpression
    | postfixExpression
    ;

postfixExpression
    : primaryExpression postfixOperator*
    ;

primaryExpression
    : integerLiteral
    | STRINGLIT
    | CHARACTER
    | IDENTIFIER
    | LPAREN expression RPAREN
    | booleanLiteral
    | NULL_LITERAL
    ;

booleanLiteral
    : TRUE
    | FALSE
    ;

integerLiteral
    : DECIMAL_LITERAL
    | HEX_LITERAL
    | BINARY_LITERAL
    | OCTAL_LITERAL
    ;

/*
operator
    : PLUS
    | PLUSEQ
    | MINUS
    | MINEQ
    | TIMES
    | TIMESEQ
    | DIVIDE
    | DIVEQ
    | MODULUS
    | MODULEQ
    | LT
    | GT
    | LE
    | GE
    | EQ
    | NEQ
    | AND
    | OR
    | NOT;
    */

equalityOperator
    : EQ
    | NEQ
    ;

relationalOperator
    : LT
    | GT
    | LE
    | GE
    ;

additiveOperator
    : PLUS
    | MINUS
    ;

multiplicativeOperator
    : TIMES
    | DIVIDE
    | MODULUS
    ;

unaryOperator
    : PLUS
    | MINUS
    | NOT
    | postfixOperator
    ;

postfixOperator
    : INCREMENT
    | DECREMENT
    ;

controlStructure
    : ifStatement
    | whileLoop
    | forLoop
    | doWhileLoop
    ;

ifStatement
    : IF LPAREN expression RPAREN block ( ELSE ( ifStatement | block ) )?
    ;

whileLoop
    : WHILE LPAREN expression RPAREN block
    ;

doWhileLoop
    : DO block WHILE LPAREN expression RPAREN SEMI
    ;

forLoop
    : FOR LPAREN (variableDeclarationExpression | assignmentExpressionList)? SEMI expression? SEMI updateExpressionList? RPAREN block
    ;

assignmentExpressionList
    : assignment (COMMA assignment)*
    ;

updateExpressionList
    : updateExpression (COMMA updateExpression)*
    ;

updateExpression
    : assignment
    | unaryExpression
    | postfixExpression
    ;

block
    : LBRACE statement* RBRACE
    ;

returnStatement
    : RETURN expression? SEMI
    ;