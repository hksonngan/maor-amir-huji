import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;


public class JackAnalyzer {
	
	private static final String XML_SUFFIX = ".xml";
	private static final String JACK_SUFFIX = ".jack";
	private static final String TEST_FILENAME = "T.xml";
	
	
	// true when we want Txml
	// We want XMLs it is FALSE.
	private static boolean isTestTokenizer = false;
	
	public static void main(String[] args) {
		File source;

		//checking that we have the right number of arguments

		if (args.length != 1){
			System.out.println("Usage: JackAnalyzer <source>");
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
				translateDirectory(source,isTestTokenizer);
			else translateFile(source,isTestTokenizer);
			
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
	private static void translateFile(File current, boolean isTestTokenizer)
			throws IOException {
		String output = current.getAbsolutePath();
		if (!(output.substring(output.length()-5,output.length())
				.equals(JACK_SUFFIX))) return;
		FileReader r = new FileReader(current);

		JackTokenizer t = new JackTokenizer(current, r);
		
		output = isTestTokenizer ?
				output.substring(0,output.length()-5) +TEST_FILENAME: 
				//+XML_SUFFIX:
				output.substring(0,output.length()-5) + XML_SUFFIX;
		BufferedWriter writer = 
				new BufferedWriter (new FileWriter(new File(output)));

		CompilationEngine comp = new CompilationEngine(t, writer);
		if (isTestTokenizer)
			comp.simpleOutput();
		else
		{
			comp.startCompile();

		}
		r.close();
		writer.close();
		
		
	}

	/**
	 * iterates over files in this folder and in its subfolders,
	 * recursively.
	 * @param directory the main directory
	 * @param isTestTokenizer false for XML file. true for xxxT.xml test file
	 * @throws IOException
	 */
	private static void translateDirectory(File directory, boolean isTestTokenizer)
			throws IOException {

		File[] filesInDirectory = directory.listFiles();
		for (File file : filesInDirectory)
		{
			if (file.isDirectory())
				translateDirectory(file,isTestTokenizer);
			else translateFile(file,isTestTokenizer);
		}
	}
	
	

}
