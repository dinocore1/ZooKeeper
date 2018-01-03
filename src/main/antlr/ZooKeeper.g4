grammar ZooKeeper;

file
    : libraryMetadataDef
    | exeBuildDef
    | libraryBuildDef
    ;

libraryMetadataDef
    : LIB name=ID version object
    ;

exeBuildDef
    : BUILD EXE name=ID version object
    ;

libraryBuildDef
    : BUILD LIB name=ID version object
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
    : ( key=ID COLON value )*
    ;

array
    : LBRACE arrayEntries RBRACE
    ;


arrayEntries
    : ( value ( COMMA value )* )?
    ;

version
    : major=NUM DOT minor=NUM DOT patch=NUM
    ;

BUILD : 'build' ;
EXE : 'exe' ;
LIB : 'lib' ;
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