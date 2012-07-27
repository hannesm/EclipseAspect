package embedding.custom.ast;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.IntLiteral;
import org.eclipse.jdt.internal.compiler.parser.TerminalTokens;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;

/**
 * Parse a source region into a CurrencyExpression.
 * Report syntax errors if source doesn't match our custom syntax.
 * @author stephan
 */
@SuppressWarnings("restriction")
public class CustomParser {
	
	/**
	 * Parse source into a CurrencyExpression.
	 * @param source       the fragment of source that was detected by the scanner
	 * @param outerStart   points at the opening "<%" 
	 * @param outerEnd     points at the end of the closing "%>"
	 * @param problemReporter use this for reporting syntax errors
	 * @return the parsed expression, may be filled with dummy values but never is null.
	 */
	public Expression parseCurrencyExpression(char[] source, 
											  int outerStart, 
											  int outerEnd, 
											  ProblemReporter problemReporter) 
	{
		// position inside "<%" and "%>" delimiters:
		int innerStart = outerStart + 2; 
		int innerEnd   = outerEnd   - 2;
		
		CurrencyExpression expr = new CurrencyExpression(outerStart, outerEnd);
		
		char[][] parts = new char[2][];
		if (!splitAndTrim(source, parts)) {
			// invalid number of tokens:
			problemReporter.parseErrorInvalidToken(innerStart, 
												   innerEnd, 
												   TerminalTokens.TokenNameIdentifier, 
												   source, 
												   "unparsed token", 
												   "'value currency'");
			expr.value = IntLiteral.buildIntLiteral("0".toCharArray(), innerStart, innerEnd);
			expr.currency = "NO CURRENCY";
			return expr;
		}
		
		// set components:
		expr.value = IntLiteral.buildIntLiteral(parts[0], innerStart, innerStart+parts[0].length-1);
		boolean validCurrency = expr.setCurrency(new String(parts[1]));
		
		if (!validCurrency) {
			int cStart = CharOperation.indexOf(parts[1], source, true);
			problemReporter.parseErrorInvalidToken(innerStart+cStart, 
												   innerStart+cStart+parts[1].length-1, 
												   TerminalTokens.TokenNameIdentifier, 
												   parts[1], 
												   "unknown currency token", 
												   "currency");
			expr.currency = "NO CURRENCY";
		}
		return expr;
	}
	/* Split source at white spaces and fill tokens into 'result'. 
	 * Report false if number of tokens doesn't match the length of 'result'. */
	boolean splitAndTrim(char[] source, char[][] result) {
		int len = source.length;
		int s = 0;
		for (int i = 0; i < result.length; i++) {
			while (s<len && CharOperation.isWhitespace(source[s])) s++;
			int e = s;
			while (e<len && !CharOperation.isWhitespace(source[e])) e++;
			if (e <= s)
				return false;
			result[i] = CharOperation.subarray(source, s, e);
			s = e+1;
		}
		return true;
	}
}
