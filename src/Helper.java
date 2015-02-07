import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Enumeration;
import java.util.ListIterator;
// Helper Classes
enum identifierType { ID_INTEGER, ID_FLOAT, ID_STRING, ID_NOTIDEN};

class SymbolTableInfo {// Hold Symbol Table fields.
	
		SymbolTableInfo(){
			identifierName = "";
			type = identifierType.ID_NOTIDEN;
			strVal = "";
			funcParam = false;
		}
		String identifierName;
		identifierType type;
		String strVal;// Only for variables of type STRING.
		boolean funcParam;
}
//////////////////////////////////////////////////////////////////////////////////////
//Code Generation
enum opCodeType { ADDI/*Add Integer*/, ADDF/*Add Float*/, SUBI/*Subtract Integer*/, SUBF/*Subtract Float*/, MULTI/*Multiply Integer*/, MULTF/*Multiply Float*/, DIVI/*Divide Integer*/, DIVF/*Divide Float*/, STOREI/*Store Integer*/, STOREF/*Store Float*/, GE/*Greater than or equal*/, LE/*Lesser than or equal*/, NE/*Not equal*/, JUMP/*Jump!*/, LABEL/*Label*/, READI/*Read Integer*/, READF/*Read Float*/, WRITEI/*Write Integer*/, WRITEF/*Write Float*/, FUNEND, END /*pseudo statement to handle return from call*/};

class IR {
	public static String ADDINTEGER = "ADDI";
	public static String ADDFLOAT = "ADDF";
	public static String SUBINTEGER = "SUBI";
	public static String SUBFLOAT = "SUBF";
	public static String MULINTEGER = "MULI";
	public static String MULFLOAT = "MULF";
	public static String DIVINTEGER = "DIVI";
	public static String DIVFLOAT = "DIVF";
	public static String READINTEGER = "READI";
	public static String READFLOAT = "READF";
	public static String WRITEINTEGER = "WRITEI";
	public static String WRITEFLOAT = "WRITEF";
	public static String STOREINTEGER = "STOREI";
	public static String STOREFLOAT = "STOREF";
	public static String LABEL = "LABEL";
	
	public IR() {
	}
	public void updateValues(opCodeType type, String operand1, String operand2, int type1, int type2, String tmp) {
		opCode = type;
		op1 = operand1;
		op1Type = type1;
		op2 = operand2;
		op2Type = type2;
		result = tmp;
	}
	opCodeType opCode;
	String op1;
	String op2;
	int op1Type; //0 - temporary, 1 - memory 
	int op2Type; //0 - temporary, 1 - memory
	String result;
	
	public String getThreeAddressInst(IR tmp) {
		String res="";
		boolean bAdd = true;
		switch(tmp.opCode)
		{
			case ADDI:
				res = IR.ADDINTEGER+ " " + tmp.op1 + " " + tmp.op2 + " " + tmp.result;
				break;
			case ADDF:
				res = IR.ADDFLOAT +  " " + tmp.op1 + " " + tmp.op2 + " " + tmp.result;
				break;
			case SUBI:
				res = IR.SUBINTEGER + " " +  tmp.op1 + " " + tmp.op2 + " " + tmp.result;
				break;
			case SUBF:
				res = IR.SUBFLOAT + " " +  tmp.op1 + " " + tmp.op2 + " " + tmp.result;
				break;
			case MULTI:
				res = IR.MULINTEGER + " " +  tmp.op1 + " " + tmp.op2 + " " + tmp.result;
				break;
			case MULTF:
				res = IR.MULFLOAT + " " +  tmp.op1 + " " + tmp.op2 + " " + tmp.result;
				break;
			case DIVI:
				res = IR.DIVINTEGER + " " +  tmp.op1 + " " + tmp.op2 + " " + tmp.result;
				break;
			case DIVF:
				res = IR.DIVFLOAT + " " +  tmp.op1 + " " + tmp.op2 + " " + tmp.result;
				break;
			case READI:
				res = IR.READINTEGER + " " +  tmp.op1;
				break;
			case READF:
				res = IR.READFLOAT + " " +  tmp.op1;
				break;
			case WRITEI:
				res = IR.WRITEINTEGER + " " +  tmp.op1;
				break;
			case WRITEF:
				res = IR.WRITEFLOAT + " " +  tmp.op1;
				break;
			case STOREI:
				res = IR.STOREINTEGER + " " +  tmp.op1 + " " + tmp.op2;
				break;
			case STOREF:
				res = IR.STOREFLOAT + " " +  tmp.op1 + " " + tmp.op2;
				break;
			case LABEL:
				res = IR.LABEL + " " +  tmp.op1;
				break;
			default:
				//Unhandled type of instruction.
				bAdd = false;
				break;
		}
		if (bAdd)
			CodeGen.irCode.add(res);
		return res;
	}
}

