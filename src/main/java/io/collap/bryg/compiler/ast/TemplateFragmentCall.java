package io.collap.bryg.compiler.ast;

import bryg.org.objectweb.asm.Label;
import io.collap.bryg.Unit;
import io.collap.bryg.closure.Closure;
import io.collap.bryg.compiler.helper.ObjectCompileHelper;
import io.collap.bryg.compiler.scope.Variable;
import io.collap.bryg.compiler.util.OperationUtil;
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
import io.collap.bryg.environment.Environment;
import io.collap.bryg.exception.BrygJitException;
import io.collap.bryg.model.Model;
import io.collap.bryg.parser.BrygParser;
import io.collap.bryg.template.TemplateType;
import io.collap.bryg.unit.ParameterInfo;
import io.collap.bryg.unit.StandardUnit;
import io.collap.bryg.unit.UnitClassLoader;
import io.collap.bryg.unit.UnitType;

import javax.annotation.Nullable;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import static bryg.org.objectweb.asm.Opcodes.*;

public class TemplateFragmentCall extends Node {

    /**
     * Either of these two must be 'null'.
     */
    private TemplateType calledTemplate;
    private Variable calledClosure;

    private @Nullable List<ArgumentExpression> argumentExpressions;
    private ClosureDeclarationNode closure;

    public TemplateFragmentCall (Context context, BrygParser.TemplateFragmentCallContext ctx) {
        super (context);
        setLine (ctx.getStart ().getLine ());

        findCalledUnit (ctx);

        /* Get argument expressions. */
        if (ctx.argumentList () != null) {
            argumentExpressions = FunctionUtil.parseArgumentList (context, ctx.argumentList ());
        }

        if (ctx.closure () != null) {
            closure = new ClosureDeclarationNode (context, ctx.closure ());
        }else {
            if (calledTemplate != null) {
                /* Check if closure is expected. */
                boolean closureExpected = false;
                List<ParameterInfo> parameters = calledTemplate.getParameters ();
                for (ParameterInfo parameterInfo : parameters) {
                    if (parameterInfo.getType ().similarTo (Closure.class) && !parameterInfo.isOptional ()) {
                        closureExpected = true;
                        break;
                    }
                }

                if (closureExpected) {
                    throw new BrygJitException ("Template '" + calledTemplate.getFullName () + "' expects a closure.", getLine ());
                }
            }
        }
    }

    private void findCalledUnit (BrygParser.TemplateFragmentCallContext ctx) {
        String fullName = ctx.templateId ().getText ().substring (1); /* Omit the AT (@). */

        /* Check if there is a closure variable that can be called. */
        Variable variable = context.getCurrentScope ().getVariable (fullName);
        if (variable != null) {
            calledClosure = variable;
            calledTemplate = null;
            return;
        }

        /* Check if the parent package needs to be prepended. */
        if (ctx.templateId ().currentPackage != null) {
            fullName = context.getUnitType ().getClassPackage () + fullName;
        }else {
            fullName = UnitClassLoader.getPrefixedName (fullName);
        }

        calledTemplate = context.getEnvironment ().getTemplateTypePrefixed (fullName);
    }

    @Override
    public void compile () {
        BrygMethodVisitor mv = context.getMethodVisitor ();

        if (calledTemplate != null) {
            compileTemplateFetch ();
        }else if (calledClosure != null) {
            compileClosureFetch ();
        }else {
            throw new BrygJitException ("The ID does not refer to a template or a closure.", getLine ());
        }

        /* Load writer. (Argument 0) */
        mv.visitVarInsn (ALOAD, context.getRootScope ().getVariable ("writer").getId ());
        // -> Writer

        /* Create model. (Argument 1) */
        new ObjectCompileHelper (mv, new Type (BasicModel.class)).compileNew ();

        /* Compile arguments and set model variables. */
        compileArguments ();

        /* Compile closure and set closure parameter. */
        if (closure != null) {
            compileClosure ();
        }

        /* Invoke render method. */
        mv.visitMethodInsn (INVOKEINTERFACE, new Type (Unit.class).getAsmType ().getInternalName (),
                "render", TypeHelper.generateMethodDesc (
                        new Class[] { Writer.class, Model.class },
                        Void.TYPE
                ), true);
        // Template, Writer, Model ->
    }


    private void compileTemplateFetch () {
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

        mv.visitLdcInsn (calledTemplate.getFullName ());
        // -> String

        /* This assumes that the full name of the unit type is already prefixed. */
        mv.visitMethodInsn (INVOKEINTERFACE, environmentType.getAsmType ().getInternalName (),
                "getTemplatePrefixed", TypeHelper.generateMethodDesc (
                        new Class[] { String.class },
                        Template.class
                ), true);
        // Environment, String -> Template
    }

    /**
     * This method only checks whether the closure is null if the variable is declared optional.
     */
    private void compileClosureFetch () {
        BrygMethodVisitor mv = context.getMethodVisitor ();

        mv.visitVarInsn (ALOAD, calledClosure.getId ());
        // -> Closure

        if (calledClosure.isNullable ()) {
            OperationUtil.compileIfNullThrowException (mv, new Type (NullPointerException.class), "Closure variable '" +
                calledClosure.getName () + "' is null.");
            // ->
        }
    }

    private void compileArguments () {
        BrygMethodVisitor mv = context.getMethodVisitor ();

        if (argumentExpressions != null) {
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
                } else {
                    argument.compile ();
                    // -> T
                }

                mv.visitMethodInsn (INVOKEINTERFACE, new Type (Model.class).getAsmType ().getInternalName (),
                        "setVariable", TypeHelper.generateMethodDesc (
                                new Class[]{String.class, Object.class},
                                Void.TYPE
                        ), true);
                // Model, String, T ->

                if (afterArgument != null) {
                    mv.visitLabel (afterArgument);
                }
            }
        }
    }

    private void compileClosure () {
        BrygMethodVisitor mv = context.getMethodVisitor ();

        mv.visitInsn (DUP);
        // Model -> Model, Model

        ParameterInfo closureParameter = null;
        for (ParameterInfo parameterInfo : calledTemplate.getParameters ()) {
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
            throw new BrygJitException ("Expected closure parameter for template '" + calledTemplate.getFullName () + "'", getLine ());
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

}
