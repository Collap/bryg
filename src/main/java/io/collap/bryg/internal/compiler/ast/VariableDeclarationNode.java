package io.collap.bryg.internal.compiler.ast;

import io.collap.bryg.internal.compiler.ast.expression.Expression;
import io.collap.bryg.internal.compiler.ast.expression.VariableExpression;
import io.collap.bryg.internal.compiler.CompilationContext;
import io.collap.bryg.internal.LocalVariable;
import io.collap.bryg.internal.Type;
import io.collap.bryg.internal.type.TypeInterpreter;
import io.collap.bryg.internal.compiler.util.CoercionUtil;
import io.collap.bryg.internal.compiler.util.IdUtil;
import io.collap.bryg.BrygJitException;
import io.collap.bryg.parser.BrygLexer;
import io.collap.bryg.parser.BrygParser;

public class VariableDeclarationNode extends Node {

    private LocalVariable variable;
    private Expression expression;

    public VariableDeclarationNode (CompilationContext compilationContext, BrygParser.VariableDeclarationContext ctx) {
        super (compilationContext);
        setLine (ctx.getStart ().getLine ());

        String name = IdUtil.idToString (ctx.id ());
        Type expectedType = null;
        if (ctx.type () != null) {
            expectedType = new TypeInterpreter (compilationContext.getEnvironment ().getClassResolver ()).interpretType (ctx.type ());
        }

        expression = null;
        if (ctx.expression () != null) {
            expression = (Expression) compilationContext.getParseTreeVisitor ().visit (ctx.expression ());
        }

        Type type;
        if (expectedType == null) {
            if (expression == null) {
                throw new BrygJitException ("Could not infer type for variable '" + name + "'.", getLine ());
            }else{
                type = expression.getType ();
            }
        }else {
            if (expression == null) {
                type = expectedType;
            }else {
                if (!expression.getType ().similarTo (expectedType)) {
                    expression = CoercionUtil.applyUnaryCoercion (compilationContext, expression, expectedType);
                }
                type = expression.getType ();
            }
        }

        if (type == null) {
            throw new BrygJitException ("Could not get type for variable '" + name + "'.", getLine ());
        }

        variable = new LocalVariable (type, name, ctx.mutability.getType () == BrygLexer.MUT);
        System.out.println (compilationContext.getCurrentScope ().getClass ());
        compilationContext.getCurrentScope ().registerLocalVariable (variable);
    }

    @Override
    public void compile () {
        System.out.println ("Compile var decl!");

        if (expression != null) {
            new VariableExpression (compilationContext, getLine (), variable, AccessMode.set, expression).compile ();
            // ->
        }else {
            throw new UnsupportedOperationException ("Currently a variable must be declared with an expression, " +
                "default values for types are not yet implemented!");
        }
    }

}