class Tiny {
	public static String tiny_ADDINTEGER = "addi ";
	public static String tiny_ADDFLOAT = "addr ";
	public static String tiny_SUBINTEGER = "subi ";
	public static String tiny_SUBFLOAT = "subr ";
	public static String tiny_MULINTEGER = "muli ";
	public static String tiny_MULFLOAT = "mulr ";
	public static String tiny_DIVINTEGER = "divi ";
	public static String tiny_DIVFLOAT = "divr ";
	public static String tiny_READINTEGER = "sys readi ";
	public static String tiny_READFLOAT = "sys readr ";
	public static String tiny_WRITEINTEGER = "sys writei ";
	public static String tiny_WRITEFLOAT = "sys writer ";
	public static String tiny_STORE = "move ";
	public static String tiny_LABEL = "label ";
	public static String tiny_RETURN = "ret ";
	public static String tiny_LINK = "link ";
	public static String tiny_UNLINK = "unlnk";
	public static String tiny_PUSH = "push";
	public static String tiny_POP = "pop";
	public static String tiny_VAR = "var ";
	public static String tiny_JMPSR = "jsr ";
	public static String tiny_HALT = "sys halt";
	public static String tiny_END = "end";
	
	
	private void generateLabelCode(IR tmpIR) {
		int linkCount = 0 ;
		CodeGen.tinyCode.add(tiny_LABEL + tmpIR.op1);
		linkCount = CodeGen.linkIndex(tmpIR.op1);
		CodeGen.tinyCode.add(tiny_LINK + linkCount);
	}
	
	private void generateStoreCode (IR tmpIR) {
		
		String inst = "";
		if (tmpIR.op1Type == 0)
		{
			inst = tmpIR.op1.replace("$T", "r");
			inst = Tiny.tiny_STORE + inst + " " + tmpIR.op2;
		}
		else
		{
			inst = tmpIR.op2.replace("$T", "r");
			inst = tiny_STORE + tmpIR.op1 + " " + inst;
		}
		/*inst = inst + tmpIR.op1.replace("$T", "r");
		else
			inst = inst + tmpIR.op1;
		
		if (tmpIR.op2Type == 0)
			inst = inst + " " + tmpIR.op2.replace("$T", "r");
		else
			inst = inst + " " + tmpIR.op2;*/
		
		CodeGen.tinyCode.add(inst);
	}
	
