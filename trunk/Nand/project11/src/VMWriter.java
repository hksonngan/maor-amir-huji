import java.io.BufferedWriter;
import java.io.IOException;

/**
 * Emits VM commands into a file,using the VM command syntax
 */
public class VMWriter {

	
	private BufferedWriter _outStream;
	
	/*
	 * constructor.
	 * out - the stream where to write the VM code
	 */
	public VMWriter(BufferedWriter out) {
		_outStream = out;
	}
	
	
	/*
	 * Writes a VM push command.
	 * seg - the segment to push from
	 * index - the index where to push
	 */
	public void writePush(String seg,int index) throws IOException{
		writeLine("push " + seg + " " + index);
		return;
	}
	
	/*
	 * Writes a VM pop command.
	 * seg - the segment to pop to.
	 * index - the index where to pop
	 */
	public void writePop(String seg, int index) throws IOException{
		writeLine("pop " + seg + " " + index);
		return;
	}
	
	/*
	 * Writes the VM arithmetic command
	 * command - the command to write
	 * isUnary - true if the op is unary. (used for '-')
	 */
	public void writeArithmetic(Character op,boolean isUnary) throws IOException{
		switch (op){
		case '+': {
			writeLine("add");
			break;
		}
		case '-': {
			if (isUnary)
				writeLine("neg");
			else writeLine("sub");
			break;
		}
		case '&':{
			writeLine("and");
			break;
		}
		case '|':{
			writeLine("or");
			break;
		}
		case '<':{
			writeLine("lt");
			break;
		}
		case '>':{
			writeLine("gt");
			break;
		}
		case '=':{
			writeLine("eq");
			break;
		}
		case '~':{
			if (isUnary)
			writeLine("not");
			break;
		}
		}
		return;
	}
		
	/*
	 * Writes a VM label command.
	 * label - the label to write
	 */
	public void writeLabel(String label) throws IOException{
		writeLine("label " + label);
		return;
		
	}
	
	/*
	 * Writes a VM goto command.
	 * label - the label to write
	 */
	public void writeGoto(String label) throws IOException{
		writeLine("goto " + label);
		return;
	}
	
	/*
	 * Writes a VM If-goto command
	 * label - the label to write
	 */
	public void writeIf(String label) throws IOException{
		writeLine("if-goto " + label);
		return;
	}
	
	/*
	 * Writes a VM call command.
	 * name - the name of the function to call
	 * nArgs - the number of arguments
	 */
	public void writeCall(String name,int nArgs) throws IOException{
		writeLine("call " + name + " " + nArgs);
		return;
	}
	
	/*
	 * Writes a VM function command.
	 * name - the name of the function
	 * nLocals - the number of local variables
	 */
	public void writeFunction(String name,int nLocals) throws IOException{
		writeLine("function " + name + " " +  nLocals);
		return;
	}
	
	
	/*
	 * Writes a VM return command
	 */
	public void writeReturn() throws IOException{
		writeLine("return");
		return;
	}
	
	/*
	 * Closes the output file.
	 */
	public void close() throws IOException{
		_outStream.close();
	}
	
	
	//private function to write new line
		private void writeLine(String line) throws IOException {
			_outStream.write(line);
			_outStream.newLine();
			_outStream.flush();

		}
	
}
