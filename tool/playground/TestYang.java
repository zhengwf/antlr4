import org.antlr.runtime.debug.BlankDebugEventListener;
import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.atn.LexerInterpreter;
import org.antlr.v4.runtime.atn.ParserInterpreter;

import java.io.File;

/** Parse a java file or directory of java files using the generated parser
 *  ANTLR builds from java.g
 */
class TestYang {
	public static long lexerTime = 0;
	public static boolean profile = false;
	public static YangJavaLexer lexer;
	public static YangJavaParser parser = null;

	public static void main(String[] args) {
		try {
			long start = System.currentTimeMillis();
			if (args.length > 0 ) {
				// for each directory/file specified on the command line
				for(int i=0; i< args.length;i++) {
					doFile(new File(args[i])); // parse it
				}
			}
			else {
				System.err.println("Usage: java Main <directory or file name>");
			}
			long stop = System.currentTimeMillis();
			System.out.println("Lexer total time " + lexerTime + "ms.");
			System.out.println("Total time " + (stop - start) + "ms.");

			System.out.println("finished parsing OK");
			System.out.println(LexerInterpreter.ATN_failover+" lexer failovers");
			System.out.println(LexerInterpreter.match_calls+" lexer match calls");
			System.out.println(ParserInterpreter.ATN_failover+" parser failovers");
			System.out.println(ParserInterpreter.predict_calls +" parser predict calls");
			if ( profile ) {
				System.out.println("num decisions "+profiler.numDecisions);
			}
		}
		catch(Exception e) {
			System.err.println("exception: "+e);
			e.printStackTrace(System.err);   // so we can get stack trace
		}
	}


	// This method decides what action to take based on the type of
	//   file we are looking at
	public static void doFile(File f)
							  throws Exception {
		// If this is a directory, walk each file/dir in that directory
		if (f.isDirectory()) {
			String files[] = f.list();
			for(int i=0; i < files.length; i++)
				doFile(new File(f, files[i]));
		}

		// otherwise, if this is a java file, parse it!
		else if ( ((f.getName().length()>5) &&
				f.getName().substring(f.getName().length()-5).equals(".java"))
			|| f.getName().equals("input") )
		{
		    System.err.println("parsing "+f.getAbsolutePath());
			parseFile(f.getAbsolutePath());
		}
	}

	static class CountDecisions extends BlankDebugEventListener {
		public int numDecisions = 0;
		public void enterDecision(int decisionNumber) {
			numDecisions++;
		}
	}
	static CountDecisions profiler = new CountDecisions();

	// Here's where we do the real work...
	public static void parseFile(String f)
								 throws Exception {
		try {
			// Create a scanner that reads from the input stream passed to us
			if ( lexer==null ) {
				lexer = new YangJavaLexer(null);
			}
			lexer.setCharStream(new ANTLRFileStream(f));

			CommonTokenStream tokens = new CommonTokenStream(lexer);
			long start = System.currentTimeMillis();
			tokens.fill();
//			System.out.println(tokens.getTokens());
			long stop = System.currentTimeMillis();
			lexerTime += stop-start;
			for (Object t : tokens.getTokens()) {
				System.out.println(t);
			}

			if ( true ) {
				// Create a parser that reads from the scanner
				if ( parser==null ) {
					parser = new YangJavaParser(tokens);
				}
				parser.setTokenStream(tokens);
				// start parsing at the compilationUnit rule
				parser.compilationUnit(null);
				//System.err.println("finished "+f);
			}
		}
		catch (Exception e) {
			System.err.println("parser exception: "+e);
			e.printStackTrace();   // so we can get stack trace
		}
	}

}

