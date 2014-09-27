package io.collap.bryg.compiler.ast;

import io.collap.bryg.StandardTemplate;
import io.collap.bryg.Template;
import io.collap.bryg.compiler.ast.expression.ArgumentExpression;
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

import java.io.Writer;
import java.util.List;

import static bryg.org.objectweb.asm.Opcodes.*;

public class TemplateFragmentCall extends Node {

    private String templateName;
    private List<ArgumentExpression> argumentExpressions;

    public TemplateFragmentCall (Context context, BrygParser.TemplateFragmentCallContext ctx) {
        super (context);
        setLine (ctx.getStart ().getLine ());

        templateName = IdUtil.templateIdToString (ctx.templateId (), context.getTemplatePackage ());

        /* Get argument expressions. */
        argumentExpressions = FunctionUtil.parseArgumentList (context, ctx.argumentList ());
    }

    @Override
    public void compile () {
        BrygMethodVisitor mv = context.getMethodVisitor ();
        Type environmentType = new Type (Environment.class);

        /* Get environment. */
        mv.visitVarInsn (ALOAD, context.getRootScope ().getVariable ("this").getId ());
        // -> StandardTemplate

        mv.visitFieldInsn (GETFIELD, new Type (StandardTemplate.class).getAsmType ().getInternalName (),
                "environment", environmentType.getAsmType ().getDescriptor ());
        // StandardTemplate -> Environment

        /* Get template with environment. (Method owning object) */
        mv.visitInsn (DUP);
        // Environment -> Environment, Environment

        int environmentVariableId = context.getCurrentScope ().calculateNextId (environmentType);
        mv.visitVarInsn (ASTORE, environmentVariableId);
        // Environment ->

        mv.visitLdcInsn (templateName);
        // -> String

        mv.visitMethodInsn (INVOKEINTERFACE, environmentType.getAsmType ().getInternalName (),
                "getTemplate", TypeHelper.generateMethodDesc (
                        new Class[] { String.class },
                        Template.class
                ), true);
        // Environment, String -> Template

        /* Load writer. (Argument 0) */
        mv.visitVarInsn (ALOAD, context.getRootScope ().getVariable ("writer").getId ());
        // -> Writer

        /* Create model with environment. (Argument 1) */
        mv.visitVarInsn (ALOAD, environmentVariableId);
        // -> Environment

        mv.visitMethodInsn (INVOKEINTERFACE, environmentType.getAsmType ().getInternalName (),
                "createModel", TypeHelper.generateMethodDesc (
                        null,
                        Model.class
                ), true);
        // Environment -> Model

        /* Compile arguments and set model variables. */
        for (ArgumentExpression argument : argumentExpressions) {
            if (argument.getName () == null) {
                throw new BrygJitException ("All arguments to a template must be named.", getLine ());
            }

            mv.visitInsn (DUP);
            // Model -> Model, Model

            mv.visitLdcInsn (argument.getName ());
            // -> String

            /* Possibly box the argument. */
            Type boxedType = BoxingUtil.boxType (argument.getType ());
            if (boxedType != null) {
                BoxingUtil.compileBoxing (mv, argument, boxedType);
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
