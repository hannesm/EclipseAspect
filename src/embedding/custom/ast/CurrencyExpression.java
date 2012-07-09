package embedding.custom.ast;

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.ast.IntLiteral;
import org.eclipse.jdt.internal.compiler.ast.SingleNameReference;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.flow.FlowContext;
import org.eclipse.jdt.internal.compiler.flow.FlowInfo;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.LocalVariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

import embedding.jdt.SyntaxAdaptor;

/**
 * Custom AST node for representing a money value: consisting of an
 * int value and a currency name.
 *
 * Note that we are sub-classing SingleNameReference to establish consistency
 * with the DOM-representation given by {@link SyntaxAdaptor.DomCurrencyLiteral}.
 * @author stephan
 */
@SuppressWarnings("restriction")
public class CurrencyExpression extends SingleNameReference {

	public IntLiteral value;
	public String currency;
	
	final static String[] CURRENCIES = { "euro", "dollar" };
	private static final char[] EXPR_NAME = "<currency expression>".toCharArray();
	
	/**
	 * Create an empty CurrencyExpression
	 * @param sourceStart start of the expression including the opening "<%"
	 * @param sourceEnd end of the expression including the closing "%>"
	 */
	public CurrencyExpression(int sourceStart, int sourceEnd) {
		super(EXPR_NAME, (((long)sourceStart)<<32)+sourceEnd);
	}
	
	/** 
	 * Try to initialize the currency field from a given string.
	 * @param string the scanned but yet uninterpreted token
	 * @return true iff 'string' actually is a legal currency name.
	 */
	public boolean setCurrency(String string) {
		for (String curr : CURRENCIES)
			if (curr.equals(string)) {
				this.currency = curr;
				return true;
			}
		return false;
	}

	@Override
	public StringBuffer printExpression(int indent, StringBuffer output) {
		this.value.printExpression(indent, output);
		output.append(' ');
		output.append(this.currency);
		return output;
	}
	
	@Override
	public TypeBinding resolveType(BlockScope scope) {
		// delegate to the value:
		this.resolvedType = this.value.resolveType(scope);
		// pretend this is a reference to a local variable:
		this.constant = Constant.NotAConstant;
		this.binding = new LocalVariableBinding(this.token, this.resolvedType, 0, false);
		this.bits &= ~(Binding.FIELD|Binding.TYPE);
		return this.resolvedType;
	}

	@Override
	public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo, boolean valueRequired) {
		// delegate to the value:
		return this.value.analyseCode(currentScope, flowContext, flowInfo, valueRequired);
	}
	
	@Override
	public void generateCode(BlockScope currentScope, CodeStream codeStream, boolean valueRequired) {
		// delegate to the value:
		this.value.generateCode(currentScope, codeStream, valueRequired);
	}
	
	@Override
	public void traverse(ASTVisitor visitor, BlockScope scope) {
		// delegate to the value:
		this.value.traverse(visitor, scope);
	}

}
