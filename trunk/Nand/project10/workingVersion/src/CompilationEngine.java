import java.io.BufferedWriter;
import java.io.IOException;


public class CompilationEngine {

	// data members
	JackTokenizer tok;
	BufferedWriter output;

	int depth;

	public CompilationEngine(JackTokenizer t, BufferedWriter out) {
		tok = t;
		output = out;
		depth=0;
	}

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
				// TODO Auto-generated catch block
				System.out.println("An IO exception has occured");
				e.printStackTrace();
				//System.exit(1);
			}
		}
		writeLine("</tokens>");
	}

	public void simpleOutput2() throws IOException
	{
		writeLine("<tokens>");
		tok.advance();
		while ( tok.hasMoreCommands())
		{
			switch(tok.TokenType())
			{
			case IDENTIFIER:
				writeLine("<identifier> "+tok.identifier() + " </identifier>");
				break;
			case INT_CONST:
				writeLine
				("<integerConstant> "+Integer.toString(tok.intVal()) + " </integerConstant>");
				break;
			case KEYWORD:
				writeLine
				("<keyword> "+tok.stringVal() + " </keyword>");
				break;
			case STRING_CONST:
				writeLine("<stringConstant> "+tok.stringVal() + " </stringConstant>");
				break;
			case SYMBOL:
				char d = tok.symbol();
				String toPrint= Character.toString(d);
				if (d=='&' || d=='<' || d=='>' || d=='"')
				{
					switch(d){
					case '&':
						toPrint = "&amp;";
						break;
					case '<':
						toPrint = "&lt;";
						break;
					case '>':
						toPrint = "&gt;";
						break;
					case '"':
						toPrint = "&quot;";
						break;
					}
				}
				writeLine("<symbol> "+toPrint + " </symbol>");
				break;
			}
			try {
				tok.advance();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.println("An IO exception has occured");
				e.printStackTrace();
				//System.exit(1);
			}
		}
		writeLine("</tokens>");
	}

	private void writeLine(String line) throws IOException {
		output.write(line);
		output.newLine();
		output.flush();

	}

	public void compileClass() throws IOException
	{
		//print class
		printCurrentTokenAl();
		tok.advance();
		// print class name
		printCurrentTokenAl();
		assert(tok.TokenType()==TokenType.IDENTIFIER) : "Not class name";
		tok.advance();
		assert(tok.TokenType()==TokenType.SYMBOL) :"No class [{] and didnt get symbol";
		assert(tok.symbol()=='{'):"No class [{]" + "we got: "+ tok.getTokenString();
		printCurrentTokenAl();
		tok.advance();
		assert(tok.TokenType() == TokenType.KEYWORD) : "dec-var/ subrut";
		while (tok.keyWord()==Keyword.STATIC || tok.keyWord()==Keyword.FIELD)
		{
			assert(tok.TokenType() == TokenType.KEYWORD) : "dec-var/ subrut";
			CompileClassVarDec();
		}
		assert(tok.TokenType() == TokenType.KEYWORD) : "expecting subroutine" +
		", we got: " + tok.getTokenString();
		while(isSubroutine())
		{
			assert(tok.TokenType() == TokenType.KEYWORD): "expecting subroutine-loop";
			CompileSubroutine();
		}
		// tok is ON } of the class
		assert(tok.TokenType()==TokenType.SYMBOL) :"No class [}] and didnt get symbol";
		assert(tok.symbol()=='}'):"No class [}]" + "we got: "+ tok.getTokenString();
		printCurrentTokenAl();


		// TODO: remove this check when submitting
		//tok.advance();
		//System.out.println(tok.getTokenString());
		tok.advance();		
		if (!tok.hasMoreCommands()) System.out.println("EOF reached!");
	}

	/*
	private void CompileClassVarDec() throws IOException {
		// tok is already on static or field
		writeLine(getAlignedStatement("<classVarDec>"));
		depth++;
		printCurrentTokenAl();
		tok.advance();
		assert(tok.TokenType() == TokenType.KEYWORD || tok.TokenType() == TokenType.IDENTIFIER)
		: "expecting variable type or class name";
		printCurrentTokenAl();
		tok.advance();
		assert(tok.TokenType() == TokenType.IDENTIFIER): 
			"expecting variable name we got: " + tok.getTokenString();

		printCurrentTokenAl();
		tok.advance();
		assert (tok.TokenType()==TokenType.SYMBOL): "expecting next variable ',' or ';'";
		while (tok.symbol()==',')
		{
			printCurrentTokenAl();
			tok.advance();
			assert(tok.TokenType() == TokenType.IDENTIFIER): "expecting variable name";
			printCurrentTokenAl();
			tok.advance();
			assert (tok.TokenType()==TokenType.SYMBOL): "expecting next variable ',' or ';'";
		}
		assert (tok.symbol()==';'): "expecting ';'";
		// printing ';'
		printCurrentTokenAl();
		tok.advance();
		depth--;
		writeLine(getAlignedStatement("</classVarDec>"));
	}*/
	private void CompileClassVarDec() throws IOException {
		// tok is already on static or field
		writeLine(getAlignedStatement("<classVarDec>"));
		depth++;
		variablesChain();
		depth--;
		writeLine(getAlignedStatement("</classVarDec>"));
	}


	private void variablesChain() throws IOException
	{
		printCurrentTokenAl();
		tok.advance();
		assert(tok.TokenType() == TokenType.KEYWORD || tok.TokenType() == TokenType.IDENTIFIER)
		: "expecting variable type or class name";
		printCurrentTokenAl();
		tok.advance();
		assert(tok.TokenType() == TokenType.IDENTIFIER): 
			"expecting variable name we got: " + tok.getTokenString();

		printCurrentTokenAl();
		tok.advance();
		assert (tok.TokenType()==TokenType.SYMBOL): "expecting next variable ',' or ';'";
		while (tok.symbol()==',')
		{
			printCurrentTokenAl();
			tok.advance();
			assert(tok.TokenType() == TokenType.IDENTIFIER): "expecting variable name";
			printCurrentTokenAl();
			tok.advance();
			assert (tok.TokenType()==TokenType.SYMBOL): "expecting next variable ',' or ';'";
		}
		assert (tok.symbol()==';'): "expecting ';'";
		// printing ';'
		printCurrentTokenAl();
		tok.advance();
	}

	//TODO: remove this when submitting
	//old check version
	/*
	private void CompileSubroutine() throws IOException {
		writeLine(getAlignedStatement("<subroutineDec>"));
		depth++;
		while(!tok.getTokenString().equals("return"))
		{
			printCurrentTokenAl();
			tok.advance();
		}
		assert(tok.TokenType()==TokenType.KEYWORD):"expecting return keyword";
		assert(tok.keyWord()==Keyword.RETURN):"expecting return keyword";
		printCurrentTokenAl();
		tok.advance();
		while (!tok.getTokenString().equals(";"))
		{
			printAligned(tok.token2XML());
			tok.advance();
		}
		assert(tok.TokenType()==TokenType.SYMBOL):"expecting ';'";
		assert(tok.symbol()==';'):"expecting ';' symbol";
		printCurrentTokenAl();
		tok.advance();
		assert(tok.TokenType()==TokenType.SYMBOL):"expecting '}'";
		assert(tok.symbol()=='}'):"expecting '}' symbol";
		printCurrentTokenAl();
		tok.advance();

		depth--;
		writeLine(getAlignedStatement("</subroutineDec>"));
		// is NOW ON } that closes the subroutine
	}
	 */

	private void CompileSubroutine() throws IOException {
		writeLine(getAlignedStatement("<subroutineDec>"));
		depth++;
		// printing sub type word
		printCurrentTokenAl();
		tok.advance();
		assert (tok.TokenType()==TokenType.IDENTIFIER ||
				tok.TokenType()==TokenType.KEYWORD): "expecting type/void";
		printCurrentTokenAl();
		tok.advance();
		assert (tok.TokenType()==TokenType.IDENTIFIER): "expecting sub name";
		printCurrentTokenAl();
		tok.advance();
		assert (tok.TokenType()==TokenType.SYMBOL): "expecting '(";
		printCurrentTokenAl();

		compileParameterList();

		assert (tok.TokenType()==TokenType.SYMBOL): "expecting ')";
		printCurrentTokenAl();
		tok.advance();
		printAligned("<subroutineBody>");
		depth++;
		assert(tok.TokenType()==TokenType.SYMBOL): "expecting '{'";
		printCurrentTokenAl();
		tok.advance();
		while (tok.TokenType() == TokenType.KEYWORD && tok.keyWord()==Keyword.VAR)
		{
			assert(tok.keyWord()==Keyword.VAR) : "expecting var dec";
			compileVarDec();
		}
		// token is on first statement
		compileStatements();
		assert(tok.TokenType()==TokenType.SYMBOL): "expecting '}'";
		printCurrentTokenAl();

		depth--;
		printAligned("</subroutineBody>");
		depth--;
		writeLine(getAlignedStatement("</subroutineDec>"));

		tok.advance();
		// is NOW ON } that closes the subroutine
	}




	private void compileStatements() throws IOException {
		// we are already on what suppose to be first statement
		// didnt check if this is a keywork
		Keyword statement = takeStatement();
		printAligned("<statements>");
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
		printAligned("</statements>");
	}

	private void compileIF() throws IOException {
		printAligned("<ifStatement>");
		depth++;
		// printing if
		printCurrentTokenAl();
		tok.advance();
		expectingSymbol('(');
		assert ((tok.TokenType() == TokenType.SYMBOL && tok.symbol() != ')')
				|| (tok.TokenType()!= TokenType.SYMBOL)):"expecting expression";
		CompileExpression();
		expectingSymbol(')');
		expectingSymbol('{');
		compileStatements();
		expectingSymbol('}');
		if (tok.TokenType() == TokenType.KEYWORD)
			if (tok.keyWord() == Keyword.ELSE)
			{
				//TODO ASK!!!
				//printAligned("<elseStatement>");

				// TODO ask- printing else
				printCurrentTokenAl();
				tok.advance();
				expectingSymbol('{');
				compileStatements();
				expectingSymbol('}');


				//TODO ASK!!!
				//printAligned("</elseStatement>");
			}



		depth--;
		printAligned("</ifStatement>");

	}

	private void compileReturn() throws IOException {
		printAligned("<returnStatement>");
		depth++;
		// printing return
		printCurrentTokenAl();
		tok.advance();
		if (tok.TokenType() != TokenType.SYMBOL)
			CompileExpression();
		else
			if (tok.symbol() != ';')
				CompileExpression();

		expectingSymbol(';');
		depth--;
		printAligned("</returnStatement>");

	}

	private void compileWhile() throws IOException {
		printAligned("<whileStatement>");
		depth++;
		// printing while
		printCurrentTokenAl();
		tok.advance();
		expectingSymbol('(');
		/*
		if (tok.TokenType() != TokenType.SYMBOL)
			CompileExpression();
		else
			if (tok.symbol() != ')')
				CompileExpression();
		 */
		assert ((tok.TokenType() == TokenType.SYMBOL && tok.symbol() != ')')
				|| (tok.TokenType()!= TokenType.SYMBOL)):"expecting expression";
		CompileExpression();

		expectingSymbol(')');
		expectingSymbol('{');

		compileStatements();

		expectingSymbol('}');
		depth--;
		printAligned("</whileStatement>");

	}

	private void expectingSymbol(char symbol) throws IOException
	{
		assert(tok.TokenType()==TokenType.SYMBOL) : "expecting " + Character.toString(symbol);
		assert(tok.symbol()==symbol):"checking " + Character.toString(symbol);
		printCurrentTokenAl(); // printing symbol
		tok.advance();
	}

	private void compileLet() throws IOException {
		printAligned("<letStatement>");
		depth++;

		//printing Let
		printCurrentTokenAl();
		tok.advance();
		assert(tok.TokenType()==TokenType.IDENTIFIER) : "expecting var name";
		printCurrentTokenAl();
		tok.advance();
		assert(tok.TokenType()==TokenType.SYMBOL) : "expecting '[' or '='";
		printCurrentTokenAl();
		if (tok.symbol()=='=')
		{
			tok.advance();
			CompileExpression();
		}else
		{
			// already checked that it is symbol
			assert(tok.symbol()=='['):"checking '['";
			tok.advance();
			CompileExpression();
			assert(tok.TokenType()==TokenType.SYMBOL) : "expecting ']'";
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

	private void compileDo() throws IOException {
		// tok is on 'Do'
		printAligned("<doStatement>");
		depth++;
		//printing Do
		printCurrentTokenAl();
		tok.advance();
		assert(tok.TokenType()==TokenType.IDENTIFIER) : "expecting a class name / sub name";
		String name = new String(tok.identifier());
		tok.advance();
		subroutineCall(name);
		//assert(tok.TokenType() == TokenType.SYMBOL) : "expecting ')'";
		//printCurrentTokenAl();
		//tok.advance();
		//assert(tok.TokenType() == TokenType.SYMBOL) : "expecting ';'";
		//printCurrentTokenAl();
		//tok.advance();
		expectingSymbol(';');
		depth--;
		printAligned("</doStatement>");

	}

	private void subroutineCall(String name) throws IOException {
		assert(tok.TokenType() == TokenType.SYMBOL) : "expecting ')' or '(' or '.'";
		assert(tok.symbol()=='(' || tok.symbol()==')' || tok.symbol()=='.')
		: "illegal subroutine call expected '(' / ')' / '.', but got: " + tok.getTokenString();
		switch (tok.symbol())
		{
		case '(':
			printAligned("<identifier> " +name+" </identifier>");
			printCurrentTokenAl();
			tok.advance();
			CompileExpressionList();
			break;
		case ')':
			// nothing to do
			break;
		case '.':
			printAligned("<identifier> " +name+" </identifier>");
			printCurrentTokenAl();
			tok.advance();
			assert(tok.TokenType()==TokenType.IDENTIFIER) : "expecting a sub name";
			printCurrentTokenAl();
			tok.advance();
			assert(tok.TokenType() == TokenType.SYMBOL) : "expecting '('";
			printCurrentTokenAl();
			tok.advance();
			CompileExpressionList();
			break;
		}

		expectingSymbol(')');

	}



	private void CompileExpressionList() throws IOException {
		printAligned("<expressionList>");
		depth++;

		if (tok.TokenType() == TokenType.SYMBOL)
		{
			if (tok.symbol()!=')') 
			{
				help_complileList();

			}
		}else
		{
			help_complileList();
		}


		depth--;
		printAligned("</expressionList>");
	}

	private void help_complileList() throws IOException
	{
		CompileExpression();
		assert(tok.TokenType() == TokenType.SYMBOL) :
			"expecting ')' or ',' or'(' , but we got: " + tok.getTokenString();
		while (tok.symbol()==',')
		{
			printCurrentTokenAl();
			tok.advance();
			CompileExpression();
			assert(tok.TokenType() == TokenType.SYMBOL) : "expecting ')' or ','";
		}
	}

	private void CompileExpression() throws IOException {
		printAligned("<expression>");
		depth++;
		CompileTerm();
		// after the last token of term
		if(checkAndPrintOp())
		{
			CompileTerm();
		}


		depth--;
		printAligned("</expression>");

	}

	private boolean checkAndPrintOp() throws IOException {
		if (tok.TokenType()!= TokenType.SYMBOL) return false;
		char c=tok.symbol();
		if (c=='+' || c== '-' || c== '*' || c=='/' || 
				c== '&' || c== '|'  || c== '<' || c== '>' || c== '=' )
		{
			printCurrentTokenAl();
			tok.advance();
			return true;
		}
		return false;
	}

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


	private void CompileTerm() throws IOException {
		printAligned("<term>");
		depth++;
		TokenType t = tok.TokenType();
		switch (t)
		{
		case INT_CONST:
			printCurrentTokenAl();
			tok.advance();
			break;
		case STRING_CONST:
			printCurrentTokenAl();
			tok.advance();
			break;
		case KEYWORD:
			if(checkAndPrintKeywordConstant())
				break;
			assert(false) : "should not get here, tok: "+tok.getTokenString();
			
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
			assert(false) : "should not get here, tok: "+tok.getTokenString();
			break;
		case IDENTIFIER:
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
					assert(sym == '.' || sym == '(' || sym == '[') 
					: "Never get here- expectong '[' / '(' / '.' we got: "+sym;
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
				printAligned("<identifier> " +prevIden + " </identifier>");
			}
		}



		depth--;
		printAligned("</term>");

	}
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

	/* for expresionless
	private void CompileTerm() throws IOException {
		printAligned("<term>");
		depth++;

		assert(tok.TokenType() == TokenType.IDENTIFIER) : 
			"expecting IDENTIFIER, but we got: " + tok.getTokenString();
		printCurrentTokenAl();
		tok.advance();

		depth--;
		printAligned("</term>");

	}
	 */
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

	private void compileVarDec() throws IOException {
		// tok is already on var
		printAligned("<varDec>");
		depth++;
		variablesChain();
		depth--;
		printAligned("</varDec>");

	}

	private void compileParameterList() throws IOException {
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
				// print ,
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

	private void help_ParameterList() throws IOException
	{
		assert (tok.TokenType()==TokenType.KEYWORD
				|| tok.TokenType()==TokenType.IDENTIFIER
				) : "expecting type / class, we got: "+ tok.getTokenString();
		printCurrentTokenAl();
		tok.advance();
		assert (tok.TokenType()==TokenType.IDENTIFIER) : "expecting param name";
		printCurrentTokenAl();
		tok.advance();
		//assert (tok.TokenType()==TokenType.SYMBOL) : "expecting ',' / ')'";
	}

	public void startCompile() throws IOException
	{
		// TODO: dont use getTokenString
		writeLine(getAlignedStatement("<class>"));
		depth++;
		tok.advance();
		assert(tok.getTokenString().equals("class")):"file dont have class";
		if (tok.getTokenString().equals("class"))
			compileClass();

		depth--;
		writeLine(getAlignedStatement("</class>"));

	}

	private String getAlignedStatement(String line)
	{
		String temp = new String();
		for (int i=0; i<depth;i++)
			temp += "  ";
		temp+=line;
		return temp;
	}

	private void printCurrentTokenAl() throws IOException
	{
		String temp = getAlignedStatement(tok.token2XML());
		writeLine(temp);
	}

	private void printAligned(String line) throws IOException
	{
		String temp = getAlignedStatement(line);
		writeLine(temp);
	}

	private boolean isSubroutine()
	{
		Keyword k = tok.keyWord();
		return k==Keyword.METHOD || k==Keyword.CONSTRUCTOR || k==Keyword.FUNCTION;
	}
	
	public void testWithExpr() throws IOException
	{
		tok.advance();
		compileDo();
	}
	/*
	private boolean isVarType()
	{
		Keyword k = tok.keyWord();
		return k==Keyword.INT || k==Keyword.BOOLEAN || k==Keyword.CHAR;
	}
	 */
}
