import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Stack;


public class CompilationEngine {

	// data members
	JackTokenizer tok;
	BufferedWriter output;
	
	
	String _className;
	VMWriter writer;
	SymbolTable tbl;

	// handles indentations
	int depth;

	
	// simple ctor
	public CompilationEngine(JackTokenizer t, BufferedWriter out) {
		tok = t;
		output = out;
		depth=0;
		writer = new VMWriter(out);
		tbl = new SymbolTable();
		
	}

	/**
	 * this method needs to start be called when we
	 * want to create a simple xxxT.xml tokens
	 * @throws IOException
	 */
	public void simpleOutput() throws IOException
	{
		writeLine("<tokens>");
		tok.advance();
		while ( tok.hasMoreCommands())
		{
			writeLine(tok.token2XML());
			try {
				tok.advance();
			} catch (IOException e) {
				System.out.println("An IO exception has occured");
				e.printStackTrace();
				System.exit(1);
			}
		}
		writeLine("</tokens>");
	}

	/**
	 * simple method for printing line to a file
	 * @param line string to print
	 * @throws IOException
	 */
	private void writeLine(String line) throws IOException {
		output.write(line);
		output.newLine();
		output.flush();

	}

	/**
	 * this method files a complete class, assuming startComplile cold this method
	 * @throws IOException
	 */
	public void compileClass() throws IOException
	{
		//print class
		printCurrentTokenAl();
		tok.advance();
		// print class name
		//printCurrentTokenAl();
		_className = tok.identifier();
		assert(tok.TokenType()==TokenType.IDENTIFIER) : "Not class name";
		tok.advance();
		assert(tok.TokenType()==TokenType.SYMBOL) :"No class [{] and didnt get symbol";
		assert(tok.symbol()=='{'):"No class [{]" + "we got: "+ tok.getTokenString();
		//printCurrentTokenAl();
		tok.advance();
		assert(tok.TokenType() == TokenType.KEYWORD) : "dec-var/ subrut";
		// iterating through class static and field declarations and compiles 
		// the declaration with CompileClassVarDec
		while (tok.keyWord()==Keyword.STATIC || tok.keyWord()==Keyword.FIELD)
		{
			assert(tok.TokenType() == TokenType.KEYWORD) : "dec-var/ subrut";
			CompileClassVarDec();
		}
		assert(tok.TokenType() == TokenType.KEYWORD) : "expecting subroutine" +
		", we got: " + tok.getTokenString();
		// iterating over all functions, methods and ctors in a class
		while(isSubroutine())
		{
			// assert(tok.TokenType() == TokenType.KEYWORD): "expecting subroutine-loop";
			CompileSubroutine();
		}
		// tok is ON } of the class
		assert(tok.TokenType()==TokenType.SYMBOL) :"No class [}] and didnt get symbol";
		assert(tok.symbol()=='}'):"No class [}]" + "we got: "+ tok.getTokenString();
		printCurrentTokenAl();

		//check
		tok.advance();		
		if (!tok.hasMoreCommands()) System.out.println("EOF reached!");
		
	}


	/**
	 * compiles one static declaration or a field declaration.
	 * assuming we read static or field
	 * it stops after ';' int the next declaration 
	 * @throws IOException
	 */
	public void CompileClassVarDec() throws IOException {
		// tok is already on static or field
		//writeLine(getAlignedStatement("<classVarDec>"));
		depth++;
		variablesChain(false);
		depth--;
		//writeLine(getAlignedStatement("</classVarDec>"));
	}

