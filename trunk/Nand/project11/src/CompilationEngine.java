import java.io.BufferedWriter;
import java.io.IOException;


public class CompilationEngine {

	// data members
	JackTokenizer tok;
	BufferedWriter output;	
	String _className;
	VMWriter writer;
	SymbolTable tbl;
	Keyword _currentSubroutineType;
	boolean _currentSubroutineRetIsVoid;
	
	//if and while counters;
	int ifIDCounter;
	int whileIDCounter;

	// simple ctor
	public CompilationEngine(JackTokenizer t, BufferedWriter out) {
		tok = t;
		output = out;
		writer = new VMWriter(out);
		tbl = new SymbolTable();
		
	}

	/**
	 * this method files a complete class, assuming startComplile cold this method
	 * @throws IOException
	 */
	public void compileClass() throws IOException
	{
		tok.advance();
		_className = tok.identifier();
		assert(tok.TokenType()==TokenType.IDENTIFIER) : "Not class name";
		tok.advance();
		assert(tok.TokenType()==TokenType.SYMBOL) :"No class [{] and didnt get symbol";
		assert(tok.symbol()=='{'):"No class [{]" + "we got: "+ tok.getTokenString();
		tok.advance();
		assert(tok.TokenType() == TokenType.KEYWORD) : "dec-var/ subrut";
		// iterating through class static and field declarations and compiles 
		// the declaration with CompileClassVarDec
		while (tok.keyWord()==Keyword.STATIC || tok.keyWord()==Keyword.FIELD)
		{
			assert(tok.TokenType() == TokenType.KEYWORD) : "dec-var/ subrut";
			variablesChain(false);
		}
		assert(tok.TokenType() == TokenType.KEYWORD) : "expecting subroutine" +
		", we got: " + tok.getTokenString();
		// iterating over all functions, methods and ctors in a class
		while(isSubroutine())
		{
			CompileSubroutine();
		}
		// tok is ON } of the class
		assert(tok.TokenType()==TokenType.SYMBOL) :"No class [}] and didnt get symbol. We get " + tok.getTokenString();
		assert(tok.symbol()=='}'):"No class [}]" + "we got: "+ tok.getTokenString();

		tok.advance();		
		//if (!tok.hasMoreCommands()) System.out.println("EOF reached!");
		
	}

	
	/**
	 * handles with compiling a list of variables.
	 * helps class and none class variables declarations.
	 * assuming we already read the var type and stops after the ';'
	 * @throws IOException
	 */
	private void variablesChain(boolean isVar) throws IOException
	{
		Keyword varType = tok.keyWord();
		Kind kind = Kind.VAR;
		if (!isVar)
		{
			//assert (kStr.equals("static") || kStr.equals("field")) : "static or field expected";
			if (varType == Keyword.STATIC)
				kind = Kind.STATIC;
			else
				kind = Kind.FIELD;
		}
		
		tok.advance();
		// assert(tok.TokenType() == TokenType.KEYWORD || tok.TokenType() == TokenType.IDENTIFIER)
		//: "expecting variable type or class name";
		String type = tok.identifier();
		tok.advance();
		// assert(tok.TokenType() == TokenType.IDENTIFIER): 
		//	"expecting variable name we got: " + tok.getTokenString();
		tbl.define(tok.identifier(), type, kind);
		tok.advance();
		// assert (tok.TokenType()==TokenType.SYMBOL): "expecting next variable ',' or ';'";
		while (tok.symbol()==',')
		{
			tok.advance();
			// assert(tok.TokenType() == TokenType.IDENTIFIER): "expecting variable name";
			tbl.define(tok.identifier(), type, kind);
			tok.advance();
			// assert (tok.TokenType()==TokenType.SYMBOL): "expecting next variable ',' or ';'";
		}
		// assert (tok.symbol()==';'): "expecting ';'";
		// printing ';'
		tok.advance();
	}

