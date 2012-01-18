import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

// created by Maor SHCHUNA!!
public class JackCompiler {
	
	private static final String VM_SUFFIX = ".vm";
	private static final String JACK_SUFFIX = ".jack";
	
	
	
	public static void main(String[] args) {
		File source;

		//checking that we have the right number of arguments

		if (args.length != 1){
			System.out.println("Usage: JackCompiler <source>");
			return;
		}

		try{
			//opening source file
			source = new File(args[0]);
			if (!source.exists()){
				System.out.println("ERROR: source file doesn't exists. Exiting...");
				
				return;
			}

			
			if (source.isDirectory())
				translateDirectory(source);
			else translateFile(source);
			
			//System.out.println("Done!");

		}

		catch (Exception e){
			System.out.println("Error reading/writing to files. Exiting...");	
		}

		return;

	}
	
	
/**
 * Compiling a single jack file.
 * @param current current file to compile.
 * @param isTestTokenizer false for XML file. true for xxxT.xml test file
 * @throws IOException
 */
	private static void translateFile(File current)
			throws IOException {
		String output = current.getAbsolutePath();
		if (!(output.substring(output.length()-5,output.length())
				.equals(JACK_SUFFIX))) return;
		FileReader r = new FileReader(current);

		JackTokenizer t = new JackTokenizer(current, r);
		
		output = output.substring(0,output.length()-5) + VM_SUFFIX ; 

		BufferedWriter writer = 
				new BufferedWriter (new FileWriter(new File(output)));

		CompilationEngine comp = new CompilationEngine(t, writer);
		comp.startCompile();

		r.close();		
	}

	/**
	 * iterates over files in this folder and in its subfolders,
	 * recursively.
	 * @param directory the main directory
	 * @param isTestTokenizer false for XML file. true for xxxT.xml test file
	 * @throws IOException
	 */
	private static void translateDirectory(File directory)
			throws IOException {

		File[] filesInDirectory = directory.listFiles();
		for (File file : filesInDirectory)
		{
			if (file.isDirectory())
				translateDirectory(file);
			else translateFile(file);
		}
	}
	
	

}
