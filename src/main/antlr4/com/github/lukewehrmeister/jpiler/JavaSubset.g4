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
STRINGLIT     : '"' (~["\\] | '\\' .)*? '"' ;
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
    : primaryExpression (operator primaryExpression)* 
    | prefixExpression
    | postfixExpression
    ;

prefixExpression
    : (INCREMENT | DECREMENT) primaryExpression  // Only one `++` or `--`
    ;

postfixExpression
    : primaryExpression (INCREMENT | DECREMENT)  // Only one `++` or `--`
    ;

primaryExpression
    : DECIMAL_LITERAL
    | HEX_LITERAL
    | BINARY_LITERAL
    | OCTAL_LITERAL
    | STRINGLIT
    | CHARACTER
    | IDENTIFIER
    | LPAREN expression RPAREN
    | TRUE
    | FALSE
    | NULL_LITERAL
    ;

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

