grammar JavaSubset;

// =======================================================================================================================================
//                                                             Lexer Rules
// =======================================================================================================================================

//Data Type Keywords
BYTE : 'byte' ;
SHORT : 'short' ;
INT : 'int' ;
LONG : 'long' ;
FLOAT : 'float' ;
DOUBLE : 'double' ;
BOOLEAN : 'boolean' ;
CHAR : 'char' ;
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
STAREQ     : '*=' ;
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
SIMPLE_KEYWORDS : 'Integer' | 'Double' | 'Float' | 'Character' | 'Boolean' | 'Byte' | 'Long' | 'Object' | 'Void' ;

//INVALID_IDENTIFIER: [0-9]+[a-zA-Z_][a-zA-Z0-9_]* ;         FIXME: 5employees
IDENTIFIER     : [a-zA-Z_][a-zA-Z0-9_]*;

//FLOAT: [0-9]* '.' [0-9]+ ([eE] [+-]? [0-9]+)? [fF]? ;       FIXME: 5.5
INTEGER: [0-9]+ ;

DOT           : '.' ;
LPAREN        : '(' ;
RPAREN        : ')' ;
LBRACE        : '{' ;
RBRACE        : '}' ;
SEMI          : ';' ;
COMMA         : ',' ;
STRINGLIT        : '"' (~["\\] | '\\' .)*? '"' ;
CHARACTER     : '\'' (~['\\] | '\\' .) '\'' ;


COMMENT        : '//' ~[\r\n]* -> skip ;
MULTICOMMENT   : '/*' .*? '*/' -> skip ;
WHITESPACE     : [ \t\r\n]+ -> skip;

INVALID_CHAR : . ;








// =======================================================================================================================================
//                                                             Parser Rules
// =======================================================================================================================================

compilationUnit
    : statement+ EOF
    ;

program
    : statement+ ;

statement
    : classDeclaration
    | methodDeclaration
    | variableDeclaration
    | assignment
    | expressionStatement
    | controlStructure
    | block
    | SEMI //empty statement
    ;

classDeclaration
    : CLASS IDENTIFIER block
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

variableDeclaration
    : type IDENTIFIER ( ASSIGN expression )? SEMI
    ;

assignment
    : IDENTIFIER ASSIGN expression SEMI
    ;

expressionStatement
    : expression SEMI
    ;

expression
    : primaryExpression (operator primaryExpression)* ;

primaryExpression
    : INTEGER
    | STRINGLIT
    | CHARACTER
    | IDENTIFIER
    | LPAREN expression RPAREN
    ;

operator
    : PLUS
    | MINUS
    | TIMES
    | DIVIDE
    | MODULUS
    | LT
    | GT
    | LE
    | GE
    | EQ
    | NEQ
    | AND
    | OR
    | NOT
    ;

controlStructure
    : ifStatement
    | whileLoop
    | forLoop
    | doWhileLoop
    ;

ifStatement
    : IF LPAREN expression RPAREN block ( ELSE ifStatement | ELSE block )?
    ;

whileLoop
    : WHILE LPAREN expression RPAREN block
    ;

doWhileLoop
    : DO block WHILE LPAREN expression RPAREN SEMI
    ;

forLoop
    : FOR LPAREN variableDeclaration expression SEMI assignment RPAREN block 
    ;

block
    : LBRACE statement* RBRACE
    ;

