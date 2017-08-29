grammar ZooKeeper;

file
    : compileContext
    ;

compileContext
    : compileContext platform
    | compileContext library
    | compileContext COMMENT
    |
    ;

platform
    : PLATFORM name=ID LPAREN keyvalues RPAREN
    ;

library
    : LIB name=ID version LPAREN libraryBody RPAREN
    ;

version
    : major=NUM DOT minor=NUM DOT patch=NUM
    ;

libraryBody
    : libraryBody dependencies
    | libraryBody sourceDirective
    | libraryBody cmakeArgs
    |
    ;

dependencies
    : DEPENDENCIES LPAREN dependList RPAREN
    ;

dependList
    : dependList COMPILE ID version
    | dependList TEST ID version
    |
    ;

sourceDirective
    : source
    | gitArgs
    ;

source
    : SRC src=STRING_LITERAL
    ;

gitArgs
    : GIT LPAREN keyvalues RPAREN
    ;

cmakeArgs
    : CMAKE LPAREN keyvalues RPAREN
    ;

keyvalues
    : keyvalue*
    ;

keyvalue
    : key=ID value=STRING_LITERAL
    ;

PLATFORM : 'platform' ;
LIB : 'lib' ;
DOT : '.' ;
LPAREN : '{' ;
RPAREN : '}' ;
DEPENDENCIES : 'dependencies' ;
SRC : 'src' ;
CMAKE : 'cmake' ;
GIT : 'git' ;
COMPILE : 'compile' ;
TEST : 'test' ;
STRING_LITERAL : '"' (~('"' | '\\' | '\r' | '\n') | '\\' ('"' | '\\'))* '"';

NUM : [0-9]+ ;
ID : [a-zA-Z0-9\-_]+ ;
COMMENT: '/*' .*? '*/' -> skip ;
WS : [ \t\r\n]+ -> skip ;