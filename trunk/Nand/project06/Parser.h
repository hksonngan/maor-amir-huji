/*
 * Parser.h
*/

#ifndef PARSER_H_
#define PARSER_H_

#include <string>
#include <fstream>
using namespace std;

#define A_COMMAND 1
#define C_COMMAND 2
#define L_COMMAND 3





/*
 * Encapsulates access to the input code. Reads an assembly language com-
 * mand, parses it, and provides convenient access to the command’s components
 * (fields and symbols). In addition, removes all white space and comments.
 */
class Parser {
public:
	Parser(ifstream* inStream);
	virtual ~Parser();
	bool hasMoreCommands();
	void advance();
	int commandType();
	void symbol(string& symbol);
	void dest(string& dest);
	void comp(string& comp);
	void jump(string& jump);

private:
	//the stream to parse
	ifstream* inputStream;
	//the current command(after calling to advance())
	string currentCommand;
	//iterating over the stream, finding the next command to read(but not reading it yet)
	void moveTosNextCommand();

};

#endif /* PARSER_H_ */
