
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;





/*
 * Main  class. Creates the translation of the given .vm file/ directory
 * to assmembly code
 */
public class VMtranslator {

	//private constants
	private static final String ASM_SUFFIX = ".asm";
	private static final String VM_SUFFIX = ".vm";
	
	/*
	 * method for translating a given file from VM language to Hack Assembly language.
	 * inputFile - the file to translate
	 * outFile - the CodeWriter object where to write the translated code
	 */
	private static void translateFile(CodeWriter outFile , File inputFile) throws IOException
	{		
		outFile.setFileName(inputFile.getName());
		
		Parser fileParser = new Parser(inputFile);
		while (fileParser.hasMoreCommands()){
			fileParser.advance();
			switch (fileParser.commandType()){
			case C_ARITHMETIC:{
				outFile.writeArithmetic(fileParser.arg1());
				break;
			}
			case C_POP:{
				outFile.WritePushPop(CommandType.C_POP,fileParser.arg1(),fileParser.arg2());
				break;
			}
			case C_PUSH:{
				outFile.WritePushPop(CommandType.C_PUSH,fileParser.arg1(),fileParser.arg2());
				break;
			}
			case C_LABEL:{
				outFile.writeLabel(fileParser.arg1());
				break;
			}
			case C_GOTO:{
				outFile.writeGoto(fileParser.arg1());
				break;
			}
			case C_IF:{
				outFile.writeIf(fileParser.arg1());
				break;
			}
			case C_CALL:{
				outFile.writeCall(fileParser.arg1(),fileParser.arg2());
				break;
			}
			case C_FUNCTION:{
				outFile.writeFunction(fileParser.arg1(),fileParser.arg2());
				break;
			}
			case C_RETURN:{
				outFile.writeReturn();
				break;
			}
			}	
		}	
	}
	
	/*
	 * given a File object represents a direcory, iterates over the directory, searching for
	 * .vm files and translating them.
	 * directory - the given directory 
	 * outFile - the CodeWriter object where to write the translated code
	 */
	private static void translateDirectory(CodeWriter outFile , File directory) throws IOException
	{
		File[] filesInDirectory = directory.listFiles();
		for (File file : filesInDirectory)
		{
			if (file.isDirectory())
				translateDirectory(outFile, file);
			else if (file.getName().substring(file.getName().length()-3).equals(VM_SUFFIX)) translateFile(outFile,file);
		}
	}
	
	/*
	 * Main
	 */
	public static void main(String[] args) {
		
		File source;
		String outputFileName;
		CodeWriter codeWriter = null;
		
		
		//checking that we have the right number of arguments
		if (args.length != 1){
			System.out.println("Usage: VMtranslator <source>");
			return;
		}
		
		try{
		//opening source file
		source = new File(args[0]);
		if (!source.exists()){
			System.out.println("ERROR: source file doesn't exists. Exiting...");
			return;
		}
		
		//creating outputFile name.
		if (source.isDirectory())	
			outputFileName = new String(args[0]+"/"+source.getName()+ASM_SUFFIX);
		else outputFileName = new String(args[0].substring(0,args[0].length()-3) + ASM_SUFFIX);
	
			
		
		//creating CodeWriter object
		codeWriter = new CodeWriter(new BufferedWriter(new FileWriter(outputFileName)));
		//adding bootstrap code
		codeWriter.writeInit();
		
		//translating given file/files(in case of directory)
		if (source.isDirectory())
			translateDirectory(codeWriter,source);
		else translateFile(codeWriter,source);
		
		codeWriter.close();
		}
		
		catch (Exception e){
			System.out.println("Error reading/writing to files. Exiting...");	
			
		}
		return;
		
	}

}
