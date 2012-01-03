/*
 * SymbolTable.h
 */

#ifndef SYMBOLTABLE_H_
#define SYMBOLTABLE_H_

#include <string>
#include <map>

using namespace std;

/*
 * Keeps a correspondence between symbolic labels and numeric
 *addresses.
*/
class SymbolTable {
public:
	SymbolTable();
	virtual ~SymbolTable();
	void addEntry(string& symbol,int address);
	bool contains(string& symbol);
	int getAddress(string& symbol);


private:
	//the map contains the mapping between the symbol and the integer.
	map<string, int>* symbolTable;

	//initializing the symbol table with predifined symbols
	void initializeSymbolTable();


};

#endif /* SYMBOLTABLE_H_ */
