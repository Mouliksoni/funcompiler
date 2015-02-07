tree grammar TreeGrammar;

options {
  backtrack=true;
  tokenVocab = Micro;
  ASTLabelType = CommonTree;
}
// Handle method scope
scope ScopeHandler {
    String scopeName;
    Hashtable symbols;
}
// Include additional headers required to build the compiler
@header {
import java.util.LinkedList;
import java.util.HashMap;
import java.util.Hashtable;
}

@members {
HashMap symbolTableInfo = new HashMap();
LinkedList irList = new LinkedList();
int tempIndex = 0; //Global temporary variable index maintainer.
enum opType { ADD, SUB, MUL, DIV, READ, WRITE, STORE, LABEL, FUNEND, END };
enum exprType { INT, FLOAT, DEFAULT };
/////////////////////////////////////////////////////////////////////////////////////////
// Symbol Table handling
// Make an entry into symbol table
protected void insertSymbolTableEntry(String varType, String idName, String text, boolean param) throws RecognitionException
{
  SymbolTableInfo tmp = new SymbolTableInfo();
  if (varType.compareTo("STRING") == 0)
  {
    tmp.strVal = text;
    tmp.type = identifierType.ID_STRING;
  }
  if (param == true)
    tmp.funcParam = true;
  if (varType.compareTo("INT") == 0)
    tmp.type = identifierType.ID_INTEGER;
  if (varType.compareTo("FLOAT") == 0)
    tmp.type = identifierType.ID_FLOAT;
    
  tmp.identifierName = idName;
  if ($ScopeHandler::symbols.containsKey(idName)) {
    try {
    handleVariableRedeclaration(idName, $ScopeHandler::scopeName);
    }
    catch (RecognitionException e) {
      throw e;
      }
  }
  else {
    $ScopeHandler::symbols.put(idName, tmp);
  }
}

// Variable redeclaration handling
protected void handleVariableRedeclaration(String var, String scope) throws RecognitionException
{
    throw new RecognitionException(); //Hack! Need to implement custom exception
}

//////////////////////////////////////////////////////////////////////////////////////
// IR handling
protected String generateIR(opType type, String op1, String op2, exprType typeOp1, exprType typeOp2, boolean incrementIndex)throws RecognitionException
{
  String res="";
  boolean bInc = true;
  int type1 = 1, type2 = 1;
  exprType typeVal;
  if (typeOp1 != typeOp2)
  {
    System.out.println("Operand 1 and 2 are of different data types");
    throw new RecognitionException ();
  }
  typeVal = typeOp1; //Even op2 will have the same type.
  IR stmt = new IR();
  String result = "\$T"+tempIndex;
  if (op1.indexOf("\$T") != -1)
  {
    type1 = 0;
    result = op1;
    bInc = false;
  }
  if (op2.indexOf("\$T") != -1 )
  {
    type2 = 0;
    if ((type != opType.SUB) && (type != opType.DIV))
    result = op2;
    bInc = false;
  }
  
  if (type1 == 0 && type2 == 0)
    bInc = true;
    
  switch(type)
  {
    case LABEL:
       stmt.updateValues(opCodeType.LABEL, op1, "", 0, 0, "");
    break;
    case END:
       stmt.updateValues(opCodeType.END, "", "", 0, 0, "");// op1 param has scope name, use it if needed.
    break;
    case FUNEND:
       stmt.updateValues(opCodeType.FUNEND, "", "", 0, 0, "");// op1 param has scope name, use it if needed.
    break;
    case ADD:
      if (typeVal == exprType.INT){
        stmt.updateValues(opCodeType.ADDI, op1, op2, type1, type2, result);
      }
      else if (typeVal == exprType.FLOAT){
        stmt.updateValues(opCodeType.ADDF, op1, op2, type1, type2, result);
      }
      res = result;
      if(bInc)tempIndex++;
    break;
    case SUB:
      if (typeVal == exprType.INT){
        if (type1 == 0 && type2 == 0)
        {
          result = op1;
          stmt.updateValues(opCodeType.SUBI, op2, op1, type1, type2, result);
        }
        else
          stmt.updateValues(opCodeType.SUBI, op1, op2, type1, type2, result);
      }
      else if (typeVal == exprType.FLOAT){
        if (type1 == 0 && type2 == 0)
        {
          result = op1;
          stmt.updateValues(opCodeType.SUBF, op2, op1, type1, type2, result);
        }
        else
          stmt.updateValues(opCodeType.SUBF, op1, op2, type1, type2, result);
      }
      res = result;
      if(bInc)tempIndex++;
    break;
    case MUL:
      if (typeVal == exprType.INT){
        stmt.updateValues(opCodeType.MULTI, op1, op2, type1, type2, result);
      }
      else if (typeVal == exprType.FLOAT){
        stmt.updateValues(opCodeType.MULTF, op1, op2, type1, type2, result);
      }
      res = result;
      if(bInc)tempIndex++;
    break;
    case DIV:
      if (typeVal == exprType.INT){
        if (type1 == 0 && type2 == 0)
        {
          result = op1;
          stmt.updateValues(opCodeType.DIVI, op2, op1, type1, type2, result);
        }
        else
          stmt.updateValues(opCodeType.DIVI, op1, op2, type1, type2, result);
      }
      else if (typeVal == exprType.FLOAT){
        if (type1 == 0 && type2 == 0)
        {
          result = op1;
          stmt.updateValues(opCodeType.DIVF, op2, op1, type1, type2, result);
        }
        else
          stmt.updateValues(opCodeType.DIVF, op1, op2, type1, type2, result);
      }
      res = result;
      if(bInc)tempIndex++;
    break;
    case READ:
      if (typeVal == exprType.INT){
        stmt.updateValues(opCodeType.READI, op1, op2, 0, 0, "");
      }
      else if (typeVal == exprType.FLOAT){
        stmt.updateValues(opCodeType.READF, op1, op2, 0, 0, "");
      }
    break;
    case WRITE:
      if (typeVal == exprType.INT){
        stmt.updateValues(opCodeType.WRITEI, op1, op2, 0, 0, "");
      }
      else if (typeVal == exprType.FLOAT){
        stmt.updateValues(opCodeType.WRITEF, op1, op2, 0, 0, "");
      }
    break;
    case STORE:
      if (typeVal == exprType.INT){
        if(incrementIndex)
          op2 = result;
        stmt.updateValues(opCodeType.STOREI, op1, op2, type1, 0, "");
      }
      else if (typeVal == exprType.FLOAT){
        if(incrementIndex)
          op2 = result;
        stmt.updateValues(opCodeType.STOREF, op1, op2, type1, 0, "");
      }
      res = result;
      if(incrementIndex)
      tempIndex++;
    break;
  }
  irList.add(stmt);
  return res;
}

protected exprType getIdentType(String identifier) throws RecognitionException
{
  // Check whether this identifier is available in the Symbol table 
  exprType res = exprType.DEFAULT;
  Hashtable tbl = $ScopeHandler::symbols;
  if (! $ScopeHandler::symbols.containsKey(identifier))
  {
     // Check the global symbol table once...
     tbl = (Hashtable)symbolTableInfo.get("");
     if (! tbl.containsKey(identifier))
     throw new RecognitionException(); //Hack! Need to implement custom exception
  }
  SymbolTableInfo symbolInfo = (SymbolTableInfo)tbl.get(identifier);
  if (symbolInfo.type == identifierType.ID_INTEGER)
  {
    res = exprType.INT;
  }
  else if (symbolInfo.type == identifierType.ID_FLOAT)
  {
    res = exprType.FLOAT;
  }
  
  return res;
}

//////////////////////////////////////////////////////////////////////////////////////
// Override ANTLR exceptions for Exception handling
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
program 
scope ScopeHandler;
@init {
   // initialize a Global scope
   $ScopeHandler::symbols = new Hashtable();
   $ScopeHandler::scopeName = "";
   // Add to HashMap when done!
  symbolTableInfo.put($ScopeHandler::scopeName, $ScopeHandler::symbols);
}
@after {  
  // Add to HashMap when done!
  //symbolTableInfo.put($ScopeHandler::scopeName, $ScopeHandler::symbols);
}
    :  ^('PROGRAM' IDENTIFIER 'BEGIN' pgm_body* 'END'){generateIR(opType.END, $ScopeHandler::scopeName, "", exprType.DEFAULT, exprType.DEFAULT, false);}
    ;

pgm_body  : (decl | func_decl)
    ;
    
decl  : (string_decl_list | var_decl_list)
    ;

string_decl_list  : ^('STRING' ^(ASSIGNOP IDENTIFIER str)){insertSymbolTableEntry("STRING", $IDENTIFIER.text, $str.text, false);}
    ;
      
str : STRINGLITERAL
    ;
  
var_decl_list : ^(var_type IDENTIFIER){insertSymbolTableEntry($var_type.text, $IDENTIFIER.text,"", false);}
    ;
    
var_type  : 'FLOAT' | 'INT'
    ;

any_type  : var_type | 'VOID'
    ;
    
param_decl_list : var_type IDENTIFIER {insertSymbolTableEntry($var_type.text, $IDENTIFIER.text,"", true);}
    ;
      
func_decl 
scope ScopeHandler;
@init {
    // Init symbols info for this function
    $ScopeHandler::symbols = new Hashtable();
}
@after {
    // Add to hashmap when done!
   // symbolTableInfo.put($ScopeHandler::scopeName, $ScopeHandler::symbols);
}
    : ^('FUNCTION' any_type IDENTIFIER {$ScopeHandler::scopeName=$IDENTIFIER.text;
                                         symbolTableInfo.put($ScopeHandler::scopeName, $ScopeHandler::symbols);
                                         generateIR(opType.LABEL, $IDENTIFIER.text, "", exprType.DEFAULT, exprType.DEFAULT, false);} param_decl_list* 'BEGIN' func_body* 'END'){generateIR(opType.FUNEND, $ScopeHandler::scopeName, "", exprType.DEFAULT, exprType.DEFAULT, false);}
    ;
      
func_body   : decl | stmt_list
    ;
      

stmt_list : stmt (stmt)*
    ;
    
stmt  : assign_expr
  | read_stmt
  | write_stmt
  | return_stmt
  |   if_stmt
  |   for_stmt
  ;

    
assign_expr : ^(ASSIGNOP IDENTIFIER expr)
               {
                exprType typeIdent = getIdentType($IDENTIFIER.text);
                generateIR(opType.STORE, $expr.expression, $IDENTIFIER.text, typeIdent, $expr.type, false);
               }
    ;
    
read_stmt : ^('READ' IDENTIFIER)
             {
                exprType typeIdent = getIdentType($IDENTIFIER.text);
                generateIR(opType.READ, $IDENTIFIER.text, "", typeIdent, typeIdent, false);
             }
    ;
    
write_stmt  : ^('WRITE' IDENTIFIER)
               {
                 exprType typeIdent = getIdentType($IDENTIFIER.text);
                 generateIR(opType.WRITE, $IDENTIFIER.text, "", typeIdent, typeIdent, false);
               }
    ;
    
return_stmt : ^('RETURN' expr)
    ;

expr returns [String expression, exprType type]  
      : ^(ADDOP e1=expr e2=expr) { $expression = generateIR(opType.ADD, $e1.expression, $e2.expression, $e1.type, $e2.type, true); $type = $e1.type; }
      | ^(SUBOP e1=expr e2=expr) { $expression = generateIR(opType.SUB, $e1.expression, $e2.expression, $e1.type, $e2.type, true); $type = $e1.type; }
      | ^(MULOP e1=expr e2=expr) { $expression = generateIR(opType.MUL, $e1.expression, $e2.expression, $e1.type, $e2.type, true); $type = $e1.type; }
      | ^(DIVOP e1=expr e2=expr) { $expression = generateIR(opType.DIV, $e1.expression, $e2.expression, $e1.type, $e2.type, true); $type = $e1.type; }
      |   IDENTIFIER { $expression = $IDENTIFIER.text; $type = getIdentType($IDENTIFIER.text); }
      |   INTLITERAL { $expression = generateIR(opType.STORE, $INTLITERAL.text, "", exprType.INT, exprType.INT, true); $type = exprType.INT; }
      |   FLOATLITERAL { $expression = generateIR(opType.STORE, $FLOATLITERAL.text, "", exprType.FLOAT, exprType.FLOAT, true); $type = exprType.FLOAT; }
      ;
    
if_stmt   : ^('IF' cond ^('THEN' stmt_list) else_part* 'ENDIF')
    ;
    
else_part : ^('ELSE' stmt_list)
    ;
    
cond    : ^(compop expr expr)
    ;
    
compop    : COMPLESSEROP
    |   COMPGRTROP
    |   COMPEQUALOP
    ;
    
for_stmt  : ^('FOR' (assign_expr)? (cond)? (assign_expr)? stmt_list* 'ENDFOR')
    ;