	/**
	 * this method is called from compileClass after compiling class vaiables.
	 * assuming tok is on the subroutine's keyword (before its name).
	 * @throws IOException
	 */
	public void CompileSubroutine() throws IOException {
		
		_currentSubroutineType =  tok.keyWord();

		tbl.startSubroutine();
		tok.advance();
		assert (tok.TokenType()==TokenType.IDENTIFIER ||
				tok.TokenType()==TokenType.KEYWORD): "expecting type/void subroutine type";
		
		if (tok.TokenType()==TokenType.KEYWORD && tok.keyWord() == Keyword.VOID)
			_currentSubroutineRetIsVoid = true;
		else _currentSubroutineRetIsVoid = false;
		
		tok.advance();
		 assert (tok.TokenType()==TokenType.IDENTIFIER): "expecting sub name";
		String subName = tok.identifier();
		
		System.out.println("compiling subroutine: " + subName);
		
		tok.advance();
		 assert (tok.TokenType()==TokenType.SYMBOL): "expecting '(";

		compileParameterList();
		
		 assert (tok.TokenType()==TokenType.SYMBOL): "expecting ')";
		tok.advance();
		 assert(tok.TokenType()==TokenType.SYMBOL): "expecting '{'";
		tok.advance();
		while (tok.TokenType() == TokenType.KEYWORD && tok.keyWord()==Keyword.VAR)
		{
			 assert(tok.keyWord()==Keyword.VAR) : "expecting var dec";
			 variablesChain(true);
		}
		writer.writeFunction(_className + "." + subName, tbl.varCount(Kind.VAR));
		ifIDCounter=-1;
		whileIDCounter=-1;
				
		if (_currentSubroutineType == Keyword.CONSTRUCTOR){
			writer.writePush("constant",tbl.varCount(Kind.FIELD));
			writer.writeCall("Memory.alloc",1);
			writer.writePop("pointer", 0);			
		}
		else if (_currentSubroutineType == Keyword.METHOD){
			writer.writePush("argument",0);
			writer.writePop("pointer",0);
		}
		
		// token is on first statement
		compileStatements();
		 assert(tok.TokenType()==TokenType.SYMBOL): "expecting '}'";
		tok.advance();
		// is NOW ON } that closes the subroutine
	}



	/**
	 * compiles a sequence of statements, not including the enclosing {}.
	 * this method is called even of there is nothing between the {}
	 * assuming token is on the beginning of the first statement or on '}'
	 * leaving when tok is on '}'
	 * @throws IOException
	 */
	public void compileStatements() throws IOException {
		// we are already on what suppose to be first statement
		Keyword statement = takeStatement();
		while (statement!=Keyword.INVALID)
		{
			switch (statement)
			{
			case DO:
				compileDo();
				break;
			case LET:
				compileLet();
				break;
			case WHILE:
				whileIDCounter++;
				compileWhile();
				break;
			case RETURN:
				compileReturn();
				break;
			case IF:
				ifIDCounter++;
				compileIF();
				break;
			}
			statement = takeStatement();
		}
	}

	/**
	 * compiles an if statement, possibly with a trailing else clause. 
	 * assuming tok is on if and finishing when tok is on next statement or
	 * after the end of statements block
	 * @throws IOException
	 */
	public void compileIF() throws IOException {
		int currentIntNumber = ifIDCounter;
		tok.advance();
		expectingSymbol('(');
		 assert ((tok.TokenType() == TokenType.SYMBOL && tok.symbol() != ')')
				|| (tok.TokenType()!= TokenType.SYMBOL)):"expecting expression";
		CompileExpression();
		expectingSymbol(')');
		writer.writeIf("IF_TRUE"+currentIntNumber);
		writer.writeGoto("IF_FALSE" + currentIntNumber);
		expectingSymbol('{');
		writer.writeLabel("IF_TRUE"+currentIntNumber);
		compileStatements();
		expectingSymbol('}');
		
		if (tok.TokenType() == TokenType.KEYWORD)
			if (tok.keyWord() == Keyword.ELSE)
			{
				writer.writeGoto("IF_END"+currentIntNumber);
				writer.writeLabel("IF_FALSE"+currentIntNumber);
				tok.advance();
				expectingSymbol('{');
				compileStatements();
				expectingSymbol('}');
				writer.writeLabel("IF_END"+currentIntNumber);

			}
			else writer.writeLabel("IF_FALSE"+currentIntNumber);
		else writer.writeLabel("IF_FALSE"+currentIntNumber);
	}
	
