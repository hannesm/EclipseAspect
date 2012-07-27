package embedding.custom.ast;

import org.eclipse.jdt.internal.compiler.ast.EmptyStatement;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.flow.FlowInfo;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;

public class StatementSpec extends EmptyStatement {

	
	private Expression expression;

	public StatementSpec(Expression e, int startPosition, int endPosition) {
		super(startPosition, endPosition);
		this.expression = e;
	}

	public int complainIfUnreachable(FlowInfo flowInfo, BlockScope scope, int previousComplaintLevel) {
		//shut up!
		return previousComplaintLevel;
	}

}