	/**
	 * handles with compiling a list of variables.
	 * helps class and none class variables declarations.
	 * assuming we already read the var type and stops after the ';'
	 * @throws IOException
	 */
	private void variablesChain(boolean isVar) throws IOException
	{
		//printCurrentTokenAl();
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
		//printCurrentTokenAl();
		String type = tok.identifier();
		tok.advance();
		// assert(tok.TokenType() == TokenType.IDENTIFIER): 
		//	"expecting variable name we got: " + tok.getTokenString();
		//printCurrentTokenAl();
		tbl.define(tok.identifier(), type, kind);
		tok.advance();
		// assert (tok.TokenType()==TokenType.SYMBOL): "expecting next variable ',' or ';'";
		while (tok.symbol()==',')
		{
			//printCurrentTokenAl();
			tok.advance();
			// assert(tok.TokenType() == TokenType.IDENTIFIER): "expecting variable name";
			//printCurrentTokenAl();
			tbl.define(tok.identifier(), type, kind);
			tok.advance();
			// assert (tok.TokenType()==TokenType.SYMBOL): "expecting next variable ',' or ';'";
		}
		// assert (tok.symbol()==';'): "expecting ';'";
		// printing ';'
		//printCurrentTokenAl();
		tok.advance();
	}

	/**
	 * this method is called from compileClass after compiling class vaiables.
	 * assuming tok is on the subroutine's keyword (before its name).
	 * @throws IOException
	 */
	public void CompileSubroutine() throws IOException {
		writeLine(getAlignedStatement("<subroutineDec>"));
		depth++;
		// printing sub function type word
		//printCurrentTokenAl();
		Keyword subType =  tok.keyWord();
		tbl.startSubroutine();
		tok.advance();
		assert (tok.TokenType()==TokenType.IDENTIFIER ||
				tok.TokenType()==TokenType.KEYWORD): "expecting type/void subroutine type";
		
		//printCurrentTokenAl();
		tok.advance();
		// assert (tok.TokenType()==TokenType.IDENTIFIER): "expecting sub name";
		String subName = tok.identifier();
		//printCurrentTokenAl();
		tok.advance();
		// assert (tok.TokenType()==TokenType.SYMBOL): "expecting '(";
		//printCurrentTokenAl();

		compileParameterList();
		
		// assert (tok.TokenType()==TokenType.SYMBOL): "expecting ')";
		//printCurrentTokenAl();
		tok.advance();
		//printAligned("<subroutineBody>");
		depth++;
		// assert(tok.TokenType()==TokenType.SYMBOL): "expecting '{'";
		//printCurrentTokenAl();
		tok.advance();
		while (tok.TokenType() == TokenType.KEYWORD && tok.keyWord()==Keyword.VAR)
		{
			// assert(tok.keyWord()==Keyword.VAR) : "expecting var dec";
			compileVarDec();
		}
		if (subType==Keyword.METHOD)
			writer.writeFunction(_className + "." + subName, tbl.varCount(Kind.VAR)+1);
		else
			writer.writeFunction(_className + "." + subName, tbl.varCount(Kind.VAR));
		
		// token is on first statement
		compileStatements();
		// assert(tok.TokenType()==TokenType.SYMBOL): "expecting '}'";
		//printCurrentTokenAl();

		depth--;
		//printAligned("</subroutineBody>");
		depth--;
		//writeLine(getAlignedStatement("</subroutineDec>"));

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
		// didnt check if this is a keywork
		Keyword statement = takeStatement();
		//printAligned("<statements>");
		depth++;
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
				compileWhile();
				break;
			case RETURN:
				compileReturn();
				break;
			case IF:
				compileIF();
				break;
			}
			statement = takeStatement();
		}
		depth--;
		//printAligned("</statements>");
	}

	/**
	 * compiles an if statement, possibly with a trailing else clause. 
	 * assuming tok is on if and finishing when tok is on next statement or
	 * after the end of statements block
	 * @throws IOException
	 */
	public void compileIF() throws IOException {
		printAligned("<ifStatement>");
		depth++;
		// printing if
		printCurrentTokenAl();
		tok.advance();
		expectingSymbol('(');
		// assert ((tok.TokenType() == TokenType.SYMBOL && tok.symbol() != ')')
		//		|| (tok.TokenType()!= TokenType.SYMBOL)):"expecting expression";
		CompileExpression();
		expectingSymbol(')');
		expectingSymbol('{');
		compileStatements();
		expectingSymbol('}');
		if (tok.TokenType() == TokenType.KEYWORD)
			if (tok.keyWord() == Keyword.ELSE)
			{

				printCurrentTokenAl();
				tok.advance();
				expectingSymbol('{');
				compileStatements();
				expectingSymbol('}');

			}



		depth--;
		printAligned("</ifStatement>");

	}
	
	/**
	 * compiles a return statement.
	 *  assuming tok is on 'return' and finishing when tok is on next 
	 *  statement or after the end of statements block
	 * @throws IOException
	 */
	public void compileReturn() throws IOException {
		//printAligned("<returnStatement>");
		depth++;
		// printing return
		//printCurrentTokenAl();
		tok.advance();
		if (tok.TokenType() != TokenType.SYMBOL)
			CompileExpression();
		else
			if (tok.symbol() != ';')
				CompileExpression();

		//expectingSymbol(';');
		writer.writeReturn();
		depth--;
		//printAligned("</returnStatement>");

	}
	/**
	 * compiles a while statement.
	 *  assuming tok is on 'while' and finishing when tok is on next 
	 *  statement or after the end of statements block
	 * @throws IOException
	 */
	public void compileWhile() throws IOException {
		printAligned("<whileStatement>");
		depth++;
		// printing while
		printCurrentTokenAl();
		tok.advance();
		expectingSymbol('(');
		// assert ((tok.TokenType() == TokenType.SYMBOL && tok.symbol() != ')')
		//		|| (tok.TokenType()!= TokenType.SYMBOL)):"expecting expression";
		CompileExpression();

		expectingSymbol(')');
		expectingSymbol('{');

		compileStatements();

		expectingSymbol('}');
		depth--;
		printAligned("</whileStatement>");

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
		printCurrentTokenAl(); // printing symbol
		tok.advance();
	}
	/**
	 * compiles a let statement.
	 *  assuming tok is on 'let' and finishing when tok is on next 
	 *  statement or after the end of statements block
	 * @throws IOException
	 */
	public void compileLet() throws IOException {
		printAligned("<letStatement>");
		depth++;

		//printing Let
		printCurrentTokenAl();
		tok.advance();
		// assert(tok.TokenType()==TokenType.IDENTIFIER) : "expecting var name";
		printCurrentTokenAl();
		tok.advance();
		// assert(tok.TokenType()==TokenType.SYMBOL) : "expecting '[' or '='";
		printCurrentTokenAl();
		if (tok.symbol()=='=')
		{
			tok.advance();
			CompileExpression();
		}else
		{
			// already checked that it is symbol
			// assert(tok.symbol()=='['):"checking '['";
			tok.advance();
			CompileExpression();
			// assert(tok.TokenType()==TokenType.SYMBOL) : "expecting ']'";
			printCurrentTokenAl();
			tok.advance();
			
			// doing the '=' part
			expectingSymbol('=');
			CompileExpression();
		}

		expectingSymbol(';');
		depth--;
		printAligned("</letStatement>");

	}

	/**
	 * compiles a do statement.
	 *  assuming tok is on 'do' and finishing when tok is on next 
	 *  statement or after the end of statements block
	 *  do statement as well as CompileTerm use subroutineCall.
	 * @throws IOException
	 */
	public void compileDo() throws IOException {
		// tok is on 'Do'
		//printAligned("<doStatement>");
		depth++;
		//printing Do
		//printCurrentTokenAl();
		tok.advance();
		 assert(tok.TokenType()==TokenType.IDENTIFIER) : "expecting a class name / sub name";
		// saving the identifier's call and peaking ahead for the subroutineCall
		String name = new String(tok.identifier());
		tok.advance();
		subroutineCall(name);
		//call class.subName have just been printed
		// now, ignoring return value
		writer.writePop("temp", 0);
		//expectingSymbol(';');
		depth--;
		//printAligned("</doStatement>");

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
			//printAligned("<identifier> " +name+" </identifier>");
			//printCurrentTokenAl();
			//className = _className;	
			//functionName = name;
			
			callSub = _className + "." + name;
			tok.advance();
			nArgs = CompileExpressionList()+1;
			// all arguments already pushed
			writer.writePush("argument",0);
			break;
		case ')':
			// TODO: need to erase
			// nothing to do
			break;
		case '.':
			//printAligned("<identifier> " +name+" </identifier>");
			
			boolean isVar = tbl.kindOf(name)==Kind.VAR;
			
			
			//printCurrentTokenAl();
			tok.advance();
			assert(tok.TokenType()==TokenType.IDENTIFIER) : "expecting a sub name";
			String subName = tok.identifier();
			if(subName.equals("new"))
			{
				//TODO: handle ctor call
				return;
			}
			// THIS IS THE ELSE PART!!
			
			//printCurrentTokenAl();
			tok.advance();
			assert(tok.TokenType() == TokenType.SYMBOL) : "expecting '('";
			//printCurrentTokenAl();
			tok.advance();
			if (isVar)
			{
				nArgs = CompileExpressionList() +1;
				callSub = tbl.typeOf(name) + "." + subName;
				writer.writePush("argument",0);
			}else{ //not var =  static function
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
		//printAligned("<expressionList>");
		depth++;
		int listLength =0;
		if (tok.TokenType() == TokenType.SYMBOL)
		{
			if (tok.symbol()!=')') 
			{
				listLength+=help_complileList();

			}
		}else
		{
			listLength+=help_complileList();
		}

		
		depth--;
		//printAligned("</expressionList>");
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
		while (tok.symbol()==',')
		{
			counter++;
			//printCurrentTokenAl();
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
		//printAligned("<expression>");
		depth++;
		Stack<Character> binaryOps = new Stack<Character>();
		CompileTerm();
		// after the last token of term
		char op  = checkAndPrintOp();
		while(op!='?')
		{
			binaryOps.push(op);
			CompileTerm();
			op  = checkAndPrintOp();
		}
		
		// in case of some calculations without brackets
		while (!binaryOps.isEmpty())
		{
			switch (binaryOps.pop())
			{
				// TODO: complete this switch
				// TODO: move the switch to writeArithmetic!!!
				//TODO: void writeArithmetic( char op)
				case '+':
					writer.writeArithmetic("add");
					break;
				case '-':
					writer.writeArithmetic("sub");
					break;
			}
		}

		depth--;
		//printAligned("</expression>");

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
			printCurrentTokenAl();
			tok.advance();
			return c;
		}
		return '?';
	}
	/**
	 * the method checks if tok is on unary operator.
	 * if it is, it prints the operator and advances tok.
	 * @return true if tok is on unary operator. false otherwise.
	 * @throws IOException
	 */
	private boolean checkAndPrintUnariOp() throws IOException {
		if (tok.TokenType()!= TokenType.SYMBOL) return false;
		char c=tok.symbol();
		if (c=='~' || c=='-')
		{
			printCurrentTokenAl();
			tok.advance();
			return true;
		}
		return false;
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
		printAligned("<term>");
		depth++;
		TokenType t = tok.TokenType();
		switch (t)
		{ // TODO: complete this switch and function for all cases
		case INT_CONST:
			//printCurrentTokenAl();
			writer.writePush("constant", tok.intVal());
			tok.advance();
			break;
		case STRING_CONST:
			printCurrentTokenAl();
			tok.advance();
			break;
		case KEYWORD:
			if(checkAndPrintKeywordConstant())
				break;
			// assert(false) : "should not get here, tok: "+tok.getTokenString();
			
		case SYMBOL:
			if (checkAndPrintUnariOp())
			{
				CompileTerm();
				break;
			}

			if (tok.symbol()=='(')
			{
				printCurrentTokenAl();
				tok.advance();
				CompileExpression();
				expectingSymbol(')');
				break;
			}
			// assert(false) : "should not get here, tok: "+tok.getTokenString();
			break;
		case IDENTIFIER:
			//TODO : ASK - is it possible to call a void function here
			// TODO: if it is - do we need to do something special?
			String prevIden = new String(tok.identifier());

			tok.advance();
			if (tok.TokenType() == TokenType.SYMBOL)
			{
				char sym = tok.symbol();
				if (sym == '.' || sym == '(' || sym == '['){
					switch(sym)
					{
					case '[':
						//print array name
						printAligned("<identifier> " +prevIden + " </identifier>");
						printCurrentTokenAl();
						tok.advance();
						CompileExpression();
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
				else
				{
					//this just an identifier <=> var name
					printAligned("<identifier> " +prevIden + " </identifier>");
				}
				
			}
			else
			{	
				//this just an identifier <=> var name
				//printAligned("<identifier> " +prevIden + " </identifier>");
				String varName = tok.identifier();
				// TODO : typeOf(var name) can also be static or field
				switch (tbl.kindOf(varName))
				{ // TODO: remember that the switch is not done
					case VAR:
						writer.writePush("local", tbl.indexOf(varName));
						break;
					case ARG:
						writer.writePush("argument", tbl.indexOf(varName));
						break;
				}
				
				
				// TODO : typeOf(var name) can also be static or field
				
			}
		}



		depth--;
		printAligned("</term>");

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
		if (k==Keyword.TRUE || k==Keyword.FALSE || k==Keyword.NULL || k==Keyword.THIS)
		{
			printCurrentTokenAl();
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
	 * compiles a var declaration.
	 * uses varChain to iterate on every identifier.
	 * @throws IOException
	 */
	public void compileVarDec() throws IOException {
		// tok is already on var
		printAligned("<varDec>");
		depth++;
		variablesChain(true);
		depth--;
		printAligned("</varDec>");

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
		printAligned("<parameterList>");
		depth++;
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
		depth--;
		printAligned("</parameterList>");

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
		//printCurrentTokenAl();
		String type = tok.identifier();
		tok.advance();
		// assert (tok.TokenType()==TokenType.IDENTIFIER) : "expecting param name";
		tbl.define(tok.identifier(), type, Kind.ARG);
		//printCurrentTokenAl();
		tok.advance();
	}
	/**
	 * Calls CompileClass to start compiling a class.
	 * prints the first tokens and moves tok to be on class name.
	 * @throws IOException
	 */
	public void startCompile() throws IOException
	{
		writeLine(getAlignedStatement("<class>"));
		depth++;
		// moves token to the first word- class keyword
		tok.advance();
		// assert(tok.getTokenString().equals("class")):"file dont have class";
		if (tok.TokenType()==TokenType.KEYWORD)
			if (tok.keyWord()==Keyword.CLASS)
				compileClass();

		depth--;
		writeLine(getAlignedStatement("</class>"));

	}
	/**
	 * Aligning a string 
	 * @param line the string
	 * @return aligned string
	 */
	private String getAlignedStatement(String line)
	{
		String temp = new String();
		for (int i=0; i<depth;i++)
			temp += "  ";
		temp+=line;
		return temp;
	}

	/**
	 * Writes current token to file as an XML token.
	 * @throws IOException
	 */
	private void printCurrentTokenAl() throws IOException
	{
		String temp = getAlignedStatement(tok.token2XML());
		writeLine(temp);
	}

	/**
	 * Write a string to a file and adds the correct indentation.
	 * @param line the string to write
	 * @throws IOException
	 */
	private void printAligned(String line) throws IOException
	{
		String temp = getAlignedStatement(line);
		writeLine(temp);
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