	/**
	 * compiles a return statement.
	 *  assuming tok is on 'return' and finishing when tok is on next 
	 *  statement or after the end of statements block
	 * @throws IOException
	 */
	public void compileReturn() throws IOException {
		tok.advance();
		if (tok.TokenType() != TokenType.SYMBOL)
			CompileExpression();
		else
			if (tok.symbol() != ';')
				CompileExpression();
			else writer.writePush("constant",0);
					
		expectingSymbol(';');
		writer.writeReturn();
	}
	/**
	 * compiles a while statement.
	 *  assuming tok is on 'while' and finishing when tok is on next 
	 *  statement or after the end of statements block
	 * @throws IOException
	 */
	public void compileWhile() throws IOException {
		int currentWhileID = whileIDCounter;
		writer.writeLabel("WHILE_EXP"+currentWhileID);
		tok.advance();
		expectingSymbol('(');
		// assert ((tok.TokenType() == TokenType.SYMBOL && tok.symbol() != ')')
		//		|| (tok.TokenType()!= TokenType.SYMBOL)):"expecting expression";
		CompileExpression();

		expectingSymbol(')');
		writer.writeArithmetic('~',true);
		writer.writeIf("WHILE_END"+currentWhileID);
		expectingSymbol('{');
		compileStatements();
		expectingSymbol('}');
		writer.writeGoto("WHILE_EXP"+currentWhileID);
		writer.writeLabel("WHILE_END"+currentWhileID);
	}

	/**
	 * this method expecting a symbol and then printing it.
	 * with // asserts enable this is VERY useful
	 * prints symbol token and advances token.
	 * @param symbol the symbol that tok is expected to be on.
	 * @throws IOException
	 */
	private void expectingSymbol(char symbol) throws IOException
	{
		// assert(tok.TokenType()==TokenType.SYMBOL) : "expecting " + Character.toString(symbol)
		//+ "but we get: " + tok.getTokenString();
		// assert(tok.symbol()==symbol):"checking " + Character.toString(symbol) +
		//"but we get: " + tok.getTokenString();
		//printCurrentTokenAl(); // printing symbol
		tok.advance();
	}
	/**
	 * compiles a let statement.
	 *  assuming tok is on 'let' and finishing when tok is on next 
	 *  statement or after the end of statements block
	 * @throws IOException
	 */
	public void compileLet() throws IOException {
		tok.advance();
		// assert(tok.TokenType()==TokenType.IDENTIFIER) : "expecting var name";
		String varName = tok.identifier();
		tok.advance();
		// assert(tok.TokenType()==TokenType.SYMBOL) : "expecting '[' or '='";
			
		//case of assignment to variable
		if (tok.symbol()=='='){
			tok.advance();
			CompileExpression();
			writePushPopVar(varName, false);			
		}
		else{ // case of assignment to array
			// already checked that it is symbol
			// assert(tok.symbol()=='['):"checking '['";
			tok.advance();
			CompileExpression();
			writePushPopVar(varName,true);
			writer.writeArithmetic('+',false);
			// assert(tok.TokenType()==TokenType.SYMBOL) : "expecting ']'";
			tok.advance();
			
						
			// doing the '=' part
			expectingSymbol('=');
			CompileExpression();		
			writer.writePop("temp", 0);
			writer.writePop("pointer",1);
			writer.writePush("temp",0);
			writer.writePop("that",0);			
		}
		expectingSymbol(';');
	}

	/**
	 * compiles a do statement.
	 *  assuming tok is on 'do' and finishing when tok is on next 
	 *  statement or after the end of statements block
	 *  do statement as well as CompileTerm use subroutineCall.
	 * @throws IOException
	 */
	public void compileDo() throws IOException {
		tok.advance();
		 assert(tok.TokenType()==TokenType.IDENTIFIER) : "expecting a class name / sub name";
		// saving the identifier's call and peaking ahead for the subroutineCall
		String name = new String(tok.identifier());
		tok.advance();
		subroutineCall(name);
		writer.writePop("temp", 0);
		expectingSymbol(';');
	}
	
