import org.antlr.runtime.*;
import org.antlr.runtime.tree.*;

import java.lang.String;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Enumeration;
import java.util.Map;
import java.util.LinkedList;
import java.util.ListIterator;
import java.io.*;

public class Micro {

	// Main method
	public static void main (String[] args){

		boolean bException = false;
		// Check if the cmdline input is provided
		if (args.length == 0)
		{
			System.out.println("Input parameter missing: Please provide file path");
			System.exit(1);
		}

		CharStream cs = null;
		MicroLexer lexer = null; // Scanner object
		HashMap symbolTableInfo;
		String scopeName;
		Hashtable symbols;
		SymbolTableInfo symbolInfo;
		LinkedList irList;
		Object key;
		String str;
		//FileWriter outFile = null;;
		//PrintWriter outBuffer = null;

		try {
			cs = new ANTLRFileStream(args[0]);
		}
		catch (IOException e) {
			System.err.println("Fatal IO error: Unable to read input file\n" + e);
			System.exit(2);
		}
		
		/*try {
			outFile = new FileWriter("out.tiny");
			outBuffer = new PrintWriter(outFile);
		}
		catch (IOException e) {
			System.err.println("Fatal IO error: Unable to create output file\n" + e);
			System.exit(2);
		}*/
		lexer = new MicroLexer (cs);
		CommonTokenStream tokenStream = new CommonTokenStream(lexer);
		MicroParser parser = new MicroParser(tokenStream);
		try {
				MicroParser.program_return ret = parser.program();
				CommonTree t = (CommonTree)ret.getTree();
		        CommonTreeNodeStream nodes = new CommonTreeNodeStream(t);
		        nodes.setTokenStream(tokenStream); 
		        TreeGrammar parseTree = new TreeGrammar(nodes);
		        parseTree.program();
		        CodeGen.irList = parseTree.irList;
		        CodeGen.symbolTableInfo = parseTree.symbolTableInfo;
		        CodeGen.generate_IR_Tiny();
		        // Get the linked list of IR's
		        //irList = parseTree.irList;
		       // ListIterator irItr = irList.listIterator();
		       /* while (irItr.hasNext())
		        {	
		        	CodeGen tmp = (CodeGen)irItr.next();
		        	String irInst=tmp.getThreeAddressInst(tmp);
		        	outBuffer.println(irInst);
		        	System.out.println(tmp.getThreeAddressInst(tmp));
		        }*/
		        // Now that we have the IR ready, time to get generate the tiny code.
		        
				// Good we are done with Parsing, now print the symbol table.
				/*symbolTableInfo = parseTree.symbolTableInfo;
				Iterator hashMapItr = symbolTableInfo.entrySet().iterator();
				while(hashMapItr.hasNext())
				{
					Map.Entry pairs = (Map.Entry)hashMapItr.next();
					scopeName = (String)pairs.getKey();
					symbols = (Hashtable)pairs.getValue();
					if (scopeName.compareTo("") == 0)
					{
						System.out.println("Printing Global Symbol Table");
					}
					else
					{
						System.out.println("Printing Symbol Table for "+scopeName);
					}
					//Now iterate through the symbols for each scope
					Enumeration keys = symbols.keys();
					while(keys.hasMoreElements())
					{
						key = keys.nextElement();
						symbolInfo = (SymbolTableInfo)symbols.get(key);
						if (symbolInfo.type == identifierType.ID_STRING)
						{
							System.out.println("name:" + symbolInfo.identifierName + " type STRING value: " + symbolInfo.strVal);
						}
						else if (symbolInfo.type == identifierType.ID_INTEGER)
						{
							System.out.println("name:" + symbolInfo.identifierName + " type INT");
						}
						else if (symbolInfo.type == identifierType.ID_FLOAT)
						{
							System.out.println("name:" + symbolInfo.identifierName + " type FLOAT");
						}
					}
					System.out.println();
				}*/
		}
		catch(RecognitionException e) {
			System.out.println("Compilation Error encountered.");
			/*outBuffer.close();
			try {
			outFile.close();
			}
			catch (IOException e1) {
				System.err.println("Fatal IO error: Unable to close output file\n" + e1);
				System.exit(2);
			}*/			
			e.printStackTrace();
			bException = true;
		}

		/*if (!bException)
		{
			// No longer need step2 output!
			//System.out.println("Accepted");
		}*/
		/*outBuffer.close();
		try {
		outFile.close();
		}
		catch (IOException e) {
			System.err.println("Fatal IO error: Unable to close output file\n" + e);
			System.exit(2);
		}*/	
	}
}