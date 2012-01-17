import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StreamTokenizer;
import java.util.HashMap;

public class JackTokenizer {

	//private variables
	
	StreamTokenizer tokenizer;
	File file;
	FileReader reader;
	
	boolean classdot;
	boolean dotSymbol;
	String classdot_Str;
	String nextCommand;
	
	private String currentToken;
	private TokenType curType;
	HashMap<String, Keyword> keywords;
	HashMap<Character, String> symbols;
	

	
	/*
	 * initiating stream tokenizer with the correct syntax
	 * init members in key words
	 */
	private void init(){
		// already checked that this is a valid file and reader
		tokenizer = new StreamTokenizer(reader);
		tokenizer.slashSlashComments(true);
		tokenizer.slashStarComments(true);
		tokenizer.wordChars('_', '_');
		tokenizer.ordinaryChar('.');
		tokenizer.ordinaryChar('/');
		tokenizer.ordinaryChar('-');
		tokenizer.ordinaryChar('\'');
		tokenizer.quoteChar('"');
		fillKeywordsAndSymbols();

	}

	/**
	 * fills keywords Map.
	 */
	private void fillKeywordsAndSymbols() {
		keywords = new HashMap<String, Keyword>();
		keywords.put(new String("class"), Keyword.CLASS);
		keywords.put(new String("constructor"), Keyword.CONSTRUCTOR);
		keywords.put(new String("function"), Keyword.FUNCTION);
		keywords.put(new String("method"), Keyword.METHOD);
		keywords.put(new String("int"), Keyword.INT);
		keywords.put(new String("boolean"), Keyword.BOOLEAN);
		keywords.put(new String("char"), Keyword.CHAR);
		keywords.put(new String("void"), Keyword.VOID);
		keywords.put(new String("var"), Keyword.VAR);
		keywords.put(new String("static"), Keyword.STATIC);
		keywords.put(new String("field"), Keyword.FIELD);
		keywords.put(new String("let"), Keyword.LET);
		keywords.put(new String("do"), Keyword.DO);
		keywords.put(new String("if"), Keyword.IF);
		keywords.put(new String("else"), Keyword.ELSE);
		keywords.put(new String("while"), Keyword.WHILE);
		keywords.put(new String("return"), Keyword.RETURN);
		keywords.put(new String("true"), Keyword.TRUE);
		keywords.put(new String("false"), Keyword.FALSE);
		keywords.put(new String("null"), Keyword.NULL);
		keywords.put(new String("this"), Keyword.THIS);
		
		
		symbols = new HashMap<Character, String>();
		symbols.put(new Character('{'), new String("{"));
		symbols.put('}', new String("}"));
		symbols.put('(', new String("("));
		symbols.put(')', new String(")"));
		symbols.put('[', new String("["));
		symbols.put(']', new String("]"));
		symbols.put('.', new String("."));
		symbols.put(',', new String(","));
		symbols.put(';', new String(";"));
		symbols.put('+', new String("+"));
		symbols.put('-', new String("-"));
		symbols.put('*', new String("*"));
		symbols.put('/', new String("/"));
		symbols.put('&', new String("&amp;"));
		symbols.put('|', new String("|"));
		symbols.put('<', new String("&lt;"));
		symbols.put('>', new String("&gt;"));
		symbols.put('=', new String("="));
		symbols.put('~', new String("~"));
	}


	/*
	 * Construcor.
	 * inputFile - a scanner object represents the
	 * opened input file.
	 */
	public JackTokenizer(File inputFile, FileReader freader){
		file = inputFile;
		reader = freader;
		init();

	}

	/*
	 * Returns true if there are move commands in the file
	 */
	public boolean hasMoreCommands(){

		return (tokenizer.ttype != StreamTokenizer.TT_EOF);
	}

