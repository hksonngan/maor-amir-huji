/*
 * Parser.cpp
*/

#include "Parser.h"
#define NOTFOUND -1
#define JUMP_SYMBOL ';'
#define DEST_SYMBOL '='
#define A_CMD_SYMBOL '@'
#define L_CMD_SYMBOL '('

/*
 * iterating over the stream finding the next command to read
 */
void Parser::moveTosNextCommand()
{
	bool found_cmd=false;
	string tempString;
	while (!inputStream->eof() && !found_cmd)
	{
		char nextChar = inputStream->peek();
		if (nextChar == '\r' || nextChar == '/' || nextChar == '\n')
			getline(*inputStream,tempString);
		else
			if (nextChar == ' ' || nextChar == EOF || nextChar == '\t')
				inputStream->get();
			else found_cmd=true;

	}
}

/*
 * basic constructor. Gets a pointer to the file stream.
 */
Parser::Parser(ifstream* inStream) {
	inputStream  = inStream;
	moveTosNextCommand();

}

/*
 * destructor
 */
Parser::~Parser(){}

/*
 * returns true if there are more commands in the input stream
 */
bool Parser::hasMoreCommands(){
	return !inputStream->eof();
}

/*
 * Reads the next command from the input and makes it the current
 * command. Should be called only if hasMoreCommands() is true.
 */
void Parser::advance(){
	getline(*inputStream,currentCommand);
	moveTosNextCommand();
}

/*
 * Returns the type of the current command:
 * A_COMMAND for @Xxx where Xxx is either a symbol or a
 * decimal number.
 * C_COMMAND for dest=comp;jumt
 * L_COMMAND for (Xxx) command where Xxx is a symbol
 */
int Parser::commandType(){
	if (currentCommand.at(0) == A_CMD_SYMBOL)
		return A_COMMAND;
	else if (currentCommand.at(0)== L_CMD_SYMBOL)
			return L_COMMAND;
		 else return C_COMMAND;
}

/*
 * changes the input string to the symbol or decimal Xxx of the current command.
 * Should be called only when commandType() is A_COMMAND or
 * L_COMMAND.
 * symbol - the string to change
 */
void Parser::symbol(string& symbol){
	char currentChar;
	symbol.clear();
	if (commandType()==A_COMMAND || commandType()==L_COMMAND)
	{
		unsigned int index=1;
		while (index < currentCommand.length())
		{
			currentChar = currentCommand[index];
			if (currentChar!='\n' && currentChar!='/' && currentChar != '\r'){
				if (currentChar != ' ' && currentChar != '\t' && currentChar != ')')
					symbol.append(1,currentChar);
				index++;
			}
			else break;
		}

	}

}

/*
 * Changes the input string to the dest mnemonic in the current
 * command. Should be called only when commandType() is
 * C_COMMAND
 * symbol - the string to change
 */
void Parser::dest(string& symbol){
	unsigned int index=0;
	char currentChar;
	symbol.clear();

	//case the command contains no dest mnemonic
	if (currentCommand.find(DEST_SYMBOL) == string::npos || currentCommand.find("//") < currentCommand.find(DEST_SYMBOL))
		return;

	//parsing the mnemonic from the command
	while (index < currentCommand.find(DEST_SYMBOL)){
		currentChar = currentCommand[index];
		if (currentChar != ' ' && currentChar != '\t')
			symbol.append(1,currentChar);
		index++;
	}
}

/*
 * Change the given string to the comp mnemonic in the current
 * C_COMMAND.Should be called only when commandType() is
 * C_COMMAND
 * symbol - the string to change.
 */
void Parser::comp(string& symbol){
	unsigned int compIndex=currentCommand.find(DEST_SYMBOL);
	unsigned int compEndIndex=currentCommand.find(JUMP_SYMBOL);
	char currentChar;
	symbol.clear();

	//determine starting index
	if (compIndex <= currentCommand.length())
		compIndex = compIndex+1;
	else compIndex=0;

	//determine ending index
	if (compEndIndex <= currentCommand.length())
		compEndIndex = compEndIndex-1;
	else compEndIndex = currentCommand.length()-2;


	//parsing the mnemonic from the command
	while (compIndex <= compEndIndex){
		currentChar = currentCommand[compIndex];
		if (currentChar!='\n' && currentChar!='/' && currentChar != '\r'){
			if (currentChar != ' ' && currentChar != '\t')
				symbol.append(1,currentChar);
			compIndex++;
		}
		else break;
	}
}

/*
 * Change the given string to the jump mnemonic in the current
 * C_COMMAND. Should be called only when commandType() is
 * C_COMMAND
 * symbol - the string to change.
 */
void Parser::jump(string& symbol){
	unsigned int jumpStartIndex = currentCommand.find(JUMP_SYMBOL)+1;
	char currentChar;
	symbol.clear();

	//case the command contains no jump mnemonic
	if (currentCommand.find(JUMP_SYMBOL) == string::npos || currentCommand.find("//") < jumpStartIndex)
		return;


	//parsing the mnemonic from the command
	while (jumpStartIndex < currentCommand.length()){
		currentChar = currentCommand[jumpStartIndex];
		if (currentChar != '\r' && currentChar !='/' && currentChar != '\n'){
			if (currentChar != ' ' && currentChar != '\t')
				symbol.append(1,currentChar);
			jumpStartIndex++;
		}
		else break;
	}
}