	private void generateAddCode (IR tmpIR) {
		
		String tmp = "";
		String inst = "";
		
		// If both the operands are from memory then ADD needs two instructions.
		if (tmpIR.op1Type == 1 && tmpIR.op2Type == 1)
		{
			// Move one of the variables to the result
			tmp = tmpIR.result.replace("$T", "r");
			inst = Tiny.tiny_STORE + tmpIR.op1 + " " + tmp ;
			CodeGen.tinyCode.add(inst);
			if (tmpIR.opCode == opCodeType.ADDI)
			{
				inst = Tiny.tiny_ADDINTEGER + tmpIR.op2 + " " + tmp ;
			}
			else if (tmpIR.opCode == opCodeType.ADDF)
			{
				inst = Tiny.tiny_ADDFLOAT + tmpIR.op2 + " " + tmp ;
			}
			CodeGen.tinyCode.add(inst);
		}
		else if (tmpIR.op1Type == 0 && tmpIR.op2Type == 1)
		{
			tmp = tmpIR.op1.replace("$T", "r");
			if (tmpIR.opCode == opCodeType.ADDI)
			{
				inst = Tiny.tiny_ADDINTEGER + tmpIR.op2 + " " + tmp ;
			}
			else if (tmpIR.opCode == opCodeType.ADDF)
			{
				inst = Tiny.tiny_ADDFLOAT + tmpIR.op2 +  " " +tmp ;
			}
			CodeGen.tinyCode.add(inst);
		}
		else if (tmpIR.op1Type == 1 && tmpIR.op2Type == 0)
		{
			tmp = tmpIR.op2.replace("$T", "r");
			if (tmpIR.opCode == opCodeType.ADDI)
			{
				inst = Tiny.tiny_ADDINTEGER + tmpIR.op1 +  " " +tmp ;
			}
			else if (tmpIR.opCode == opCodeType.ADDF)
			{
				inst = Tiny.tiny_ADDFLOAT + tmpIR.op1 + " " + tmp ;
			}
			CodeGen.tinyCode.add(inst);
		}
		else
		{
			if (tmpIR.opCode == opCodeType.ADDI)
			{
				inst = Tiny.tiny_ADDINTEGER + tmpIR.op1.replace("$T", "r") +  " " + tmpIR.op2.replace("$T", "r") ;
			}
			else if (tmpIR.opCode == opCodeType.ADDF)
			{
				inst = Tiny.tiny_ADDFLOAT + tmpIR.op1.replace("$T", "r") +  " " + tmpIR.op2.replace("$T", "r") ;
			}
			CodeGen.tinyCode.add(inst);
		}
	}
	private void generateSubCode (IR tmpIR) {
		
		String tmp = "";
		String inst = "";
		
		// If both the operands are from memory then SUB needs two instructions.
		if (tmpIR.op1Type == 1 && tmpIR.op2Type == 1)
		{
			// Move one of the variables to the result
			tmp = tmpIR.result.replace("$T", "r");
			inst = Tiny.tiny_STORE + tmpIR.op1 +  " " +tmp ;
			CodeGen.tinyCode.add(inst);
			if (tmpIR.opCode == opCodeType.SUBI)
			{
				inst = Tiny.tiny_SUBINTEGER + tmpIR.op2 + " " + tmp ;
			}
			else if (tmpIR.opCode == opCodeType.SUBF)
			{
				inst = Tiny.tiny_SUBFLOAT + tmpIR.op2 +  " " +tmp ;
			}
			CodeGen.tinyCode.add(inst);
		}
		else if (tmpIR.op1Type == 0 && tmpIR.op2Type == 1)
		{
			tmp = tmpIR.op1.replace("$T", "r");
			if (tmpIR.opCode == opCodeType.SUBI)
			{
				inst = Tiny.tiny_SUBINTEGER + tmpIR.op2 + " " + tmp ;
			}
			else if (tmpIR.opCode == opCodeType.SUBF)
			{
				inst = Tiny.tiny_SUBFLOAT + tmpIR.op2 +  " " +tmp ;
			}
			CodeGen.tinyCode.add(inst);
		}
		else if (tmpIR.op1Type == 1 && tmpIR.op2Type == 0)
		{
			tmp = tmpIR.result.replace("$T", "r");
			inst = Tiny.tiny_STORE + tmpIR.op1 + " " + tmp ;
			CodeGen.tinyCode.add(inst);
			
			//tmp = tmpIR.op2.replace("$T", "r");
			if (tmpIR.opCode == opCodeType.SUBI)
			{
				inst = Tiny.tiny_SUBINTEGER  + tmpIR.op2.replace("$T", "r") + " " + tmp ;
			}
			else if (tmpIR.opCode == opCodeType.SUBF)
			{
				inst = Tiny.tiny_SUBFLOAT + tmpIR.op2.replace("$T", "r") + " " + tmp ;
			}
			CodeGen.tinyCode.add(inst);
		}
		else
		{
			if (tmpIR.opCode == opCodeType.SUBI)
			{
				inst = Tiny.tiny_SUBINTEGER + tmpIR.op1.replace("$T", "r") +  " " + tmpIR.op2.replace("$T", "r") ;
			}
			else if (tmpIR.opCode == opCodeType.SUBF)
			{
				inst = Tiny.tiny_SUBFLOAT + tmpIR.op1.replace("$T", "r") +  " " + tmpIR.op2.replace("$T", "r") ;
			}
			CodeGen.tinyCode.add(inst);
		}
	}
	
