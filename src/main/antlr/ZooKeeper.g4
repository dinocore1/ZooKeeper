grammar ZooKeeper;

file
    : library+
    ;


library
    : LIB name=ID LPAREN keyvalues RPAREN
    ;

keyvalues
    : keyvalues keyvalue
    |
    ;

keyvalue
    : key=ID value=STRING_LITERAL
    ;

LIB : 'lib' ;
LPAREN : '{' ;
RPAREN : '}' ;
STRING_LITERAL : '"' (~('"' | '\\' | '\r' | '\n') | '\\' ('"' | '\\'))* '"';

ID : [a-zA-Z0-9_]+ ;
WS : [ \t\r\n]+ -> skip ;