	/**
	 * Helps to compile a do statement and a term.
	 * Assuming the token is on the next symbol that makes the decision
	 * how to continue parsing. Finishes when tok is on what comes after
	 * the ')' of the call (maybe on ';'). 
	 * @param name is a class name or a subroutine name.
	 * @throws IOException
	 */
	private void subroutineCall(String name) throws IOException {
		// assert(tok.TokenType() == TokenType.SYMBOL) : "expecting ')' or '(' or '.'";
		// assert(tok.symbol()=='(' || tok.symbol()==')' || tok.symbol()=='.')
		//: "illegal subroutine call expected '(' / ')' / '.', but got: " + tok.getTokenString();
		
		String callSub=null;
		int nArgs = -2;
		switch (tok.symbol())
		{
		case '(':
			callSub = _className + "." + name;
			tok.advance();

			writer.writePush("pointer",0);
			nArgs = CompileExpressionList()+1;
			break;
		case '.':
			tok.advance();
			assert(tok.TokenType()==TokenType.IDENTIFIER) : "expecting a sub name";
			String subName = tok.identifier();
			tok.advance();
			assert(tok.TokenType() == TokenType.SYMBOL) : "expecting '('";
			tok.advance();
			if (tbl.typeOf(name) != null){
				writePushPopVar(name, true);
				nArgs = CompileExpressionList() +1;
				callSub = tbl.typeOf(name) + "." + subName;
			}
			else{ //not var =  static function
				nArgs = CompileExpressionList();
				callSub = name + "." + subName;
			}			
			break;
		}
		assert (nArgs>=0 && callSub!=null) 
		: "nArgs must be non-negative, and callSun must be not null";
		writer.writeCall(callSub, nArgs);
		expectingSymbol(')');		
	}
	


	/**
	 * compiles a (possibly empty) comma-separated list of expressions.
	 * This is called even if no expression is in '(' ')'.
	 * Assuming tok is already on first expression or on '(' if
	 * no expression on between brackets.
	 * Finishes when token is on ')'.
	 * @throws IOException
	 */
	public int CompileExpressionList() throws IOException {
		int listLength =0;
		if (tok.TokenType() == TokenType.SYMBOL){
			if (tok.symbol()!=')'){
				listLength+=help_complileList();
			}
		}
		else listLength+=help_complileList();

		return listLength;
	}

	/**
	 * iterating over all expressions in the list, 
	 * and helps to CompileExpressionList. 
	 * Assuming that tok is on first expression and finishes when tok is
	 * on ')' after all expressions.
	 * @throws IOException
	 */
	private int help_complileList() throws IOException
	{
		// according to the assumptions, we have at least one arg
		int counter =1;
		CompileExpression();
		assert(tok.TokenType() == TokenType.SYMBOL) :
		"expecting ')' or ',' or'(' , but we got: " + tok.getTokenString();
		while (tok.symbol()==','){
			counter++;
			tok.advance();
			CompileExpression();
			// assert(tok.TokenType() == TokenType.SYMBOL) : "expecting ')' or ','";
		}
		return counter;
	}

	/**
	 * compiles an expression.
	 * @throws IOException
	 */
	public void CompileExpression() throws IOException {
		CompileTerm();
		// after the last token of term
		char op  = checkAndPrintOp();
		while(op!='?'){
			CompileTerm();
			switch (op){
			case '*':
				writer.writeCall("Math.multiply",2);
				break;
			case '/':
				writer.writeCall("Math.divide",2);
				break;
			default:{
				writer.writeArithmetic(op,false);
				break;
			}
			}
			op  = checkAndPrintOp();		
		}
	}

