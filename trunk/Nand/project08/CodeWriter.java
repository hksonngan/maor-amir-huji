import java.io.BufferedWriter;
import java.io.IOException;

/*
 * Translates VM commands into Hack assembly code.
 */
public class CodeWriter  {

	//private constant
	private static final String tempPos = "@R5";
	
	//private integer to use in the Labeling
	private int labelNumber;
	
	//private integer to save a unique number of call command(for a unique return address label)
	private int callNumber;
	
	//private output file object
	private BufferedWriter outFileBuffer;
	
	//string that saves the currentFileName(for static variables)
	private String currentFile;
	
	//string that saves the currentFunctionName
	private String currentFunction;
	
	
	//private function to write new line
	private void writeLine(String line) throws IOException {
		outFileBuffer.write(line);
		outFileBuffer.newLine();
		outFileBuffer.flush();

	}
	
	/*
	 * private function that writes the lines to move the sp to the
	 * next empty slot.
	 */
	private void writeIncreaseSP() throws IOException{
		writeLine("@SP");
		writeLine("M=M+1");
	}
	
	/*
	 * private function that writes the prefix of all Binary functions.At the end
	 * D contains the second argument (y) and M contains the first argument(x).
	 * RAM[0] is pointing to the place of the first argument
	 * 
	 */
	private void writeBinaryPrefix() throws IOException{
		writeLine("@SP");
		writeLine("AM=M-1");
		writeLine("D=M");
		writeLine("@SP");
		writeLine("AM=M-1");
	}
	
	/*
	 * private function that writes the prefix of all Unary functions.At the end
	 * M contains the argument (y) 
	 * RAM[0] is pointing to the place of the first argument
	 * 
	 */
	private void writeUnaryPrefix() throws IOException{
		writeLine("@SP");
		writeLine("AM=M-1");
	}

	
	/*private functions to write arithmetic commands*/
	private void writeAdd() throws IOException{
		writeBinaryPrefix();
		writeLine("M=M+D");
		writeIncreaseSP();
		
	}
	private void writeSub() throws IOException{
		writeBinaryPrefix();
		writeLine("M=M-D");
		writeIncreaseSP();
	}
	private void writeNeg() throws IOException{
		writeUnaryPrefix();
		writeLine("M=-M");
		writeIncreaseSP();
	}
	private void writeEq() throws IOException{
		writeBinaryPrefix();
		writeLine("D=M-D");
		writeLine("@EQJUMP_" + labelNumber);
		writeLine("D;JEQ");
		writeLine("D=0");
		writeLine("@EQEND_" + labelNumber);
		writeLine("0;JMP");
		writeLine("(EQJUMP_"+labelNumber+")");
		writeLine("D=-1");
		writeLine("(EQEND_"+labelNumber+")");
		writeLine("@SP");
		writeLine("A=M");
		writeLine("M=D");
		writeIncreaseSP();	
		labelNumber++;
	}
	private void writeGt() throws IOException{
		writeBinaryPrefix();
		writeLine("D=M-D");
		writeLine("@GTJUMP_" + labelNumber);
		writeLine("D;JGT");
		writeLine("D=0");
		writeLine("@GTEND_" + labelNumber);
		writeLine("0;JMP");
		writeLine("(GTJUMP_"+labelNumber+")");
		writeLine("D=-1");
		writeLine("(GTEND_"+labelNumber+")");
		writeLine("@SP");
		writeLine("A=M");
		writeLine("M=D");
		writeIncreaseSP();
		labelNumber++;
	}
	private void writeLt() throws IOException{
		writeBinaryPrefix();
		writeLine("D=M-D");
		writeLine("@LTJUMP_"+labelNumber);
		writeLine("D;JLT");
		writeLine("D=0");
		writeLine("@LTEND_"+labelNumber);
		writeLine("0;JMP");
		writeLine("(LTJUMP_"+labelNumber+")");
		writeLine("D=-1");
		writeLine("(LTEND_"+labelNumber+")");
		writeLine("@SP");
		writeLine("A=M");
		writeLine("M=D");
		writeIncreaseSP();	
		labelNumber++;
	}
	private void writeAnd() throws IOException{
		writeBinaryPrefix();
		writeLine("M=D&M");
		writeIncreaseSP();
		
	}
	private void writeOr() throws IOException{
		writeBinaryPrefix();
		writeLine("M=D|M");
		writeIncreaseSP();
		
	}
	private void writeNot() throws IOException{
		writeUnaryPrefix();
		writeLine("M=!M");
		writeIncreaseSP();
	}
	
