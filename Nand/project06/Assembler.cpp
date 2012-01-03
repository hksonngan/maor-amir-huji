/*
 * Assembler.cpp
 */


#include "Parser.h"
#include "Code.h"
#include "SymbolTable.h"
#include <stdlib.h>
#include <iostream>
#include <fstream>

using namespace std;

#define SUCCESS 0
#define FAIL 1
#define A_COMMAND_BIT_NUM 16
#define STARTING_VARIABLE_INDEX 16


/*
 * given the file name, open the input and output streams required to read the assembly file
 * and write the hack file.
 * argv - the array of argument to the program
 * inputStream - the stream of the input file
 * outputStream - the stream of the output file.
 * return FAIL in case something went wrong, SUCCESS otherwise.
 */
static int openStreams(const char* argv[],ifstream& inputStream,ofstream& outputStream)
{
	inputStream.open(argv[1],ifstream::in);
	if (inputStream.fail())
	{
		cout << "Cannot open assembly file. Exiting...\n";
		return FAIL;
	}
	//the input file name
	string fileNameNoSuffix(argv[1]);

	outputStream.open(fileNameNoSuffix.erase(fileNameNoSuffix.find(".asm"),4).append(".hack").c_str(),ofstream::out);
	if (outputStream.fail())
	{
		cout << "Cannot create hack file. Exiting...\n";
		inputStream.close();
		return FAIL;
	}
	return SUCCESS;

}

/*
 * given an integer, change str to be the binary representation of the
 * number.
 * number - the integer
 * str - the string where to save the binary representation.
 */
static void binary(int number, string& str) {
	int remainder;

	if(number == 1) {
		str.append("1");
		return;
	}
	else if (number == 0){
				str.append("0");
				return;
			}

	remainder = number%2;
	binary(number >> 1,str);
	str.append(remainder==1 ? "1" : "0");
}


/*
 * running over the file given in the inputStream, updating the given symbolTable
 * with the symbols in the file. (first run over the file)
 * symbolTable - the symbol table to update
 * inputString - the stream of the file
 */
void buildSymbolTable(SymbolTable& symbolTable,ifstream& inputStream)
{
	int currentCommand=0;
	Parser first_parse(&inputStream);
	string symbol;
	//iterating over the commands in the file, searching for L_COMMANDs (symbol commands)
	while (first_parse.hasMoreCommands()){
		first_parse.advance();
		if (first_parse.commandType()==L_COMMAND){
			first_parse.symbol(symbol);
			symbolTable.addEntry(symbol,currentCommand);
		}
		else currentCommand++;
	}
	inputStream.close();
}



/*
 * main
 */
int main(int argc,const char *argv[])
{

	//checking the validity of the input
	if (argc != 2 ){
		cout << "Usage: Assmbler <file_name.asm>\n";
		return FAIL;
	}

	//opening the input and output file streams
	ifstream inputFileStream;
	ofstream outputFileStream;
	if (openStreams(argv,inputFileStream,outputFileStream) == FAIL)
		return FAIL;


	//initialize and constructing symbol table
	SymbolTable symbolTable;
	buildSymbolTable(symbolTable,inputFileStream);


	//reopen input stream
	inputFileStream.open(argv[1],ifstream::in);
		if (inputFileStream.fail())
		{
			cout << "Cannot open assembly file. Exiting...\n";
			return FAIL;
		}

	//initializing parser and coder instances
	Parser parser(&inputFileStream);
	Code coder;

	//helping variables
	string dest,comp,jump,symbol;
	string destCode,compCode,jumpCode;
	int symbol_num;
	int current_variable_index=STARTING_VARIABLE_INDEX;

	//running over the code, command by command (second run over the file)
	while (parser.hasMoreCommands()){
		parser.advance();
		switch (parser.commandType()){
		case L_COMMAND:
			break;

		case C_COMMAND:{
			parser.dest(dest);
			coder.dest(dest,destCode);
			parser.comp(comp);
			coder.comp(comp,compCode);
			parser.jump(jump);
			coder.jump(jump,jumpCode);
			outputFileStream << string("111") << compCode << destCode << jumpCode << endl;
			break;
			}
		case A_COMMAND:{
			symbol.clear();
			parser.symbol(symbol);

			//case of number in command
			if (symbol.at(0) >= '0' && symbol.at(0) <= '9')
				symbol_num = atoi(symbol.c_str());
			else{ //case of symbol or variable
				if (symbolTable.contains(symbol))
					symbol_num = symbolTable.getAddress(symbol);
				else{
					symbolTable.addEntry(symbol,current_variable_index);
					symbol_num=current_variable_index;
					current_variable_index++;
					}

			}
			symbol.clear();
			//getting the binary representation of the address
			binary(symbol_num,symbol);
			symbol.insert(0,A_COMMAND_BIT_NUM-symbol.length(),'0');
			outputFileStream << symbol.c_str() << endl;
			break;
		}
		}
	}
	//closing open streams
	inputFileStream.close();
	outputFileStream.close();
	return SUCCESS;
}
