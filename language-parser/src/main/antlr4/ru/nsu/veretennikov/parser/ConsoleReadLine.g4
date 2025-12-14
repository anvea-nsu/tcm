grammar ConsoleReadLine;

// Parser rules
program
    : statement* EOF
    ;

statement
    : CONSOLE_READLINE LPAREN argument? RPAREN SEMICOLON
    ;

argument
    : IDENTIFIER
    ;

// Lexer rules
CONSOLE_READLINE : 'Console.ReadLine' ;
LPAREN           : '(' ;
RPAREN           : ')' ;
SEMICOLON        : ';' ;
IDENTIFIER       : [a-zA-Z_][a-zA-Z0-9_]* ;

// Skip whitespace and comments
WS               : [ \t\r\n]+ -> skip ;
LINE_COMMENT     : '//' ~[\r\n]* -> skip ;
BLOCK_COMMENT    : '/*' .*? '*/' -> skip ;