	/*
	 * constructor.
	 * outputFile - the stream where to write the translated code
	 */
	public CodeWriter(BufferedWriter outputFile){
		outFileBuffer = outputFile;
		labelNumber=0;
		callNumber=0;
		currentFunction = null;
	}
	
	/*
	 * informs the output file that a new file is being translated
	 * fileName - the new file name
	 */
	public void setFileName(String fileName){
		currentFile = fileName;
	}
	
	/*
	 * Writes the assembly code that is the translation
	 * of the given arithmetic command
	 * command - the arithmetic command to translate
	 */
	public void writeArithmetic(String command) throws IOException {
		if (command.equals("add")) {writeAdd(); return;}
		if (command.equals("sub")) {writeSub(); return;}
		if (command.equals("neg")) {writeNeg(); return;}
		if (command.equals("eq")) {writeEq(); return;}
		if (command.equals("gt")) {writeGt(); return;}
		if (command.equals("lt")) {writeLt(); return;}
		if (command.equals("and")) {writeAnd(); return;}
		if (command.equals("or")) {writeOr(); return;}
		if (command.equals("not")) {writeNot(); return;}	
		
	}
	
	
	
	/*
	 * Assuming that the required data to push to stack is located in D
	 * register, updates the stack with D and increase the sp.
	 */
	private void pushDRegToStack() throws IOException{
		writeLine("@SP");
		writeLine("A=M");
		writeLine("M=D");
		writeIncreaseSP();
	}
	
	/*
	 * used for writing the code for pushing a value from *(segment+index) to the stack.
	 * segment - the segment to use(will be used with LCL,ARG,THIS,THAT)
	 * index - the offset in the segment of the data to push
	 */
	private void writePushFromPointedSegment(String segment,int index) throws IOException{
		writeLine("@"+index);
		writeLine("D=A");
		writeLine("@"+segment);
		writeLine("A=D+M");
		writeLine("D=M");
		pushDRegToStack();
	}
	
	/*
	 * used for writing the code for poping a value from stack and writing it in *(segment+index).
	 * segment - the segment to use(will be used with LCL,ARG,THIS,THAT)
	 * index - the offset in the segment of the data to write the popped data from stack.
	 */
	private void writePopToPointedSegment(String segment, int index) throws IOException{
		writeLine("@"+index);
		writeLine("D=A");
		writeLine("@"+segment);
		writeLine("D=D+M");
		writeLine("@R13");
		writeLine("M=D");		
		writeLine("@SP");
		writeLine("AM=M-1");
		writeLine("D=M");
		writeLine("@R13");
		writeLine("A=M");
		writeLine("M=D");
		
	}
	
	
	
