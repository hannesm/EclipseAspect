package embedding.custom.ast;

import org.eclipse.jdt.internal.compiler.ast.AssertStatement;
import org.eclipse.jdt.internal.compiler.ast.Expression;

public class MyStatement extends AssertStatement {

	public MyStatement(Expression exceptionArgument,
			Expression assertExpression, int startPosition) {
		super(exceptionArgument, assertExpression, startPosition);
		// TODO Auto-generated constructor stub
		System.out.println("instantiated MyStatement");
	}

}