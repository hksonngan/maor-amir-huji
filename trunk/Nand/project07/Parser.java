
import java.util.HashMap;
import java.util.Scanner;





/*
 * Handles the parsing of a single  .vm file, and encapsulates access to the input code. It reads VM 
 * commands, parses them, and provides convenient access to their components. In addition, it removes all 
 * white space and comments. 
 */
public class Parser {

	//private variables
	Scanner file;
	String[] currentCommand;
	String nextCommand;
	HashMap<String, CommandType> commandMap;
	
	/*
	 * initializing commandMap with a pair of command and 
	 * corresponding CommandType.
	 */
	private void initializeCommandMap(){
		commandMap = new HashMap<String, CommandType>();
		commandMap.put(new String("add"), CommandType.C_ARITHMETIC);
		commandMap.put(new String("sub"), CommandType.C_ARITHMETIC);
		commandMap.put(new String("neg"), CommandType.C_ARITHMETIC);
		commandMap.put(new String("eq"), CommandType.C_ARITHMETIC);
		commandMap.put(new String("gt"), CommandType.C_ARITHMETIC);
		commandMap.put(new String("lt"), CommandType.C_ARITHMETIC);
		commandMap.put(new String("and"), CommandType.C_ARITHMETIC);
		commandMap.put(new String("or"), CommandType.C_ARITHMETIC);
		commandMap.put(new String("not"), CommandType.C_ARITHMETIC);
		commandMap.put(new String("push"), CommandType.C_PUSH);
		commandMap.put(new String("pop"), CommandType.C_POP);
		
	}
	
	/*
	 * given a string represents a line in the file,the function checks
	 * if it is a legal command line(non empty and doesnt start with //)
	 * line - the line to check
	 * RETURNS: true if the line is a legal command, false otherwise.
	 */
	private boolean checkIsCommand(String line){
		//checking that the line is not empty and that "//" is not the beginning of the 
		//first word
		String trimmedLine = line.trim();
		if (!trimmedLine.isEmpty() && !trimmedLine.substring(0,2).equals(new String("//")))
				return true;
		else return false;
	}
	
	
	/*
	 * used to advance to the next command in file. If another command exists,
	 * 'nextCommand' will be set to the command string.Otherwise,'nextCommand' will be
	 * set to null. 
	 */
	private void advanceToNextCommand(){
		String currentLine;
		while (file.hasNextLine()){
				currentLine = file.nextLine();
				if (checkIsCommand(currentLine)){
					nextCommand = currentLine;
					return;
				}
				
		}
		nextCommand = null;
		file.close();
	}
	
	/*
	 * Construcor.
	 * inputFile - a scanner object represents the
	 * opened input file.
	 */
	Parser(Scanner inputFile){
		file = inputFile;
		nextCommand = null;
		initializeCommandMap();
		advanceToNextCommand();
	}
	
	/*
	 * Returns true if there are move commands in the file
	 */
	public boolean hasMoreCommands(){
		
		if (nextCommand != null)
			return true;
		else return false;
	}
	
	/*
	 * Reads the next command from the input and 
	 * makes it the current command. Should be called
	 * only if hasMoreCommands() is true. Initially
	 * there is no current command.
	 */
	public void advance(){
		currentCommand = nextCommand.trim().split("\\s+");
		advanceToNextCommand();
	}
	
	/*
	 * Returns the type of the current VM command.
	 * C_ARITHMETIC is returned for all the 
	 * arithmetic commands.
	 */
	public CommandType commandType(){
		if (currentCommand[0].indexOf("//") != NOT_FOUND)
			currentCommand[0] = currentCommand[0].substring(0,currentCommand[0].indexOf("//"));
		return commandMap.get(currentCommand[0]);
		
	}
	
	/*
	 * Returns the first argument of the current command. In the case
	 * of C_ARITHMETIC, the command itself is returned.
	 * Sould not be called if the current command is C_RETURN.
	 * assuming that commandType() was called before the call of this function.
	 */
	public String arg1(){
		if (commandMap.get(currentCommand[0]) == CommandType.C_ARITHMETIC)
			return currentCommand[0];
		else
		{
			if (currentCommand[1].indexOf("//") == NOT_FOUND)
					return currentCommand[1];
			else return currentCommand[1].substring(0,currentCommand[1].indexOf("//"));
		}

	}
	
	/*
	 * Returns the second argument of the current command. Should be called
	 * only if the current command is C_PUSH,C_POP,C_FUNCTION,C_CALL
	 */
	public int arg2(){
		String arg2;
		if (currentCommand.length >= 2)
		{
			if (currentCommand[2].indexOf("//") == NOT_FOUND)
				arg2 =  currentCommand[2];
			else arg2= currentCommand[2].substring(0,currentCommand[2].indexOf("//"));
			return new Integer(arg2).intValue();
		}
		else return NOT_FOUND;
	}
	
	
	
	
	
	//private constants
	private static final int NOT_FOUND = -1;
	
	
}