	/*
	 * Deals with C_PUSH commands. Writes the required translated Assembly code into the output buffer.
	 * segment - the segment from where to push the data.
	 * index - the index in the segment
	 */
	private void writePushCommand(String segment , int index) throws IOException{
		if (segment.equals("constant")) {
			writeLine("@"+index);
			writeLine("D=A");
			pushDRegToStack();
			return;
		}
		
		if (segment.equals("local")){
			writePushFromPointedSegment("LCL", index);
			return;
		}
		
		if (segment.equals("argument")){
			writePushFromPointedSegment("ARG", index);
			return;
		}
		
		if (segment.equals("this")){
			writePushFromPointedSegment("THIS", index);
			return;
		}
		
		if (segment.equals("that")){
			writePushFromPointedSegment("THAT", index);
			return;
		}
		
		if (segment.equals("pointer")){
			if (index==0)
				writeLine("@THIS");
			else writeLine("@THAT");
			writeLine("D=M");				
			pushDRegToStack();
			return;
		}
		
		if (segment.equals("temp")){
			writeLine("@"+index);
			writeLine("D=A");
			writeLine(tempPos);
			writeLine("A=D+A");
			writeLine("D=M");
			pushDRegToStack();
			return;
		}
		
		if (segment.equals("static")){
			writeLine("@"+currentFile + "." + index);
			writeLine("D=M");
			pushDRegToStack();
			return;
		}
	}
	

	
	/*
	 * Deals with C_POP commands. Writes the required translated Assembly code into the output buffer.
	 * segment - the segment from where to saved the poped data.
	 * index - the index in the segment
	 */
	private void writePopCommand(String segment,int index) throws IOException{
		if (segment.equals("local")){
			writePopToPointedSegment("LCL", index);
			return;
		}
		
		if (segment.equals("argument")){

			writePopToPointedSegment("ARG", index);
			return;
		}
		
		if (segment.equals("this")){
			writePopToPointedSegment("THIS", index);
			return;
		}
		
		if (segment.equals("that")){
			writePopToPointedSegment("THAT", index);
			return;
		}
		
		if (segment.equals("pointer")){
			writeLine("@SP");
			writeLine("AM=M-1");
			writeLine("D=M");
			if (index==0)
				writeLine("@THIS");
			else writeLine("@THAT");		
			writeLine("M=D");				
			return;
		}
		
		if (segment.equals("temp")){			
			writeLine("@"+index);
			writeLine("D=A");
			writeLine(tempPos);
			writeLine("D=D+A");
			writeLine("@R13");
			writeLine("M=D");
			writeLine("@SP");
			writeLine("AM=M-1");
			writeLine("D=M");
			writeLine("@R13");
			writeLine("A=M");
			writeLine("M=D");
			return;
		}
		
		if (segment.equals("static")){
			writeLine("@SP");
			writeLine("AM=M-1");
			writeLine("D=M");
			writeLine("@"+currentFile + "." + index);
			writeLine("M=D");
			return;
		}
		
	}
	
	/*
	 * Write the assembly code that is the translation of the
	 * given command,where command is either C_PUSH or C_POP
	 * command - the push/pop command type
	 * segment - the segment(second argument) in the command
	 * index - the index(third argument) in the command
	 */
	public void WritePushPop(CommandType command, String segment, int index) throws IOException{
		if (command == CommandType.C_PUSH)
			writePushCommand(segment,index);
		else if (command == CommandType.C_POP)
				writePopCommand(segment,index);
		
		return;
	}
	
	/* Writes assembly code that effects the VM initialization(bootstrap code). The code must
	 * be placed at the beginning of the output file.
	 */
	public void writeInit() throws IOException{
		//SP=256
		writeLine("@256");
		writeLine("D=A");
		writeLine("@SP");
		writeLine("M=D");
		//call Sys.init
		writeCall(new String("Sys.init"),0);
	}
	
	/*
	 * Writes assembly code that effects the label command.
	 * label - the label to write
	 */
	public void writeLabel(String label) throws IOException{
		//if we are not writing a code inside a function then null$label will be written
		writeLine("("+ currentFunction+"$"+label + ")");		
	}
	
	/*
	 * Writes aseembly code that effects the goto command.
	 * label - the label to jump to its place.
	 */
	public void writeGoto(String label) throws IOException {
		//if we are not writing a code inside a function then null$label will be written
		writeLine("@"+ currentFunction+"$"+label);
		writeLine("0;JMP");
	}
	