	private void generateMULTCode (IR tmpIR) {
		
		String tmp = "";
		String inst = "";
		
		// If both the operands are from memory then SUB needs two instructions.
		if (tmpIR.op1Type == 1 && tmpIR.op2Type == 1)
		{
			// Move one of the variables to the result
			tmp = tmpIR.result.replace("$T", "r");
			inst = Tiny.tiny_STORE + tmpIR.op1 + " " + tmp ;
			CodeGen.tinyCode.add(inst);
			if (tmpIR.opCode == opCodeType.MULTI)
			{
				inst = Tiny.tiny_MULINTEGER + tmpIR.op2 +  " " + tmp ;
			}
			else if (tmpIR.opCode == opCodeType.MULTF)
			{
				inst = Tiny.tiny_MULFLOAT + tmpIR.op2 +  " " + tmp ;
			}
			CodeGen.tinyCode.add(inst);
		}
		else if (tmpIR.op1Type == 0 && tmpIR.op2Type == 1)
		{
			tmp = tmpIR.op1.replace("$T", "r");
			if (tmpIR.opCode == opCodeType.MULTI)
			{
				inst = Tiny.tiny_MULINTEGER + tmpIR.op2 + " " + tmp ;
			}
			else if (tmpIR.opCode == opCodeType.MULTF)
			{
				inst = Tiny.tiny_MULFLOAT + tmpIR.op2 +  " " +tmp ;
			}
			CodeGen.tinyCode.add(inst);
		}
		else if (tmpIR.op1Type == 1 && tmpIR.op2Type == 0)
		{
			tmp = tmpIR.op2.replace("$T", "r");
			if (tmpIR.opCode == opCodeType.MULTI)
			{
				inst = Tiny.tiny_MULINTEGER + tmpIR.op1 +  " " +tmp ;
			}
			else if (tmpIR.opCode == opCodeType.MULTF)
			{
				inst = Tiny.tiny_MULFLOAT + tmpIR.op1 +  " " +tmp ;
			}
			CodeGen.tinyCode.add(inst);
		}
		else
		{
			if (tmpIR.opCode == opCodeType.MULTI)
			{
				inst = Tiny.tiny_MULINTEGER + tmpIR.op1.replace("$T", "r") +  " " + tmpIR.op2.replace("$T", "r") ;
			}
			else if (tmpIR.opCode == opCodeType.MULTF)
			{
				inst = Tiny.tiny_MULFLOAT + tmpIR.op1.replace("$T", "r") +  " " + tmpIR.op2.replace("$T", "r") ;
			}
			CodeGen.tinyCode.add(inst);
		}
	}
	
