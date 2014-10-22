package io.collap.bryg.compiler.ast;

import bryg.org.objectweb.asm.Label;
import io.collap.bryg.closure.Closure;
import io.collap.bryg.compiler.helper.ObjectCompileHelper;
import io.collap.bryg.model.BasicModel;
import io.collap.bryg.template.Template;
import io.collap.bryg.compiler.ast.expression.ArgumentExpression;
import io.collap.bryg.compiler.ast.expression.coercion.BoxingExpression;
import io.collap.bryg.compiler.bytecode.BrygMethodVisitor;
import io.collap.bryg.compiler.context.Context;
import io.collap.bryg.compiler.type.Type;
import io.collap.bryg.compiler.type.TypeHelper;
import io.collap.bryg.compiler.util.BoxingUtil;
import io.collap.bryg.compiler.util.FunctionUtil;
import io.collap.bryg.compiler.util.IdUtil;
import io.collap.bryg.environment.Environment;
import io.collap.bryg.exception.BrygJitException;
import io.collap.bryg.model.Model;
import io.collap.bryg.parser.BrygParser;
import io.collap.bryg.unit.ParameterInfo;
import io.collap.bryg.unit.StandardUnit;
import io.collap.bryg.unit.UnitType;

import java.io.Writer;
import java.util.List;

import static bryg.org.objectweb.asm.Opcodes.*;

public class TemplateFragmentCall extends Node {

    private UnitType calledUnitType;
    private List<ArgumentExpression> argumentExpressions;
    private ClosureDeclarationNode closure;

    public TemplateFragmentCall (Context context, BrygParser.TemplateFragmentCallContext ctx) {
        super (context);
        setLine (ctx.getStart ().getLine ());

        String templateName = IdUtil.templateIdToString (ctx.templateId (), context.getUnitType ().getClassPackage ());
        calledUnitType = context.getEnvironment ().getTemplateTypePrefixed (templateName);

        /* Get argument expressions. */
        argumentExpressions = FunctionUtil.parseArgumentList (context, ctx.argumentList ());

        if (ctx.closure () != null) {
            closure = new ClosureDeclarationNode (context, ctx.closure ());
        }
    }

    @Override
    public void compile () {
        BrygMethodVisitor mv = context.getMethodVisitor ();
        Type environmentType = new Type (Environment.class);

        /* Get environment. */
        mv.visitVarInsn (ALOAD, context.getRootScope ().getVariable ("this").getId ());
        // -> StandardTemplate

        mv.visitFieldInsn (GETFIELD, new Type (StandardUnit.class).getAsmType ().getInternalName (),
                "environment", environmentType.getAsmType ().getDescriptor ());
        // StandardTemplate -> Environment

        /* Get template with environment. (Method owning object) */
        mv.visitInsn (DUP);
        // Environment -> Environment, Environment

        int environmentVariableId = context.getCurrentScope ().calculateNextId (environmentType);
        mv.visitVarInsn (ASTORE, environmentVariableId);
        // Environment ->

        mv.visitLdcInsn (calledUnitType.getFullName ());
        // -> String

        /* This assumes that the full name of the unit type is already prefixed. */
        mv.visitMethodInsn (INVOKEINTERFACE, environmentType.getAsmType ().getInternalName (),
                "getTemplatePrefixed", TypeHelper.generateMethodDesc (
                        new Class[] { String.class },
                        Template.class
                ), true);
        // Environment, String -> Template

        /* Load writer. (Argument 0) */
        mv.visitVarInsn (ALOAD, context.getRootScope ().getVariable ("writer").getId ());
        // -> Writer

        /* Create model. (Argument 1) */
        new ObjectCompileHelper (mv, new Type (BasicModel.class)).compileNew ();

        /* Compile arguments and set model variables. */
        for (ArgumentExpression argument : argumentExpressions) {
            if (argument.getName () == null) {
                throw new BrygJitException ("All arguments to a template must be named.", getLine ());
            }

            /* Compile predicate. */
            Label afterArgument = argument.compilePredicate ();

            mv.visitInsn (DUP);
            // Model -> Model, Model

            mv.visitLdcInsn (argument.getName ());
            // -> String

            /* Possibly box the argument. */
            Type boxedType = BoxingUtil.boxType (argument.getType ());
            if (boxedType != null) {
                new BoxingExpression (context, argument, boxedType).compile ();
                // -> T
            }else {
                argument.compile ();
                // -> T
            }

            mv.visitMethodInsn (INVOKEINTERFACE, new Type (Model.class).getAsmType ().getInternalName (),
                    "setVariable", TypeHelper.generateMethodDesc (
                            new Class[] { String.class, Object.class },
                            Void.TYPE
                    ), true);
            // Model, String, T ->

            if (afterArgument != null) {
                mv.visitLabel (afterArgument);
            }
        }

        /* Compile closure and set closure parameter. */
        if (closure != null) {
            mv.visitInsn (DUP);
            // Model -> Model, Model

            ParameterInfo closureParameter = null;
            for (ParameterInfo parameterInfo : calledUnitType.getParameters ()) {
                if (parameterInfo.getType ().similarTo (Closure.class)) {
                    if (closureParameter == null) {
                        closureParameter = parameterInfo;
                    } else {
                        throw new BrygJitException ("Found two or more closure parameters: "
                                + closureParameter.getName () + ", " + parameterInfo.getName (), getLine ());
                    }
                }
            }

            if (closureParameter == null) {
                throw new BrygJitException ("Expected closure parameter for template '" + calledUnitType.getFullName () + "'", getLine ());
            }

            mv.visitLdcInsn (closureParameter.getName ());
            // -> String

            closure.compile ();
            // -> Closure

            mv.visitMethodInsn (INVOKEINTERFACE, new Type (Model.class).getAsmType ().getInternalName (),
                    "setVariable", TypeHelper.generateMethodDesc (
                            new Class[]{String.class, Object.class},
                            Void.TYPE
                    ), true);
            // Model, String, Closure ->
        }

        /* Invoke render method. */
        mv.visitMethodInsn (INVOKEINTERFACE, new Type (Template.class).getAsmType ().getInternalName (),
                "render", TypeHelper.generateMethodDesc (
                        new Class[] { Writer.class, Model.class },
                        Void.TYPE
                ), true);
        // Template, Writer, Model ->
    }

}
