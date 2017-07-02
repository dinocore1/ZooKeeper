grammar ZooKeeper;

file
    : library+
    ;


library
    : LIB name=ID version LPAREN libraryBody RPAREN
    ;

version
    : major=NUM DOT minor=NUM DOT patch=NUM
    ;

libraryBody
    : libraryBody DEPENDENCIES LPAREN dependList RPAREN
    |
    ;

dependList
    : dependList COMPILE ID version
    | dependList TEST ID version
    |
    ;

keyvalues
    : keyvalues keyvalue
    |
    ;

keyvalue
    : key=ID value=STRING_LITERAL
    ;

LIB : 'lib' ;
DOT : '.' ;
LPAREN : '{' ;
RPAREN : '}' ;
DEPENDENCIES : 'dependencies' ;
COMPILE : 'compile' ;
TEST : 'test' ;
STRING_LITERAL : '"' (~('"' | '\\' | '\r' | '\n') | '\\' ('"' | '\\'))* '"';

NUM : [0-9]+ ;
ID : [a-zA-Z0-9\-_]+ ;
WS : [ \t\r\n]+ -> skip ;