grammar Javamm;

@header {
    package pt.up.fe.comp2023;
}

IMPORT : 'import' ;
CLASS : 'class' ;
EXTENDS : 'extends' ;
PUBLIC : 'public' ;
STATIC : 'static' ;
VOID : 'void' ;
RETURN : 'return' ;
NEW : 'new' ;

INT : 'int' ;
BOOLEAN : 'boolean' ;

IF : 'if' ;
ELSE : 'else' ;
WHILE : 'while' ;

THIS : 'this' ;
TRUE : 'true' ;
FALSE : 'false' ;

LT : '<';
DBLAMPERSAND : '&&';
PLUS : '+' ;
MINUS : '-' ;
ASTERISC : '*' ;
FSLASH : '/' ;
EXCLAMATION : '!' ;
EQUALS : '=' ;

COMMA : ',' ;
DOT : '.' ;
LPAREN : '(' ;
RPAREN : ')' ;
LBRACKET : '{' ;
RBRACKET : '}' ;
LSQBRACKET : '[' ;
RSQBRACKET : ']' ;
SEMICOLON : ';' ;

INTEGER : '0' | [1-9][0-9]* ;
ID : [$a-zA-Z_][$a-zA-Z_0-9]* ;

BLOCKCOMMENT : '/*' .*? '*/' -> skip ;
LINECOMMENT : '//' ~[\r\n]*  -> skip ;
WS : [ \t\n\r\f]+ -> skip ;

program // Doesn't need type checking
    : (importDeclaration)* classDeclaration EOF
    ;

importDeclaration // Doesn't need type checking
    : IMPORT path+=ID (DOT path+=ID)* SEMICOLON
    ;

classDeclaration // Doesn't need type checking
    : CLASS name=ID (EXTENDS superName=ID)? LBRACKET
        (varDeclaration)*
        (methodDeclaration)*
      RBRACKET
    ;

varDeclaration // Doesn't need type checking
    : type name=ID SEMICOLON
    ;

parameterDeclaration // Doesn't need type checking
    : type name=ID
    ;

methodDeclaration locals [boolean isPublic = false, boolean isStatic = false] // Doesn't need type checking
    : (PUBLIC {$isPublic = true;})? (STATIC {$isStatic = true;}) VOID methodName=ID LPAREN arrayType=ID LSQBRACKET RSQBRACKET arrayName=ID RPAREN LBRACKET
        (varDeclaration)*
        (statement)*
      RBRACKET
      #MainMethodDeclaration
    | (PUBLIC {$isPublic = true;})? type methodName=ID LPAREN (parameterDeclaration (COMMA parameterDeclaration)*)? RPAREN LBRACKET
        (varDeclaration)*
        (statement)*
        RETURN expression SEMICOLON
      RBRACKET
      #GenericMethodDeclaration
    ;

type locals [boolean isArray = false] // Doesn't need type checking
    : name=INT (LSQBRACKET RSQBRACKET {$isArray = true;})?
    | name=BOOLEAN
    | name=ID
    ;

statement
    : LBRACKET (statement)* RBRACKET #BlockStatement // Doesn't need type checking
    | IF LPAREN expression RPAREN statement (ELSE statement)? #IfStatement
    | WHILE LPAREN expression RPAREN statement #WhileStatement
    | expression SEMICOLON #ExpressionStatement // Doesn't need type checking
    | name=ID EQUALS expression SEMICOLON #VariableAssignmentStatement
    | name=ID LSQBRACKET expression RSQBRACKET EQUALS expression SEMICOLON #ArrayIndexAssignmentStatement
    ;

expression
    : LPAREN expression RPAREN #ParenthesisExpression
    | expression LSQBRACKET expression RSQBRACKET #ArrayIndexExpression
    | expression DOT name=ID LPAREN (expression (COMMA expression)*)? RPAREN #MethodCallExpression
    | expression DOT name=ID #PropertyAccessExpression
    | op=EXCLAMATION expression #UnaryOp
    | expression op=(ASTERISC | FSLASH) expression #BinaryOp
    | expression op=(PLUS | MINUS) expression #BinaryOp
    | expression op=LT expression #BinaryOp
    | expression op=DBLAMPERSAND expression #BinaryOp
    | NEW INT LSQBRACKET expression RSQBRACKET #ArrayInitializationExpression
    | NEW className=ID LPAREN RPAREN #ObjectInitializationExpression
    | value=INTEGER #IntegerLiteral
    | value=TRUE #BooleanLiteral
    | value=FALSE #BooleanLiteral
    | THIS #ThisLiteral
    | name=ID #VariableLiteral
    ;
