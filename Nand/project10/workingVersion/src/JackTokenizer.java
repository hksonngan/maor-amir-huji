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
	private boolean isSymbol;
	HashMap<String, Keyword> keywords;
	//private boolean devideSym;
	
	/*
	 * initializing commandMap with a pair of command and 
	 * corresponding TokenType.
	 */
	private void init(){
		// already checked that this is a valid file and reader
		tokenizer = new StreamTokenizer(reader);
		tokenizer.slashSlashComments(true);
		tokenizer.slashStarComments(true);
		tokenizer.wordChars('_', '_');
		tokenizer.wordChars('.', '.');
		tokenizer.ordinaryChar('/');
		tokenizer.ordinaryChar('-');
		fillKeywords();
		//tokenChoose = new TokenChooser();
	}

	private void fillKeywords() {
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
		
	}


	/*
	 * Construcor.
	 * inputFile - a scanner object represents the
	 * opened input file.
	 */
	public JackTokenizer(File inputFile, FileReader freader){
		file = inputFile;
		reader = freader;
		classdot = false;
		dotSymbol = false;
		classdot_Str=null;
		isSymbol = false;
		init();
		//advanceToNextCommand();
	}

	/*
	 * Returns true if there are move commands in the file
	 */
	public boolean hasMoreCommands(){
		/*
		if (nextCommand != null)
			return true;
		else return false;
		*/
		return (tokenizer.ttype != StreamTokenizer.TT_EOF);
	}

	/*
	 * Reads the next command from the input and 
	 * makes it the current command. Should be called
	 * only if hasMoreCommands() is true. Initially
	 * there is no current command.
	 */
	public void advance() throws IOException{
		if (dotSymbol)
		{
			currentToken = ".";
			classdot=true;
			dotSymbol = false;
			isSymbol = true;
			return;
		}
			
		else if (classdot)
		{
			currentToken = classdot_Str;
			
			isSymbol = false;
			classdot= false;
			return;
		}
		if (tokenizer.ttype!=StreamTokenizer.TT_EOF)
		{
			tokenizer.nextToken();
			currentToken = tokenizer.toString();
			if (currentToken.charAt(6)=="'".charAt(0))
			{
				currentToken = Character.toString(currentToken.charAt(7));
				isSymbol = true;
			}
			else
			{
				currentToken= currentToken.substring(6, 
						currentToken.lastIndexOf(']',currentToken.length()-1));
				isSymbol = false;
				if ((currentToken.contains(".")) &&
						tokenizer.ttype== StreamTokenizer.TT_WORD)
				{
					String temp = currentToken.substring(0, 
							currentToken.indexOf('.'));
					classdot_Str = currentToken.substring
							(currentToken.indexOf('.')+1,
							currentToken.length());
					currentToken = temp;
					classdot = false;
					isSymbol=false;
					dotSymbol = true;
				}
			}
				
			
			/*
			else
			{
				currentToken.ind
			}*/
			
			
			/*
			char[] tokenstr = tokenizer.toString().toCharArray();
			String a = "'";
			if(tokenizer.ttype == StreamTokenizer.TT_WORD)
				currentToken = tokenizer.sval;
			else if (tokenizer.ttype == StreamTokenizer.TT_NUMBER)
			{
				currentToken = Double.toString(tokenizer.nval);
			}
			else if(tokenizer.ttype == StreamTokenizer.TT_EOF)
			{
				currentCommand = null;
			}
			else if(tokenstr[6]==a.charAt(0))
			{
				currentToken = Character.toString(tokenstr[7]);
			}
			else
				currentToken = "!UNKNOWN TOKEN! " + tokenizer.toString()+"!!!";
				*/
			
			
			
		}
	}

	/*
	 * Returns the type of the current VM command.
	 * C_ARITHMETIC is returned for all the 
	 * arithmetic commands.
	 */
	public TokenType TokenType(){
		if (isSymbol) {
			return TokenType.SYMBOL;
		} else if (tokenizer.ttype==StreamTokenizer.TT_WORD)
		{
			if (keywords.containsKey(currentToken))
			{
				return TokenType.KEYWORD;
			}else{
				return TokenType.IDENTIFIER;
			}
		}else if(tokenizer.ttype==StreamTokenizer.TT_NUMBER)
			return TokenType.INT_CONST;
		else
			return TokenType.STRING_CONST;

	}
	
	public String getTokenString()
	{
		TokenType t = TokenType();
		String toPrint = new String(currentToken);
		// get integer value to string
		if (t==TokenType.INT_CONST)
			toPrint = Integer.toString((int)tokenizer.nval);
		return toPrint;
	}
	
	public String token2XML()
	{
		TokenType t = TokenType();
		String toPrint = currentToken;
		// get integer value to string
		if (t==TokenType.INT_CONST)
			toPrint = Integer.toString((int)tokenizer.nval);
		else if(t==TokenType.SYMBOL)
		{
			char d = currentToken.charAt(0);
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
		return XMLtokenType(t, true) +" "+toPrint +" "+ 
				XMLtokenType(t, false);
	}

	public static String XMLtokenType(TokenType t , boolean open)
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
		return currentToken;
	}

}


