package embedding.jdt;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Statement;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.parser.TerminalTokens;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;

import embedding.custom.ast.CurrencyExpression;
import embedding.custom.ast.CustomParser;
import embedding.custom.ast.StatementSpec;
import embedding.custom.ast.TypeSpec;

import base org.eclipse.jdt.core.dom.ASTConverter;
import base org.eclipse.jdt.core.dom.SimpleName;
import base org.eclipse.jdt.internal.compiler.parser.Parser;
import base org.eclipse.jdt.internal.compiler.parser.Scanner;

/**
 * This team adapts those classes of the JDT that directly manipulate syntax and create AST
 * (two kinds of AST actually). 
 * With this adaptation the JDT handles a custom syntax embedded into Java.
 * Regions of custom syntax are to be delimited by "<%" and "%>". 
 * 
 * @author stephan
 */
@SuppressWarnings("restriction")
public team class SyntaxAdaptor {

	/**
	 * <strong>Part 1 of the adaptation:</strong> 
	 * Wait until '<' is seen and check if it actually is a special string enclosed in '<%' and '%>'.
	 */
	protected class ScannerAdaptor playedBy Scanner {

		// access fields from Scanner ("callout bindings"):
		int getCurrentPosition() 					 -> get int currentPosition;
		void setCurrentPosition(int currentPosition) -> set int currentPosition;
		char[] getSource()       					 -> get char[] source;

		// intercept this method from Scanner ("callin binding"):
		int getNextToken() <- replace int getNextToken();

		callin int getNextToken() throws InvalidInputException {
			// invoke the original method:
			int token = base.getNextToken();
			if (token == TerminalTokens.TokenNameLESS) {
				char[] source = getSource();
				int pos = getCurrentPosition();
				if (source[pos++] == '%') { 									  // detecting the opening "<%" ?
					int start = pos; 											  // inner start, just behind "<%" 
					try {
						while (source[pos++] != '%' || source[pos++] != '>') 	  // detecting the closing "%>" ?
							; 													  // empty body
					} catch (ArrayIndexOutOfBoundsException aioobe) {			  // not found, proceed as normal
						return token;
					}
					setCurrentPosition(pos);									  // tell the scanner what we have consumed (pointing one past '>')
					int end = pos-2; 											  // position of "%>"
					char[] fragment = CharOperation.subarray(source, start, end); // extract the custom string (excluding <% and %>)
					// prepare an inner adaptor to intercept the expected parser action 
					new InnerCompilerAdaptor(fragment, start-2, end+1).activate();	// positions include <% and %>
					return TerminalTokens.TokenNameSEMICOLON;					  // pretend we saw an empty declaration (';') 
				}
			}
			return token;
		}
	}
	/**
	 * <strong>Part 2 of the adaptation:</strong> 
	 * If the ScannerAdaptor found a match intercept creation of the faked null expression
	 * and replace it with a custom AST. 
	 * 
	 * This is a team with a nested role so that we can control activation separately.
	 * 
	 * This team should be activated for the current thread only to ensure that 
	 * concurrent compilations don't interfere: By using thread activation any state of
	 * this team is automatically local to that thread.
	 */
	protected team class InnerCompilerAdaptor {
		
		CustomParser customParser = new CustomParser();
		
		char[] source;
		int start, end;
		
		protected InnerCompilerAdaptor(char[] source, int start, int end) {
			this.source = source;
			this.start = start;
			this.end = end;
		}
		
		/** This inner role does the real work of the InnerCompilerAdaptor. */
		@SuppressWarnings("decapsulation")
		protected class ParserAdaptor playedBy Parser {

			// import methods from Parser ("callout bindings"):
			void pushOnAstStack(ASTNode stmt) -> void pushOnAstStack(ASTNode stmt);
			ProblemReporter getProblemReporter()        -> ProblemReporter problemReporter();

			// peek the top of the ast stack, or null if stack is empty
			ASTNode getCurrentASTNode() -> get ASTNode[] astStack
				with { result <- base.astPtr > -1 ? astStack[base.astPtr] : null }
			
			int getStartPosition() -> get Scanner scanner
				with { result <- scanner.startPosition }


			boolean hasParsedSpec = false;
			
			// intercept this method from Parser ("callin binding"):
			void consumeToken(int type) <- replace void consumeToken(int type);
			
			@SuppressWarnings("basecall")
			callin void consumeToken(int type) {
				if (type == TerminalTokens.TokenNameSEMICOLON) {
					if (start == getStartPosition()) {
						// inspect the adjacent node to see where we are: 
						ASTNode currentNode = getCurrentASTNode();
						if (currentNode instanceof TypeDeclaration || currentNode instanceof FieldDeclaration || currentNode instanceof AbstractMethodDeclaration) {
							pushOnAstStack(new TypeSpec(parseSpec(), start, end));
							return;
						} else if (currentNode instanceof Statement) {
							pushOnAstStack(new StatementSpec(parseSpec(), start, end));
							return;
						}
					}
				}
				// shouldn't happen: only activated when scanner returns TokenNamenull
				base.consumeToken(type);
			}
			
			Expression parseSpec() {
				this.hasParsedSpec = true;
				return customParser.parseCurrencyExpression(source, start, end, this.getProblemReporter());
			}

			consumeSemi <- replace consumeEmptyTypeDeclaration, consumeEmptyStatement
				when (hasParsedSpec);
			
			/** Given we have parsed as spec, don't create AST for the fake ';' */
			@SuppressWarnings({ "basecall", "inferredcallout" })
			callin void consumeSemi() {
				// this inner adaptor has done its job, no longer intercept
				InnerCompilerAdaptor.this.deactivate(); 
				// don't push on astLengthStack (as consumeEmptyTypeDeclaration does)
				flushCommentsDefinedPriorTo(this.endStatementPosition);
			}
			
			void deactivate() <- after void parse(); // fail safe, just in case consumeSemi() didn't get triggered
		}		
	}
	
	/** 
	 * DOM representation of CurrencyExpression.
	 * Since the constructors of all DOM classes are package private we cannot subclass, so we use a role instead.
	 * We use a SimpleName as the base representation because that's the simplest node matching our requirements.
	 */
    protected class DomCurrencyLiteral playedBy SimpleName
	{
	    protected String currency;

		void setSourceRange(int sourceStart, int length) -> void setSourceRange(int sourceStart, int length);
		
		@SuppressWarnings("decapsulation")
	    public DomCurrencyLiteral(AST ast, CurrencyExpression expression){
			base(ast);
			this.currency = expression.currency;
			setSourceRange(expression.sourceStart, expression.sourceEnd-expression.sourceStart+1);
		}
	}
	
	/**
 	 * <h4>Part 3 of the adaptation:</h4> 
	 * This adaptor role helps the {@link org.eclipse.jdt.core.dom.ASTConverter} to convert {@link DomCurrencyLiteral}s, too.
	 */
	@SuppressWarnings("decapsulation")
	protected class DomConverterAdaptor playedBy ASTConverter {

		// whenever convert(Expression) is called ...
		org.eclipse.jdt.core.dom.Expression convertCurrencyExpression(CurrencyExpression expression) 
		<- replace org.eclipse.jdt.core.dom.Expression convert(Expression expression)
			// ... and when the literal is actually a CurrencyExpression ...
			base when (expression instanceof CurrencyExpression)
			// ... perform the cast we just checked for and feed it into the callin method below. 
			with { expression <- (CurrencyExpression)expression }
        
        /** 
         * Convert a CurrencyExpression from the compiler to its DOM counter part.
         * This method uses inferred callouts (OTJLD §3.1(j),3.5(h)) which need to be enabled in the OT/J compiler preferences. 
         */
        @SuppressWarnings({ "basecall", "inferredcallout" })
		callin org.eclipse.jdt.core.dom.Expression convertCurrencyExpression(CurrencyExpression expression){
    		final DomCurrencyLiteral name = new DomCurrencyLiteral(this.ast, expression);
    		if (this.resolveBindings) {
    			recordNodes(name, expression);
    		}
    		return name;
        }
	}
}
