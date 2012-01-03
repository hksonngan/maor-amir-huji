/*
 * Code.h
 */

#ifndef CODE_H_
#define CODE_H_

#include <string>
#include <map>

using namespace std;

/*
 * Translates Hack assembly language mnemonics into binary codes.
 */
class Code {
public:
	Code();
	virtual ~Code();
	void dest(string& mnemonic,string& binaryCode);
	void comp(string& mnemonic,string& binaryCode);
	void jump(string& mnemonoc,string& binaryCode);
private:
	//maps that saves the mapping from mnemonics strings into binary codes strings
	map<string,string>* destMap;
	map<string,string>* compMap;
	map<string,string>* jumpMap;

	//map initial functions
	void initializeDestMap();
	void initializeCompMap();
	void initializeJumpMap();
};

#endif /* CODE_H_ */
