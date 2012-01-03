/*
 * SymbolTable.cpp
 *
*/

#include "SymbolTable.h"

/*
 * constructor
 */
SymbolTable::SymbolTable() {
	symbolTable = new map<string,int>;
	initializeSymbolTable();
}

/*
 * destructor
 */
SymbolTable::~SymbolTable() {
	symbolTable->clear();
	delete symbolTable;
}

/*
 * Initializing symbol table with predefined symbols
 */
void SymbolTable::initializeSymbolTable()
{
	symbolTable->insert(pair<string,int>(string("SP"),0));
	symbolTable->insert(pair<string,int>(string("LCL"),1));
	symbolTable->insert(pair<string,int>(string("ARG"),2));
	symbolTable->insert(pair<string,int>(string("THIS"),3));
	symbolTable->insert(pair<string,int>(string("THAT"),4));
	symbolTable->insert(pair<string,int>(string("R0"),0));
	symbolTable->insert(pair<string,int>(string("R1"),1));
	symbolTable->insert(pair<string,int>(string("R2"),2));
	symbolTable->insert(pair<string,int>(string("R3"),3));
	symbolTable->insert(pair<string,int>(string("R4"),4));
	symbolTable->insert(pair<string,int>(string("R5"),5));
	symbolTable->insert(pair<string,int>(string("R6"),6));
	symbolTable->insert(pair<string,int>(string("R7"),7));
	symbolTable->insert(pair<string,int>(string("R8"),8));
	symbolTable->insert(pair<string,int>(string("R9"),9));
	symbolTable->insert(pair<string,int>(string("R10"),10));
	symbolTable->insert(pair<string,int>(string("R11"),11));
	symbolTable->insert(pair<string,int>(string("R12"),12));
	symbolTable->insert(pair<string,int>(string("R13"),13));
	symbolTable->insert(pair<string,int>(string("R14"),14));
	symbolTable->insert(pair<string,int>(string("R15"),15));
	symbolTable->insert(pair<string,int>(string("SCREEN"),16384));
	symbolTable->insert(pair<string,int>(string("KBD"),24576));
}


/*
 * adds the pair <symbol,address> to the symbol table.
 * symbol - the symbol (the key)
 * address - the corresponding address (the value)
 */
void SymbolTable::addEntry(string& symbol,int address)
{
	symbolTable->insert(pair<string,int>(string(symbol),address));
}

/*
 * checks if the symbol table contains the given symbol.
 * symbol - the symbol to find
 */
bool SymbolTable::contains(string& symbol)
{
	if (symbolTable->count(symbol)==1)
		return true;
	else return false;
}

/*
 * Returns the address associated with the symbol
 * symbol - the symbol to get its address.
 */
int SymbolTable::getAddress(string& symbol)
{
	return (*symbolTable)[symbol];
}
