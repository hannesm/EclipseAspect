package embedding.jdt;

import org.eclipse.jdt.internal.compiler.ast.Block;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.MethodScope;
import org.eclipse.jdt.internal.compiler.parser.Scanner;
import org.eclipse.jdt.internal.formatter.Scribe;

import base embedding.custom.ast.CurrencyExpression;
import base org.eclipse.jdt.internal.compiler.ASTVisitor;
import base org.eclipse.jdt.internal.formatter.CodeFormatterVisitor;

/**
 * This team adapts (potentially several) visitors from the JDT for handling {@link CurrencyExpression}s. 
 * @author stephan
 */
@SuppressWarnings("restriction")
public team class VisitorsAdaptor {
	/**
	 * Re-usable part of visitor adaptation. 
	 * Create one sub-class per visitor to be adapted.
	 */
/*
	@SuppressWarnings("abstractrelevantrole")
	protected team class AstVisiting playedBy ASTVisitor {
		// whenever visiting something that could contain an expression
		// activate this team to enable callins of the inner role
		callin void visiting() {
			within(this)
				base.visiting();
		}
		void visiting() <- replace boolean visit(Block block, BlockScope scope), 
								   boolean visit(FieldDeclaration fieldDeclaration, MethodScope scope);
		
		protected class CustomAst playedBy CurrencyExpression {
			// variant of traversal that should be used when the enclosing team is active:
			// (implement in subclasses)
		    callin void process() { };
			void process() <- replace void traverse(ASTVisitor visitor, BlockScope scope);
		}
	}
*/
	/**
	 * Apply the above infrastructure for adapting source code formatting.
	 */
	protected team class AstFormatting /*extends AstVisiting */ playedBy CodeFormatterVisitor {
		callin void visiting() {
			within(this)
				base.visiting();
		}
		void visiting() <- replace boolean visit(Block block, BlockScope scope), 
								   boolean visit(FieldDeclaration fieldDeclaration, MethodScope scope);
		
		// one more trigger that should activate the team:
		@SuppressWarnings("decapsulation")
		visiting <- replace formatStatements;
		
		Scribe getScribe() -> get Scribe scribe;

		/** This role implements formatting of our custom ast: */
		//@Override
		protected class CustomAst playedBy CurrencyExpression {
			@SuppressWarnings({ "inferredcallout", "basecall" })
			callin void process() {
				Scribe scribe = getScribe();
				Scanner scanner = scribe.scanner;
				
				// format this AST node into a StringBuffer:
				StringBuffer replacement = new StringBuffer();
				replacement.append("<% ");
				this.value.printExpression(0, replacement);
				replacement.append(' ');
				replacement.append(this.currency);
				replacement.append(" %>");
				
				// feed the formatted string into the Scribe:
				int start = this.sourceStart();
				int end = this.sourceEnd();
				scribe.addReplaceEdit(start, end, replacement.toString());
				
				// advance the scanner:
				scanner.resetTo(end+1, scribe.scannerEndPosition - 1);
				scribe.pendingSpace = false;
			}
		}
	}
}