	/*
	 * Writes assembly code that effects the if-goto command.
	 * label - the label to jump to (in case of true condition in the stack)
	 */
	public void writeIf(String label) throws IOException{
		//if we are not writing a code inside a function then null$label will be written
		writeLine("@SP");
		writeLine("AM=M-1");
		writeLine("D=M"); 
		writeLine("@" + currentFunction+"$"+label);
		writeLine("D;JNE");
	}
	
	/*
	 * Writes assembly code that effects the call command.
	 * functionName - the name of the function to call
	 * numArgs - number of arguments in the call command
	 */
	public void writeCall(String functionName, int numArgs) throws IOException {
		//pushing unique return address label
		writeLine("@CALL_"+callNumber);
		writeLine("D=A");
		pushDRegToStack();
		//push LCL
		writeLine("@LCL");
		writeLine("D=M");
		pushDRegToStack();
		//push ARG
		writeLine("@ARG");
		writeLine("D=M");
		pushDRegToStack();
		//push THIS
		writeLine("@THIS");
		writeLine("D=M");
		pushDRegToStack();
		//push THAT
		writeLine("@THAT");
		writeLine("D=M");
		pushDRegToStack();
		//set ARG to SP-numArgs-5
		writeLine("@"+(numArgs+5));
		writeLine("D=A");
		writeLine("@SP");
		writeLine("D=M-D");
		writeLine("@ARG");
		writeLine("M=D");
		//set LCL=SP
		writeLine("@SP");
		writeLine("D=M");
		writeLine("@LCL");
		writeLine("M=D");
		//write goto f
		writeLine("@"+functionName);
		writeLine("0;JMP");		
		//write (return-address)
		writeLine("(CALL_"+callNumber+")");
		callNumber++;		
	}
	
	/*
	 * Writes assembly code that effects the return command.
	 */
	public void writeReturn() throws IOException{
		//FRAME=LCL (@R15 = FRAME)
		writeLine("@LCL");
		writeLine("D=M");
		writeLine("@R15");
		writeLine("M=D");
		//RET = *(FRAME-5) (@R14-RET)
		writeLine("@5");
		writeLine("A=D-A");
		writeLine("D=M");
		writeLine("@R14");
		writeLine("M=D");
		//*ARG = pop()
		writePopCommand(new String("argument"),0);
		//SP=ARG+1
		writeLine("@ARG");
		writeLine("D=M+1");
		writeLine("@SP");
		writeLine("M=D");
		//THAT = *(FRAME-1)
		writeLine("@R15");
		writeLine("AM=M-1");
		writeLine("D=M");
		writeLine("@THAT");
		writeLine("M=D");
		//THIS = *(FRAME-2)
		writeLine("@R15");
		writeLine("AM=M-1");
		writeLine("D=M");
		writeLine("@THIS");
		writeLine("M=D");
		//ARG = *(FRAME-3)
		writeLine("@R15");
		writeLine("AM=M-1");
		writeLine("D=M");
		writeLine("@ARG");
		writeLine("M=D");
		//LCL = *(FRAME-4)
		writeLine("@R15");
		writeLine("AM=M-1");
		writeLine("D=M");
		writeLine("@LCL");
		writeLine("M=D");
		//goto RET
		writeLine("@R14");
		writeLine("A=M");
		writeLine("0;JMP");
		
		
	}
	
	/*
	 * Writes assembly code that effects the function command.
	 * functionName - the name of the function 
	 * numLocals - the number of local variables in the function
	 */
	public void writeFunction(String functionName, int numLocals) throws IOException{
		currentFunction = functionName;
		writeLine("("+functionName+")");
		for (int k=0;k<numLocals;k++)
			writePushCommand(new String("constant"),0);
	}
	

	/*
	 * Closes the output file
	 */
	public void close() throws IOException{
		outFileBuffer.close();
	}
	
}