	/*
	 * Reads the next command from the input and 
	 * makes it the current command. Should be called
	 * only if hasMoreCommands() is true. Initially
	 * there is no current command.
	 */
	public void advance() throws IOException{
		if (tokenizer.ttype!=StreamTokenizer.TT_EOF)
		{
			tokenizer.nextToken();
			int type = tokenizer.ttype;
			boolean isCase = false;
			switch(type)
			{
				case StreamTokenizer.TT_NUMBER:
					curType = TokenType.INT_CONST;
					currentToken = Integer.toString(((int)tokenizer.nval));
					isCase=true;
					break;
				case StreamTokenizer.TT_WORD:
					currentToken = tokenizer.sval;
					curType = (keywords.containsKey(currentToken)) ? 
							TokenType.KEYWORD : TokenType.IDENTIFIER;
					isCase=true;
					break;
				case '"':
					currentToken = tokenizer.sval;
					currentTokenSpecialCase();
					curType = TokenType.STRING_CONST;
					isCase=true;
					break;
			}
			if (!isCase)
			{
				if (symbols.containsKey((char)tokenizer.ttype))
				{
					currentToken = symbols.get((char)tokenizer.ttype);
					curType = TokenType.SYMBOL;
				}
				else{
					advance();
				}
			}
		}
	}

/**
 * returns the TokenType of currentToken
 * @return the TokenType of currentToken
 */
	public TokenType TokenType(){

		return curType;

	}
	
	/**
	 * prints current token in a readable string format
	 * @return currentToken in a string.
	 */
	public String getTokenString()
	{
		TokenType t = TokenType();
		String toPrint = new String(currentToken);
		// get integer value to string
		if (t==TokenType.INT_CONST)
			toPrint = Integer.toString((int)tokenizer.nval);
		return toPrint;
	}
	
	/**
	 * Prints current token's xml tag as a function
	 * of currentToken's type
	 * @return a string containing conversion of current token to xml format.
	 */
	public String token2XML()
	{
		TokenType t = TokenType();
		String toPrint = currentToken;
		if (t==TokenType.STRING_CONST)
			// fixing string in specail cases
			toPrint = stringVal();
		
		// get integer value to string
		if (t==TokenType.INT_CONST)
			toPrint = Integer.toString((char)tokenizer.nval);
		return XMLtokenType(t, true) +" "+toPrint +" "+ 
				XMLtokenType(t, false);
	}

	/**
	 * adds xml tag to type
	 * @param t then type of token
	 * @param open open or close xml tag
	 * @return string of the correct xml tag.
	 */
	private static String XMLtokenType(TokenType t , boolean open)
	{
		switch (t){
		case INT_CONST:
			return open ? "<integerConstant>" : "</integerConstant>";
		case IDENTIFIER:
			return open ? "<identifier>" : "</identifier>";
		case KEYWORD:
			return open ? "<keyword>" : "</keyword>";
		case STRING_CONST:
			return open ? "<stringConstant>" : "</stringConstant>";
		case SYMBOL:
			return open ? "<symbol>" : "</symbol>";
			
		}
		
		return "programer's note: never get in here";
	}
	
	/* the following functions implement the given tokenizer interface*/

	public Keyword keyWord()
	{
		if (TokenType()!=TokenType.KEYWORD) return Keyword.INVALID;
		return keywords.get(currentToken);
		
	}
	
	public char symbol()
	{
		if (TokenType()!=TokenType.SYMBOL) return '?';
		return currentToken.charAt(0);
	}
	
	public String identifier()
	{
		return currentToken;
	}
	
	public int intVal()
	{
		return ((int)tokenizer.nval);
	}

	public String stringVal()
	{
		currentToken = currentToken.replace("&", symbols.get('&'));
		currentToken = currentToken.replace("<", symbols.get('<'));
		currentToken = currentToken.replace(">", symbols.get('>'));
		
		return currentToken;
	}
	
	/**
	 *  taking care of special cases in Java string that may
	 *  crash our program
	 */
	private void currentTokenSpecialCase()
	{

		
		currentToken = currentToken.replace("\n","\\n");
		currentToken = currentToken.replace("\t","\\t");
		currentToken = currentToken.replace("\b","\\b");
		currentToken = currentToken.replace("\r","\\r");
        
        char[]  b = {0xB};
        currentToken = currentToken.replace(new String(b),"\\v");
       
        char[] c = {0xC} ;
        currentToken = currentToken.replace(new String(c),"\\f");
       
        char[] a = {0x7};
        currentToken = currentToken.replace(new String(a),"\\a");
        
	}

}


