/*
 * Code.cpp
 *
 */

#include "Code.h"

/*
 * basic constructor. creates and initialize maps
 */
Code::Code() {
	destMap = new map<string,string>;
	compMap = new map<string,string>;
	jumpMap = new map<string,string>;
	initializeCompMap();
	initializeDestMap();
	initializeJumpMap();

}

/*
 * destructor
 */
Code::~Code() {
	destMap->clear();
	compMap->clear();
	jumpMap->clear();
	delete destMap;
	delete compMap;
	delete jumpMap;

}

/*
 * given a string represents the destination in the command, change the
 * given binaryCode string with the corresponding binary code
 * mnemonic - the dest string
 * binaryCode - the string to change
 */
void Code::dest(string& mnemonic,string& binaryCode){
	binaryCode = (*destMap)[mnemonic];

}

/*
 * given a string represents the computation in the command, change the
 * given binaryCode string with the corresponding binary code
 * mnemonic - the comp string
 * binaryCode - the string to change
 */
void Code::comp(string& mnemonic,string& binaryCode){
	binaryCode = (*compMap)[mnemonic];
}

/*
 * given a string represents the jump in the command, change the
 * given binaryCode string with the corresponding binary code
 * mnemonic - the jump string
 * binaryCode - the string to change
 */
void Code::jump(string& mnemonic,string& binaryCode){
	binaryCode = (*jumpMap)[mnemonic];
}

/*
 * initializing the destination map with the available options
 */
void Code::initializeDestMap(){
	destMap->insert(pair<string,string>(string(""),string("000")));
	destMap->insert(pair<string,string>(string("M"),string("001")));
	destMap->insert(pair<string,string>(string("D"),string("010")));
	destMap->insert(pair<string,string>(string("MD"),string("011")));
	destMap->insert(pair<string,string>(string("A"),string("100")));
	destMap->insert(pair<string,string>(string("AM"),string("101")));
	destMap->insert(pair<string,string>(string("AD"),string("110")));
	destMap->insert(pair<string,string>(string("AMD"),string("111")));
}

/*
 * initializing the computation map with the available options
 */
void Code::initializeCompMap(){
	compMap->insert(pair<string,string>(string("0"),string("0101010")));
	compMap->insert(pair<string,string>(string("1"),string("0111111")));
	compMap->insert(pair<string,string>(string("-1"),string("0111010")));
	compMap->insert(pair<string,string>(string("D"),string("0001100")));
	compMap->insert(pair<string,string>(string("A"),string("0110000")));
	compMap->insert(pair<string,string>(string("!D"),string("0001101")));
	compMap->insert(pair<string,string>(string("!A"),string("0110001")));
	compMap->insert(pair<string,string>(string("-D"),string("0001111")));
	compMap->insert(pair<string,string>(string("-A"),string("0110011")));
	compMap->insert(pair<string,string>(string("D+1"),string("0011111")));
	compMap->insert(pair<string,string>(string("A+1"),string("0110111")));
	compMap->insert(pair<string,string>(string("D-1"),string("0001110")));
	compMap->insert(pair<string,string>(string("A-1"),string("0110010")));
	compMap->insert(pair<string,string>(string("D+A"),string("0000010")));
	compMap->insert(pair<string,string>(string("D-A"),string("0010011")));
	compMap->insert(pair<string,string>(string("A-D"),string("0000111")));
	compMap->insert(pair<string,string>(string("D&A"),string("0000000")));
	compMap->insert(pair<string,string>(string("D|A"),string("0010101")));
	compMap->insert(pair<string,string>(string("M"),string("1110000")));
	compMap->insert(pair<string,string>(string("!M"),string("1110001")));
	compMap->insert(pair<string,string>(string("-M"),string("1110011")));
	compMap->insert(pair<string,string>(string("M+1"),string("1110111")));
	compMap->insert(pair<string,string>(string("M-1"),string("1110010")));
	compMap->insert(pair<string,string>(string("D+M"),string("1000010")));
	compMap->insert(pair<string,string>(string("D-M"),string("1010011")));
	compMap->insert(pair<string,string>(string("M-D"),string("1000111")));
	compMap->insert(pair<string,string>(string("D&M"),string("1000000")));
	compMap->insert(pair<string,string>(string("D|M"),string("1010101")));

}

/*
 * initializing  the jump map with the available options
 */
void Code::initializeJumpMap(){
	jumpMap->insert(pair<string,string>(string(""),string("000")));
	jumpMap->insert(pair<string,string>(string("JGT"),string("001")));
	jumpMap->insert(pair<string,string>(string("JEQ"),string("010")));
	jumpMap->insert(pair<string,string>(string("JGE"),string("011")));
	jumpMap->insert(pair<string,string>(string("JLT"),string("100")));
	jumpMap->insert(pair<string,string>(string("JNE"),string("101")));
	jumpMap->insert(pair<string,string>(string("JLE"),string("110")));
	jumpMap->insert(pair<string,string>(string("JMP"),string("111")));
}