	private void generateDIVCode (IR tmpIR) {
		
		String tmp = "";
		String inst = "";
		
		// If both the operands are from memory then SUB needs two instructions.
		if (tmpIR.op1Type == 1 && tmpIR.op2Type == 1)
		{
			// Move one of the variables to the result
			tmp = tmpIR.result.replace("$T", "r");
			inst = Tiny.tiny_STORE + tmpIR.op1 +  " " +tmp ;
			CodeGen.tinyCode.add(inst);
			if (tmpIR.opCode == opCodeType.DIVI)
			{
				inst = Tiny.tiny_DIVINTEGER + tmpIR.op2 +  " " +tmp ;
			}
			else if (tmpIR.opCode == opCodeType.DIVF)
			{
				inst = Tiny.tiny_DIVFLOAT + tmpIR.op2 +  " " +tmp ;
			}
			CodeGen.tinyCode.add(inst);
		}
		else if (tmpIR.op1Type == 0 && tmpIR.op2Type == 1)
		{
			tmp = tmpIR.op1.replace("$T", "r");
			if (tmpIR.opCode == opCodeType.DIVI)
			{
				inst = Tiny.tiny_DIVINTEGER + tmpIR.op2 +  " " +tmp ;
			}
			else if (tmpIR.opCode == opCodeType.DIVF)
			{
				inst = Tiny.tiny_DIVFLOAT + tmpIR.op2 +  " " +tmp ;
			}
			CodeGen.tinyCode.add(inst);
		}
		else if (tmpIR.op1Type == 1 && tmpIR.op2Type == 0)
		{
			tmp = tmpIR.result.replace("$T", "r");
			inst = Tiny.tiny_STORE + tmpIR.op1 + " " + tmp ;
			CodeGen.tinyCode.add(inst);
			
			if (tmpIR.opCode == opCodeType.DIVI)
			{
				inst = Tiny.tiny_DIVINTEGER + tmpIR.op2.replace("$T", "r") + " " + tmp ;
			}
			else if (tmpIR.opCode == opCodeType.DIVF)
			{
				inst = Tiny.tiny_DIVFLOAT + tmpIR.op2.replace("$T", "r") + " " + tmp ;
			}
			CodeGen.tinyCode.add(inst);
		}
		else
		{
			if (tmpIR.opCode == opCodeType.DIVI)
			{
				inst = Tiny.tiny_DIVINTEGER + tmpIR.op1.replace("$T", "r") +  " " + tmpIR.op2.replace("$T", "r") ;
			}
			else if (tmpIR.opCode == opCodeType.DIVF)
			{
				inst = Tiny.tiny_DIVFLOAT + tmpIR.op1.replace("$T", "r") +  " " + tmpIR.op2.replace("$T", "r") ;
			}
			CodeGen.tinyCode.add(inst);
		}
	}
	
	private void generateReadCode(IR tmpIR) {

		String inst="";
		if ( tmpIR.opCode == opCodeType.READI)
			CodeGen.tinyCode.add(tiny_READINTEGER + tmpIR.op1);
		else
			CodeGen.tinyCode.add(tiny_READFLOAT + tmpIR.op1);
	}
	
	private void generateWriteCode(IR tmpIR) {

		String inst="";
		if ( tmpIR.opCode == opCodeType.WRITEI)
			CodeGen.tinyCode.add(tiny_WRITEINTEGER + tmpIR.op1);
		else
			CodeGen.tinyCode.add(tiny_WRITEFLOAT + tmpIR.op1);
	}
	
	private void generateFunEndCode(IR tmpIR) {
		
		CodeGen.tinyCode.add(tiny_UNLINK);
		CodeGen.tinyCode.add(tiny_RETURN);
	}
	
	private void generateEndCode(IR tmpIR) {
		
		CodeGen.tinyCode.add(tiny_END);
	}
	
	public void genTinyCode (IR tmpIR) {
		
		switch (tmpIR.opCode) {
		
		case ADDI:
			generateAddCode (tmpIR);
			break;
		case ADDF:
			generateAddCode (tmpIR);
			break;
		case SUBI:
			generateSubCode (tmpIR);
			break;
		case SUBF:
			generateSubCode (tmpIR);
			break;
		case MULTI:
			generateMULTCode (tmpIR);
			break;
		case MULTF:
			generateMULTCode (tmpIR);
			break;
		case DIVI:
			generateDIVCode (tmpIR);
			break;
		case DIVF:
			generateDIVCode (tmpIR);
			break;
		case READI:
			generateReadCode (tmpIR);
			break;
		case READF:
			generateReadCode (tmpIR);
			break;
		case WRITEI:
			generateWriteCode (tmpIR);
			break;
		case WRITEF:
			generateWriteCode (tmpIR);
			break;
		case STOREI:
			generateStoreCode (tmpIR);
			break;
		case STOREF:
			generateStoreCode (tmpIR);
			break;
		case LABEL:
			generateLabelCode(tmpIR);
			break;
		case FUNEND:
			generateFunEndCode(tmpIR);
			break;
		case END:
			generateEndCode(tmpIR);
			break;
		default:
			//Unhandled type of instruction.
			break;
		}
	}
}


