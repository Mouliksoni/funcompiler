grammar Micro;
//To handle 
//1.) "\r\n"
//2.) Overloaded functions
options {
    backtrack=true;
    output=AST;
    ASTLabelType=CommonTree;
}

// Overridden member functions to make sure ANTLR does not handle
// the errors encountered during parsing.
@members {

protected Object recoverFromMismatchedToken(IntStream input,
int ttype, BitSet follow) throws RecognitionException
{
        throw new MismatchedTokenException(ttype, input);
}

public Object recoverFromMismatchedSet(IntStream input,
RecognitionException e, BitSet follow) throws RecognitionException
{
        throw e;
}
}

@rulecatch {
  catch (RecognitionException e) {
  throw e;
  }
}

program   : 'PROGRAM' IDENTIFIER 'BEGIN' pgm_body* 'END'->^('PROGRAM' IDENTIFIER 'BEGIN' pgm_body* 'END')
  ;

pgm_body  : (decl | func_decl)
    ;
    
decl  : (string_decl_list | var_decl_list)
  ;

string_decl_list  : 'STRING' IDENTIFIER ASSIGNOP str SEMICOLONOP->^('STRING' ^(ASSIGNOP IDENTIFIER str))
      ;
      
str : STRINGLITERAL
  ;
  
var_decl_list : var_type IDENTIFIER (COMMAOP IDENTIFIER)* SEMICOLONOP->^(var_type IDENTIFIER)+
    ;
    
var_type  : 'FLOAT' | 'INT'
    ;

any_type  : var_type | 'VOID'
    ;
    
param_decl_list : var_type IDENTIFIER (COMMAOP! var_type IDENTIFIER)*
    ;
      
func_decl   : 'FUNCTION' any_type IDENTIFIER LPARENOP (param_decl_list)* RPARENOP 'BEGIN' func_body* 'END'->^('FUNCTION' any_type IDENTIFIER param_decl_list* 'BEGIN' func_body* 'END')
      ;
      
func_body   : (decl | stmt_list)
      ;
      

stmt_list : stmt (stmt)*
    ;
    
stmt  : assign_stmt
  | read_stmt
  | write_stmt
  | return_stmt
  |   if_stmt
  |   for_stmt
  ;


assign_stmt : assign_expr SEMICOLONOP!
    ;
    
assign_expr : IDENTIFIER ASSIGNOP expr->^(ASSIGNOP IDENTIFIER expr)
    ;
    
read_stmt : 'READ' LPARENOP IDENTIFIER (COMMAOP IDENTIFIER)* RPARENOP SEMICOLONOP->^('READ' IDENTIFIER)+
    ;
    
write_stmt  : 'WRITE' LPARENOP IDENTIFIER (COMMAOP IDENTIFIER)* RPARENOP SEMICOLONOP->^('WRITE' IDENTIFIER)+
    ;
    
return_stmt :   'RETURN' expr SEMICOLONOP->^('RETURN' expr)
    ;


expr    : factor ((ADDOP|SUBOP)^ factor)*
    ;
    
factor    : postfix_expr ((MULOP|DIVOP)^ postfix_expr)*
    ;
    
postfix_expr  : primary | call_expr
    ;
    
call_expr : IDENTIFIER LPARENOP! (expr (COMMAOP! expr)*)? RPARENOP!
    ;

primary   : LPARENOP! expr RPARENOP! 
    |   IDENTIFIER 
    |   INTLITERAL
    |   FLOATLITERAL
    ;
    
if_stmt   : 'IF' LPARENOP cond RPARENOP 'THEN' stmt_list else_part* 'ENDIF'->^('IF' cond ^('THEN' stmt_list) else_part* 'ENDIF')
    ;
    
else_part : 'ELSE' stmt_list->^('ELSE' stmt_list)
    ;
    
cond    : expr compop expr->^(compop expr expr)
    ;
    
compop    : COMPLESSEROP
    |   COMPGRTROP
    |   COMPEQUALOP
    ;
    
for_stmt  : 'FOR' LPARENOP (assign_expr)? SEMICOLONOP (cond)? SEMICOLONOP (assign_expr)? RPARENOP stmt_list* 'ENDFOR'->^('FOR' (assign_expr)? (cond)? (assign_expr)? stmt_list* 'ENDFOR')
    ;
    
IDENTIFIER  : ('a'..'z'|'A'..'Z') ('a'..'z'|'A'..'Z'|'0'..'9')*
    ;
    
INTLITERAL  : '0'..'9'+
        ;
        
FLOATLITERAL  :   ('0'..'9')+ '.' ('0'..'9')+
    | '.' ('0'..'9')+
    ;
  
STRINGLITERAL   :       '"' (~'"' | '""') *'"'
    ;

COMMENT   :     '--' (~'\n')* '\n' {$channel = HIDDEN;}
    ;

WS    :   (' ' |'\n' |'\r' |'\t' | '\f')+ {$channel = HIDDEN;} 
    ;

ASSIGNOP  : ':='
    ;

ADDOP     : '+'
    ;
    
SUBOP     : '-'
    ;
    
MULOP     : '*'
    ;
    
DIVOP     : '/'
    ;
    
COMPEQUALOP : '='
    ;
    
COMPLESSEROP  : '<'
    ;
    
COMPGRTROP  : '>'
    ;

LPARENOP  : '('
    ;
    
RPARENOP  : ')'
    ;
    
COMMAOP   : ','
    ;
    
SEMICOLONOP : ';'
    ;