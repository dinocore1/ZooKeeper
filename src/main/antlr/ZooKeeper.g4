grammar ZooKeeper;

file
    : exeBuildDef
    | libraryBuildDef
    | precompiledLibDef
    ;

precompiledLibDef
    : LIB name=ID version object
    ;

exeBuildDef
    : BUILD EXE name=ID version object
    ;

libraryBuildDef
    : BUILD SHAREDLIB name=ID version object
    ;

value
    : string
    | array
    | object
    ;

string
    : STRING_LITERAL
    ;

object
    : LPAREN keyValueEntries RPAREN
    ;

keyValueEntries
    : keyValueEntries key=ID COLON value
    |
    ;

array
    : LBRACE ( value ( COMMA value )* )? RBRACE
    ;


version
    : major=NUM DOT minor=NUM DOT patch=NUM
    ;

LIB : 'lib' ;
BUILD : 'build' ;
EXE : 'exe' ;
SHAREDLIB : 'sharedlib' ;
DOT : '.' ;
COLON : ':' ;
COMMA : ',' ;
LPAREN : '{' ;
RPAREN : '}' ;
LBRACE : '[' ;
RBRACE : ']' ;
STRING_LITERAL : '"' (~('"' | '\\' | '\r' | '\n') | '\\' ('"' | '\\'))* '"';
NUM : [0-9]+ ;
ID : [a-zA-Z0-9\-_]+ ;
COMMENT: '/*' .*? '*/' -> skip ;
WS : [ \t\r\n]+ -> skip ;