class CodeGen {
	
	static int regIndex = 0;
	static LinkedList irList;
	static HashMap symbolTableInfo;
	static LinkedList irCode = new LinkedList();
	static LinkedList tinyCode = new LinkedList();
	
	public CodeGen(HashMap info, LinkedList ir) {
		symbolTableInfo = info;
		irList = ir;
	}
	
	public static int generate_IR_Tiny() {
		
		IR intRep;
		Tiny tinyObj =  new Tiny();
		String tmp;
		FileWriter outFile = null;;
		PrintWriter outBuffer = null;
		
		generateStartCode();
		ListIterator irItr = irList.listIterator();
	    while (irItr.hasNext())
	    {	
	    	intRep = (IR)irItr.next();
	    	intRep.getThreeAddressInst(intRep);
	    	tinyObj.genTinyCode(intRep);
	    }
	    
	    try {
			outFile = new FileWriter("out.tiny");
			outBuffer = new PrintWriter(outFile);
		}
		catch (IOException e) {
			System.err.println("Fatal IO error: Unable to create output file\n" + e);
			System.exit(2);
		}
		irItr = CodeGen.irCode.listIterator();
	    while (irItr.hasNext())
        {	
	    	tmp = (String) irItr.next();
        	outBuffer.println(";" + tmp);
        	//System.out.println(";" + tmp);
        }
	    
	    irItr = CodeGen.tinyCode.listIterator();
	    while (irItr.hasNext())
        {	
	    	tmp = (String) irItr.next();
        	outBuffer.println(tmp);

        	//System.out.println(tmp);
        }
	    
	    outBuffer.close();
		try {
		outFile.close();
		}
		catch (IOException e) {
			System.err.println("Fatal IO error: Unable to close output file\n" + e);
			System.exit(2);
		}	
		return 0;
	}
	
	private static void generateStartCode() {
		
		declareGlobalVariables();
		backupRegisters(0, "");
	}
	
	private static void declareGlobalVariables () {
		Object key;
		int paramVar = 0;
		Hashtable scopetbl = (Hashtable)symbolTableInfo.get("");
		Enumeration keys = scopetbl.keys();
		SymbolTableInfo symbolInfo;
		while(keys.hasMoreElements())
		{
			key = keys.nextElement();
			symbolInfo = (SymbolTableInfo)scopetbl.get(key);
			tinyCode.add(Tiny.tiny_VAR + symbolInfo.identifierName);
		}
	}
	
	public static int linkIndex(String scopeName) {

		return ((Hashtable)symbolTableInfo.get(scopeName)).size();
	}
	
	public static void backupRegisters(int paramCount, String calledFunc) {
		if (paramCount != 0){
			//To be filled in later
		}
		tinyCode.add(Tiny.tiny_PUSH);
		tinyCode.add(Tiny.tiny_PUSH + " r0");
		tinyCode.add(Tiny.tiny_PUSH + " r1");
		tinyCode.add(Tiny.tiny_PUSH + " r2");
		tinyCode.add(Tiny.tiny_PUSH + " r3");
		
		if (calledFunc == "") {
			tinyCode.add(Tiny.tiny_JMPSR + "main");
			tinyCode.add(Tiny.tiny_HALT);
		}
		else {
			tinyCode.add(Tiny.tiny_JMPSR + calledFunc);
		}
	}
}
