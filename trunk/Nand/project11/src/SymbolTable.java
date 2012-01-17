import java.util.HashMap;
import java.util.Iterator;

/**
 * Provides a symbol table abstraction.Associates the identifier names found 
 * in the program with identifier properties needed for compilation: type,kind,running index.
 * The symbol table for Jack programs has two nested scopes(class/subroutine) 
 */
public class SymbolTable {
	
	
	private HashMap<String,Symbol> _classHashMap;
	private HashMap<String,Symbol> _subroutineHashMap;

	/*
	 * Creates a new empty symbol
	 */
	public SymbolTable() {
		_classHashMap = new HashMap<String, SymbolTable.Symbol>();
		_subroutineHashMap = new HashMap<String, SymbolTable.Symbol>();
	}

	/*
	 * Starts a new subroutine scope.(resets the subroutine's symbol table
	 */
	public void startSubroutine(){
		_subroutineHashMap.clear();
		return;
	}

	/*
	 * Defines a new identifier of a given name,type,kind and assign it a running index.
	 * STATIC and FIELD identifiers have a subroutine scope where ARG and VAR has subroutine scope.
	 */
	public void define(String name, String type , Kind kind) {
		int currentNumber = varCount(kind);
		Symbol symbol = new Symbol(type,kind,currentNumber);
		if (kind == Kind.FIELD || kind == Kind.STATIC){
			_classHashMap.put(new String(name),symbol);
		}
		else _subroutineHashMap.put(new String(name),symbol);
		return;
	}
	
	/*
	 * Returns the number of variables of the given kind already defined in the
	 * current scope.
	 */
	public int varCount(Kind kind){
		
		Iterator<Symbol> iter;
		Symbol currentSymbol;
		int counter = 0;
		if (kind == Kind.FIELD || kind == Kind.STATIC){
			iter = _classHashMap.values().iterator();
		}
		else iter = _subroutineHashMap.values().iterator();
		
		while (iter.hasNext()){
			currentSymbol = iter.next();
			if (currentSymbol.get_kind() == kind)
				counter++;
		}
		return counter;
	}
	
	/*
	 * Returns the kind of the named identifier in the current scope.
	 * It the identifier is unknown in the current scope, returns null.
	 */
	public Kind kindOf(String name){
		if (_subroutineHashMap.containsKey(name)){
			return _subroutineHashMap.get(name).get_kind();
		}
		else if (_classHashMap.containsKey(name))
				return _classHashMap.get(name).get_kind();
		else return null;
	}
	
	/*
	 * Returns the type of the named identifier in the current scope.
	 * It the identifier is unknown in the current scope, returns null.
	 */
	public String typeOf(String name){
		if (_subroutineHashMap.containsKey(name)){
			return _subroutineHashMap.get(name).get_type();
		}
		else if (_classHashMap.containsKey(name))
				return _classHashMap.get(name).get_type();
		else return null;
	} 
	
	
	/*
	 * Returns the index assigned to the named identifier.
	 * It the identifier is unknown in the current scope, returns -1.
	 */
	public int indexOf(String name){
		if (_subroutineHashMap.containsKey(name)){
			return _subroutineHashMap.get(name).get_runningIndex();
		}
		else if (_classHashMap.containsKey(name))
				return _classHashMap.get(name).get_runningIndex();
		else return -1;
	}
		

	
	/**
	 * Implements a symbol in the symbol table.
	 * Saves the type,kind and running Index of the symbol.
	 * The map will be from symbolName(Key) to Symbol object (Value)
	 */
	private static class Symbol{
		
		private String _type;
		private Kind _kind;
		private int _runningIndex;
		
		
		public Symbol(String type,Kind kind , int runningIndex) {
			_type = type;
			_kind = kind;
			_runningIndex = runningIndex;
		}


		public String get_type() {
			return _type;
		}


		public Kind get_kind() {
			return _kind;
		}


		public int get_runningIndex() {
			return _runningIndex;
		}		
		
	}
	
	
	
}
