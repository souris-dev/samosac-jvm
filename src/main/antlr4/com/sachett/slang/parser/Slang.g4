grammar Slang;

// Use when separating lexer and parser grammars:
//options {
//    tokenVocab = 'libs/SlangLexer';
//}

// Lexer Grammar

PROGSTART: '<SLANG>';
PROGEND: '</SLANG>';

LPAREN: '(';
RPAREN: ')';
LCURLYBR: '{';
RCURLYBR: '}';
LSQBR: '[';
RSQBR: ']';

PLUS: '+';
MINUS: '-';
DIVIDE: '/';
MULT: '*';
MODULO: '%';
EQUAL: '=';

LT: '<';
GT: '>';
LTEQ: '<=';
GTEQ: '>=';
COMP: '==';
COMPNOTEQ: '!=';

MARKERCOMMULTILINESTART: '\\\\*';
MARKERCOMMULTILINEEND: '*\\\\';
MARKERCOMSINGLELINE: ('\\\\-' | 'binod');
COMMENTSL : MARKERCOMSINGLELINE ~[\r\n]* '\r'? '\n' -> skip ;
COMMENTML: MARKERCOMMULTILINESTART .*? MARKERCOMMULTILINEEND -> skip;

LOGICALAND: ('and' | '&&');
LOGICALOR: ('or' | '||');
LOGICALXOR: ('strictor' | '||!');
LOGICALNOT: ('no' | '!!');

TRUE: ('true' | 'yes' | 'True' | 'TRUE');
FALSE: ('false' | 'nope' | 'False' | 'FALSE');

IF: 'if';
ELSE: 'else';
FUNCDEF: 'action';
VARDEF: ('bro,');

BINAND: '&';
BINOR: '|';
BINXOR: '|!';
BINNOT: '!';

RIGHTARROW: '->';

COLON: ':';
STATEMENTEND: '.';
WHILE: 'while';
RETURN: 'return';

INTTYPE: 'int';
STRINGTYPE: 'string';
BOOLTYPE: 'boolie';
VOIDTYPE: 'void';
NULLVALUE: 'null';

DECINT: [0-9]+;
IDENTIFIER: [_a-zA-Z][_a-zA-Z0-9]+;
STRING: '"' ('\\"'|.)*? '"';
COMMA: ',';

NEWLINE: ('\r'? '\n' | '\r' | '\n')+ -> skip;
TAB: ('t' | '    ' | '        ') -> skip;
WHITESPACE: ' ' -> skip;

OTHER: .;


// Parser Grammar

program: EOF
        | PROGSTART PROGEND
        | PROGSTART statements PROGEND
        | statements EOF;

statements: (statement | compoundStmt | funcDef | COMMENTSL | COMMENTML)+;

statement: (declStmt | assignStmt | declAssignStmt | functionCall | returnStmt) STATEMENTEND;

returnStmt: RETURN #returnStmtNoExpr
          | RETURN expr #returnStmtWithExpr;

assignStmt: IDENTIFIER EQUAL expr #exprAssign
          | IDENTIFIER EQUAL booleanExpr #booleanExprAssign;

expr: MINUS expr #unaryMinus
    | expr DIVIDE expr #exprDivide
    | expr MULT expr #exprMultiply
    | expr MODULO expr #exprModulo
    | expr PLUS expr #exprPlus
    | expr MINUS expr #exprMinus
    | LPAREN expr RPAREN #exprParen
    | IDENTIFIER #exprIdentifier
    | DECINT #exprDecint
    | STRING #exprString
    | functionCall #exprFunctionCall;

declStmt: VARDEF IDENTIFIER COLON typeName;
typeName: INTTYPE | STRINGTYPE | VOIDTYPE | BOOLTYPE;

declAssignStmt: VARDEF IDENTIFIER COLON BOOLTYPE EQUAL booleanExpr #booleanDeclAssignStmt
              | VARDEF IDENTIFIER COLON typeName EQUAL expr #normalDeclAssignStmt
              | VARDEF IDENTIFIER COLON EQUAL expr #typeInferredDeclAssignStmt
              | VARDEF IDENTIFIER COLON EQUAL booleanExpr #typeInferredBooleanDeclAssignStmt;

block: LCURLYBR RCURLYBR
     | LCURLYBR statements RCURLYBR;

ifStmt: IF LPAREN booleanExpr RPAREN block (elseifblocks+=ELSE IF LPAREN booleanExpr RPAREN block)*? (ELSE block)?;

whileStmt: WHILE LPAREN booleanExpr RPAREN block;

booleanExpr: LOGICALNOT booleanExpr #booleanExprNot
           | booleanExpr LOGICALOR booleanExpr #booleanExprOr
           | booleanExpr LOGICALAND booleanExpr #booleanExprAnd
           | booleanExpr LOGICALXOR booleanExpr #booleanExprXor
           | expr relOp expr #booleanExprRelOp
           | expr compOp expr #booleanExprCompOp
           | LPAREN booleanExpr RPAREN #booleanExprParen
           | IDENTIFIER #booleanExprIdentifier
           | TRUE #booleanTrue
           | FALSE #booleanFalse
           | functionCall #booleanFunctionCall;

compOp: (COMP | COMPNOTEQ);

relOp: (LT | GT | LTEQ | GTEQ);

compoundStmt: (ifStmt | whileStmt);

funcDef: (FUNCDEF IDENTIFIER block
       | FUNCDEF IDENTIFIER LPAREN RPAREN block
       | FUNCDEF IDENTIFIER LPAREN funcArgList RPAREN block) (STATEMENTEND)? #implicitRetTypeFuncDef
       | (FUNCDEF IDENTIFIER COLON typeName block
       | FUNCDEF IDENTIFIER LPAREN RPAREN COLON typeName block
       | FUNCDEF IDENTIFIER LPAREN funcArgList RPAREN COLON typeName block) (STATEMENTEND)? #explicitRetTypeFuncDef;

funcArgList: args+=argParam
           | (args+=argParam COMMA)+ args+=argParam;
argParam: IDENTIFIER COLON typeName;

callArgList: (callParams+=expr COMMA)? (booleanCallParams+=booleanExpr COMMA)?
                (callParams+=expr COMMA)? (booleanCallParams+=booleanExpr COMMA)?
                    (callParams+=expr | booleanCallParams+=booleanExpr);
functionCall: LPAREN RPAREN RIGHTARROW IDENTIFIER #functionCallNoArgs
            | LPAREN callArgList RPAREN RIGHTARROW IDENTIFIER #functionCallWithArgs;