	/**
	 * the method checks if tok is on binary operator.
	 * if it is, it prints the operator and advances tok.
	 * @return true if tok is on binary operator. false otherwise.
	 * @throws IOException
	 */
	private char checkAndPrintOp() throws IOException {
		if (tok.TokenType()!= TokenType.SYMBOL) return '?';
		char c=tok.symbol();
		if (c=='+' || c== '-' || c== '*' || c=='/' || 
				c== '&' || c== '|'  || c== '<' || c== '>' || c== '=' )
		{
			tok.advance();
			return c;
		}
		return '?';
	}
	/**
	 * the method checks if tok is on unary operator.
	 * if it is, it prints the operator and advances tok.
	 * @return the unaryOp char if tok is an unary operator. null otherwise.
	 * @throws IOException
	 */
	private Character checkAndPrintUnariOp() throws IOException {
		if (tok.TokenType()!= TokenType.SYMBOL) return null;
		char c=tok.symbol();
		if (c=='~' || c=='-')
		{
			tok.advance();
			return c;
		}
		return null;
	}

	/**
	 * the method writes the VM commands handling String constant
	 * @param str the string
	 * @throws IOException
	 */
	private void pushStringConstant(String str) throws IOException
	{
		writer.writePush("constant", str.length());
		writer.writeCall("String.new", 1);
		for (int i=0;i<str.length(); i++)
		{
			writer.writePush("constant", str.charAt(i));
			writer.writeCall("String.appendChar", 2);
		}
	}
	
	/**
	 * compiles a term.  This method is faced 
	 * with a slight difficulty when trying to 
	 * decide between some of the alternative 
	 * rules.  Specifically, if the current token 
	 * is an identifier, it must still distinguish 
	 * between a variable, an array entry, and 
	 * a subroutine call. The distinction can be 
	 * made by looking ahead one extra token.  
	 * A single look-ahead token, which may 
	 * be one of '[', '(', '.' ,  suffices to 
	 * distinguish between the three 
	 * possibilities. It uses subroutineCall to compile a call.
	 * Any other token is not part of this term and should not be 
	 * advanced over. 
	 * @throws IOException
	 */
	public void CompileTerm() throws IOException {
		TokenType t = tok.TokenType();
		switch (t){ 
		case INT_CONST:
			writer.writePush("constant", tok.intVal());
			tok.advance();
			break;
		case STRING_CONST:
			pushStringConstant(tok.stringVal());
			tok.advance();
			break;
		case KEYWORD:
			checkAndPrintKeywordConstant();
			break;			
		case SYMBOL:
			Character unaryOp =checkAndPrintUnariOp();
			if (unaryOp != null){
				CompileTerm();
				writer.writeArithmetic(unaryOp,true);
				break;
			}

			if (tok.symbol()=='('){
				tok.advance();
				CompileExpression();
				expectingSymbol(')');
				break;
			}
			assert(false) : "should not get here, tok: "+tok.getTokenString();
			break;
		case IDENTIFIER:
			String prevIden = new String(tok.identifier());
			tok.advance();
			if (tok.TokenType() == TokenType.SYMBOL){
				char sym = tok.symbol();
				if (sym == '.' || sym == '(' || sym == '['){
					switch(sym){
					case '[':
						tok.advance();
						CompileExpression();
						writePushPopVar(prevIden,true);
						writer.writeArithmetic('+',false);
						writer.writePop("pointer", 1);
						writer.writePush("that", 0);
						expectingSymbol(']');
						break;
					case '(':
						subroutineCall(prevIden);
						break;
					case '.':
						subroutineCall(prevIden);
						break;
						
					}
					// assert(sym == '.' || sym == '(' || sym == '[') 
				//	: "Never get here- expectong '[' / '(' / '.' we got: "+sym;
				}
				else writePushPopVar(prevIden,true);						
			}
		}
	}
	
