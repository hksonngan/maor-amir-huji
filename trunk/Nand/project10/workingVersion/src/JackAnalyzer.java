import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;


public class JackAnalyzer {
	
	private static final String XML_SUFFIX = ".xml";
	private static final String JACK_SUFFIX = ".jack";
	private static final String TEST_FILENAME = "T.xml";
	
	
	public static void main(String[] args) {

		File source;
		//String outputFileName;
		//JackTokenizer tokenizer;

		//checking that we have the right number of arguments

		if (args.length != 1){
			System.out.println("Usage: VMtranslator <source>");
			return;
		}

		try{
			//opening source file
			source = new File(args[0]);
			if (!source.exists()){
				System.out.println("ERROR: source file doesn't exists. Exiting...");
				
				return;
			}

			//creating outputFile name.
			/*
			if (source.isDirectory())	
				//outputFileName = new String(args[0]+"/"+source.getName()+ASM_SUFFIX);
				outputFileName = args[0]+"/"+source.getName()+XML_SUFFIX;
			else outputFileName = new String(args[0].substring(0,args[0].length()-3) + XML_SUFFIX);
			*/


			//creating CodeWriter object
			//codeWriter = new CodeWriter(new BufferedWriter(new FileWriter(outputFileName)));

			//translating given file/files(in case of directory)
			boolean isTestTokenizer = false;
			if (source.isDirectory())
				translateDirectory(source,isTestTokenizer);
			else translateFile(source,isTestTokenizer);
			
			//TODO: remove DONE print
			System.out.println("Done!");

		}

		catch (Exception e){
			System.out.println("Error reading/writing to files. Exiting...");	
		}

		return;

	}

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
			comp.simpleOutput2();
		else
		{
			comp.startCompile();
			//TODO : only start
			//comp.testWithExpr();
		}
		r.close();
		writer.close();
		
		
	}


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
