package embedding.custom.ast;

import org.eclipse.jdt.internal.compiler.ast.Block;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.Initializer;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.flow.FlowContext;
import org.eclipse.jdt.internal.compiler.flow.FlowInfo;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.MethodScope;
import org.eclipse.jdt.internal.compiler.parser.Parser;

@SuppressWarnings("restriction")
public class TypeSpec extends Initializer {
	

	public TypeSpec(Expression expr, int start, int end) {
		super(new Block(0), 0);
		this.initialization = expr;
		this.declarationSourceStart = this.sourceStart = start;
		this.declarationSourceEnd = this.sourceEnd = end;
//		this.bodyStart = this.bodyEnd = end;
	}

	@Override
	public FlowInfo analyseCode(MethodScope methodScope, FlowContext flowContext, FlowInfo flowInfo) {
		return flowInfo;
	}
	
	@Override
	public void generateCode(BlockScope currentScope, CodeStream codeStream) {
		// nop
	}

	@Override
	public void parseStatements(Parser parser, TypeDeclaration typeDeclaration, CompilationUnitDeclaration unit) {
		// nop
	}
	
	@Override
	public void resolve(MethodScope scope) {
		// nop
	}
	
	public int complainIfUnreachable(FlowInfo flowInfo, BlockScope scope, int previousComplaintLevel) {
		//shut up!
		return previousComplaintLevel;
	}
}