	/**
	 * used to write the command to push or pop a variable
	 * @param var the variable
	 * @param isPush true if we need to push. False otherwise.
	 * @throws IOException
	 */
	private void writePushPopVar(String var,boolean isPush) throws IOException{
		
		String segment = null;
		int indexOfVar = tbl.indexOf(var);
		
		switch (tbl.kindOf(var)){ 
			case VAR:{
				segment = "local";
				break;
			}
			case ARG:{
				segment = "argument";
				if (_currentSubroutineType == Keyword.METHOD)
					indexOfVar =  tbl.indexOf(var)+1;
				else indexOfVar =  tbl.indexOf(var);
				break;
			}
			case STATIC:{
				segment = "static";
				break;
			}
			case FIELD:{
				segment = "this";
				break;
			}
		}
		
		if (isPush)
			writer.writePush(segment,indexOfVar);
		else writer.writePop(segment,indexOfVar);
		
		
	}
	
	
	/**
	 * the method checks if tok is on constant keyword.
	 * if it is, it prints the constant keyword and advances tok.
	 * @return true if tok is on constant keyword. false otherwise.
	 * @throws IOException
	 */
	private boolean checkAndPrintKeywordConstant() throws IOException {
		// we already know that tok is on symbol
		Keyword k = tok.keyWord();
		switch (k){
		case TRUE:{
			writer.writePush("constant", 0);
			writer.writeArithmetic('~', true);
			break;
		}
		case FALSE:{
			writer.writePush("constant",0);
			break;
		}
		case NULL:{
			writer.writePush("constant",0);
			break;

		}
		case THIS:{
			writer.writePush("pointer",0);	
			break;
		}
			
		}
		
		if (k==Keyword.TRUE || k==Keyword.FALSE || k==Keyword.NULL || k==Keyword.THIS)
		{
			tok.advance();
			return true;
		}
		return false;
	}

	
	private Keyword takeStatement() {
		if (tok.TokenType() != TokenType.KEYWORD) return Keyword.INVALID;
		Keyword temp = tok.keyWord();
		if (temp == Keyword.DO || 
				temp == Keyword.LET ||
				temp == Keyword.WHILE ||
				temp == Keyword.RETURN ||
				temp == Keyword.IF)
		{
			return temp;
		}

		return Keyword.INVALID;

	}


	/**
	 * compiles a (possibly empty) parameter
	 * list, not including the enclosing "()".
	 * called even if  ().
	 * assuming tok is on first parameter.
	 * Finishes when tok is in ')'.
	 * @throws IOException
	 */
	public void compileParameterList() throws IOException {
		tok.advance();
		if (tok.TokenType()!=TokenType.SYMBOL) {
			help_ParameterList();
			//print type
			//advance
			//print name
			// advance 
			while(tok.symbol()==',')
			{
				// print ','
				//advance
				//print type
				//advance
				//print name
				// advance
				expectingSymbol(',');
				help_ParameterList();
			}

		}
	}

	/**
	 * helps to parse parameter list.
	 * @throws IOException
	 */
	private void help_ParameterList() throws IOException
	{
		// assert (tok.TokenType()==TokenType.KEYWORD
		//		|| tok.TokenType()==TokenType.IDENTIFIER
		//		) : "expecting type / class, we got: "+ tok.getTokenString();
		String type = tok.identifier();
		tok.advance();
		// assert (tok.TokenType()==TokenType.IDENTIFIER) : "expecting param name";
		tbl.define(tok.identifier(), type, Kind.ARG);
		tok.advance();
	}
	/**
	 * Calls CompileClass to start compiling a class.
	 * prints the first tokens and moves tok to be on class name.
	 * @throws IOException
	 */
	public void startCompile() throws IOException
	{
		// moves token to the first word- class keyword
		tok.advance();
		 assert(tok.getTokenString().equals("class")):"file dont have class";
		if (tok.TokenType()==TokenType.KEYWORD)
			if (tok.keyWord()==Keyword.CLASS)
				compileClass();
	}

	/**
	 * Is tok on Subroutine keyword?
	 * @return True if it is. Else, otherwise.
	 */
	private boolean isSubroutine()
	{
		Keyword k = tok.keyWord();
		return k==Keyword.METHOD || k==Keyword.CONSTRUCTOR || k==Keyword.FUNCTION;
	}
	
	public void testWithExpr() throws IOException
	{
		tok.advance();
		compileIF();
	}
	
}
