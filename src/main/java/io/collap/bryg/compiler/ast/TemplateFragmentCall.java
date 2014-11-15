package io.collap.bryg.compiler.ast;

import bryg.org.objectweb.asm.Label;
import io.collap.bryg.Unit;
import io.collap.bryg.closure.Closure;
import io.collap.bryg.closure.ClosureType;
import io.collap.bryg.compiler.ast.expression.VariableExpression;
import io.collap.bryg.compiler.helper.ObjectCompileHelper;
import io.collap.bryg.compiler.scope.Variable;
import io.collap.bryg.compiler.util.IdUtil;
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
import io.collap.bryg.template.TemplateFragmentInfo;
import io.collap.bryg.template.TemplateType;
import io.collap.bryg.unit.*;

import javax.annotation.Nullable;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import static bryg.org.objectweb.asm.Opcodes.*;

public class TemplateFragmentCall extends Node {

    /**
     * Only one may be set.
     */
    private TemplateFragmentInfo calledFragment;
    private Variable calledClosure;

    private boolean isFragmentInternal;
    private boolean isCallInClosure;

    private @Nullable List<ArgumentExpression> argumentExpressions;
    private ClosureDeclarationNode closure;

    public TemplateFragmentCall (Context context, BrygParser.TemplateFragmentCallContext ctx) {
        super (context);
        setLine (ctx.getStart ().getLine ());

        isCallInClosure = context.getUnitType () instanceof ClosureType;
        isFragmentInternal = false;
        findCalledUnit (ctx);

        /* Get argument expressions. */
        if (ctx.argumentList () != null) {
            argumentExpressions = FunctionUtil.parseArgumentList (context, ctx.argumentList ());

            if (calledFragment != null) {
                /* Infer parameter/argument names. */
                boolean shouldInfer = false;

                /* Check first whether to infer or not, because we need to check whether the
                   order of arguments is correct even if some arguments are named. */
                for (ArgumentExpression argumentExpression : argumentExpressions) {
                    if (argumentExpression.getName () == null) {
                        shouldInfer = true;
                        break;
                    }
                }

                if (shouldInfer) {
                    List<ParameterInfo> localParameters = calledFragment.getLocalParameters ();
                    for (int i = 0; i < argumentExpressions.size (); ++i) {
                        ArgumentExpression argumentExpression = argumentExpressions.get (i);
                        ParameterInfo localParameter = localParameters.get (i);
                        if (argumentExpression.getName () != null) {
                            if (!localParameter.getName ().equals (argumentExpression.getName ())) {
                                throw new BrygJitException ("Argument " + i + " is invalid: Expected name '" +
                                        localParameter.getName () + "' but read '" + argumentExpression.getName () + "'.", getLine ());
                            }
                        } else {
                            argumentExpression.setName (localParameter.getName ());
                        }
                    }
                }
            }
        }

        if (ctx.closure () != null) {
            closure = new ClosureDeclarationNode (context, ctx.closure ());
        }else {
            if (calledFragment != null) {
                /* Check if closure is expected. */
                boolean closureExpected = false;
                List<ParameterInfo> parameters = calledFragment.getAllParameters ();
                for (ParameterInfo parameterInfo : parameters) {
                    if (parameterInfo.getType ().similarTo (Closure.class) && !parameterInfo.isOptional ()) {
                        closureExpected = true;
                        break;
                    }
                }

                if (closureExpected) {
                    throw new BrygJitException ("Fragment '" + calledFragment.getOwner ().getFullName () +
                            ":" + calledFragment.getName () + "' expects a closure.", getLine ());
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
            calledFragment = null;
            return;
        }

        /* Check if there is a local fragment function. */
        {
            TemplateType templateType = context.getUnitType ().getParentTemplateType ();
            TemplateFragmentInfo fragmentInfo = templateType.getFragment (fullName);
            if (fragmentInfo != null) {
                calledClosure = null;
                calledFragment = fragmentInfo;
                isFragmentInternal = true;
                return;
            }
        }

        /* Check if the parent package needs to be prepended. */
        if (ctx.templateId ().currentPackage != null) {
            fullName = context.getUnitType ().getClassPackage () + fullName;
        }else {
            fullName = UnitClassLoader.getPrefixedName (fullName);
        }

        /* Get the name for the fragment. */
        String fragName;
        if (ctx.frag != null) {
            fragName = IdUtil.idToString (ctx.frag);
        }else {
            fragName = "render"; // TODO: Make this a universal constant.
        }

        TemplateType templateType = context.getEnvironment ().getTemplateTypePrefixed (fullName);
        if (templateType == null) {
            throw new BrygJitException ("Template " + fullName + " not found for template call!", getLine ());
        }

        calledFragment = templateType.getFragment (fragName);
        if (calledFragment == null) {
            throw new BrygJitException ("Fragment " + fullName + ":" + fragName + " not found for template call!", getLine ());
        }

        calledClosure = null;
    }

    @Override
    public void compile () {
        if (calledFragment != null) {
            compileFragmentCall ();
        }else if (calledClosure != null) {
            compileClosureCall ();
        }else {
            throw new BrygJitException ("The ID does not refer to a template, fragment or a closure.", getLine ());
        }
    }

    private void compileFragmentCall () {
        BrygMethodVisitor mv = context.getMethodVisitor ();

        if (isFragmentInternal) {
            if (isCallInClosure) {
                mv.visitVarInsn (ALOAD, context.getRootScope ().getVariable (ClosureType.PARENT_FIELD_NAME).getId ());
            }else {
                loadThis ();
            }
            // -> T extends Template
        }else {
            compileTemplateFetch ();
        }
        // -> T extends Template

        loadWriter ();
        // -> Writer

        if (isFragmentInternal) {
            /* Pass the current model as a parent of a new model and add the new arguments. */
            new ObjectCompileHelper (mv, new Type (BasicModel.class)).compileNew (
                    TypeHelper.generateMethodDesc (
                            new Class[]{Model.class},
                            Void.TYPE
                    ),
                    new ArrayList<Node> () {{
                        Variable model;
                        if (isCallInClosure) {
                            model = context.getRootScope ().getVariable (ClosureType.PARENT_MODEL_FIELD_NAME);
                        }else {
                            model = context.getRootScope ().getVariable ("model");
                        }
                        add (new VariableExpression (context, model, AccessMode.get, getLine ()));
                    }}
            );
        }else {
            /* Just create a new model. */
            new ObjectCompileHelper (mv, new Type (BasicModel.class)).compileNew ();
        }
        // -> BasicModel

        compileArguments ();
        // Model -> Model

        TemplateType owner = calledFragment.getOwner ();
        context.getUnitType ().getParentTemplateType ().getReferencedTemplates ().add (owner);
        compileFragmentInvocation (calledFragment.getName (), owner.getJvmName (), false);
    }

    private void compileTemplateFetch () {
        BrygMethodVisitor mv = context.getMethodVisitor ();
        Type environmentType = new Type (Environment.class);

        /* Get environment. */
        loadThis ();
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

        mv.visitLdcInsn (calledFragment.getOwner ().getFullName ());
        // -> String

        /* This assumes that the full name of the unit type is already prefixed. */
        mv.visitMethodInsn (INVOKEINTERFACE, environmentType.getAsmType ().getInternalName (),
                "getTemplatePrefixed", TypeHelper.generateMethodDesc (
                        new Class[] { String.class },
                        Template.class
                ), true);
        // Environment, String -> Template

        mv.visitTypeInsn (CHECKCAST, calledFragment.getOwner ().getJvmName ());
        // Template -> T extends Template
    }

    private void compileClosureCall () {
        BrygMethodVisitor mv = context.getMethodVisitor ();

        compileClosureFetch ();

         /* Load writer. (Argument 0) */
        loadWriter ();
        // -> Writer

        /* Create model. (Argument 1) */
        new ObjectCompileHelper (mv, new Type (BasicModel.class)).compileNew ();

        /* Compile arguments and set model variables. */
        compileArguments ();

        /* Invoke render method. */
        compileFragmentInvocation ("render", new Type (Unit.class).getAsmType ().getInternalName (), true);
    }

    /**
     * ->
     *
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

    /**
     * Model -> Model
     *
     * Also compiles the closure argument, if applicable.
     */
    private void compileArguments () {
        BrygMethodVisitor mv = context.getMethodVisitor ();

        if (argumentExpressions != null) {
            for (ArgumentExpression argument : argumentExpressions) {
                if (argument.getName () == null) {
                    throw new BrygJitException ("Argument name was neither supplied nor inferred.", getLine ());
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

        if (closure != null) {
            compileClosure ();
        }
    }

    private void compileClosure () {
        if (calledFragment == null) {
            throw new BrygJitException ("Currently only fragments can be called with closures.", getLine ());
        }

        BrygMethodVisitor mv = context.getMethodVisitor ();

        mv.visitInsn (DUP);
        // Model -> Model, Model

        ParameterInfo closureParameter = findClosureParameter (calledFragment.getLocalParameters ());
        if (closureParameter == null) {
            closureParameter = findClosureParameter (calledFragment.getGeneralParameters ());
        }

        if (closureParameter == null) {
            throw new BrygJitException ("Expected closure parameter for fragment '" +
                    calledFragment.getOwner ().getFullName () +
                    ":" + calledFragment.getName () + "'", getLine ());
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

    private ParameterInfo findClosureParameter (List<ParameterInfo> parameters) {
        ParameterInfo closureParameter = null;
        for (ParameterInfo parameter : parameters) {
            if (parameter.getType ().similarTo (Closure.class)) {
                if (closureParameter == null) {
                    closureParameter = parameter;
                } else {
                    throw new BrygJitException ("Found two or more closure parameters: "
                            + closureParameter.getName () + ", " + parameter.getName (), getLine ());
                }
            }
        }

        return closureParameter;
    }

    private void loadThis () {
        BrygMethodVisitor mv = context.getMethodVisitor ();
        mv.visitVarInsn (ALOAD, context.getRootScope ().getVariable ("this").getId ());
    }

    private void loadWriter () {
        BrygMethodVisitor mv = context.getMethodVisitor ();
        mv.visitVarInsn (ALOAD, context.getRootScope ().getVariable ("writer").getId ());
    }

    private void compileFragmentInvocation (String name, String ownerName, boolean isInterfaceCall) {
        context.getMethodVisitor ().visitMethodInsn (
                isInterfaceCall ? INVOKEINTERFACE : INVOKEVIRTUAL, ownerName,
                name, TypeHelper.generateMethodDesc (
                        new Class[] { Writer.class, Model.class },
                        Void.TYPE
                ), isInterfaceCall);
        // Template, Writer, Model ->
    }

}
