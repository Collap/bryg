package io.collap.bryg.internal.compiler.ast;

import bryg.org.objectweb.asm.Label;
import bryg.org.objectweb.asm.Opcodes;
import io.collap.bryg.Unit;
import io.collap.bryg.Closure;
import io.collap.bryg.internal.*;
import io.collap.bryg.internal.compiler.ast.expression.ClosureInstantiationExpression;
import io.collap.bryg.internal.compiler.ast.expression.VariableExpression;
import io.collap.bryg.internal.compiler.util.ObjectCompileHelper;
import io.collap.bryg.internal.type.Types;
import io.collap.bryg.internal.compiler.util.IdUtil;
import io.collap.bryg.internal.compiler.util.OperationUtil;
import io.collap.bryg.MapModel;
import io.collap.bryg.Template;
import io.collap.bryg.internal.compiler.ast.expression.ArgumentExpression;
import io.collap.bryg.internal.compiler.ast.expression.coercion.BoxingExpression;
import io.collap.bryg.internal.compiler.BrygMethodVisitor;
import io.collap.bryg.internal.compiler.CompilationContext;
import io.collap.bryg.internal.type.TypeHelper;
import io.collap.bryg.internal.compiler.util.FunctionUtil;
import io.collap.bryg.Environment;
import io.collap.bryg.BrygJitException;
import io.collap.bryg.Model;
import io.collap.bryg.parser.BrygParser;
import io.collap.bryg.internal.FragmentInfo;
import io.collap.bryg.internal.TemplateType;

import javax.annotation.Nullable;
import java.io.Writer;
import java.util.List;

import static bryg.org.objectweb.asm.Opcodes.*;

// TODO: Add coercion to arguments.

public class TemplateFragmentCall extends Node {

    /**
     * Only one may be set.
     */
    private FragmentInfo calledFragment;
    private Variable calledClosure;

    private boolean isFragmentInternal;
    private boolean isCallInClosure;

    private @Nullable List<ArgumentExpression> generalArgumentExpressions;
    private @Nullable List<ArgumentExpression> localArgumentExpressions;
    private ClosureInstantiationExpression closure;

    public TemplateFragmentCall (CompilationContext compilationContext, BrygParser.TemplateFragmentCallContext ctx) {
        super (compilationContext);
        setLine (ctx.getStart ().getLine ());

        isCallInClosure = compilationContext.getUnitType () instanceof ClosureType;
        isFragmentInternal = false;
        findCalledUnit (ctx);

        /* Get argument expressions. */
        if (ctx.argumentList () != null) {



            localArgumentExpressions = FunctionUtil.parseArgumentList (compilationContext, ctx.argumentList ());

            if (calledFragment != null) {
                /* Infer parameter/argument names. */
                boolean shouldInferNames = false;

                /* Check first whether to infer or not, because we need to check whether the
                   order of arguments is correct even if some arguments are named. */
                for (ArgumentExpression argumentExpression : localArgumentExpressions) {
                    if (argumentExpression.getName () == null) {
                        shouldInferNames = true;
                        break;
                    }
                }

                if (shouldInferNames) {
                    List<VariableInfo> parameters;
                    if (isFragmentInternal) {
                        parameters = calledFragment.getParameters();
                    }else {
                        parameters = calledFragment.getAllParameters ();
                    }

                    if (parameters.size () < localArgumentExpressions.size ()) {
                        throw new BrygJitException ("The fragment call has more arguments than parameters expected by" +
                                " the fragment", getLine ());
                    }

                    for (int i = 0; i < localArgumentExpressions.size (); ++i) {
                        ArgumentExpression argumentExpression = localArgumentExpressions.get (i);
                        VariableInfo parameter = parameters.get (i);
                        if (argumentExpression.getName () != null) {
                            if (!parameter.getName ().equals (argumentExpression.getName ())) {
                                throw new BrygJitException ("Argument " + i + " is invalid: Expected name '" +
                                        parameter.getName () + "' but read '" + argumentExpression.getName () + "'.", getLine ());
                            }
                        } else {
                            argumentExpression.setName (parameter.getName ());
                        }
                    }
                }
            }
        }

        if (ctx.closure () != null) {
            closure = new ClosureInstantiationExpression(compilationContext, ctx.closure ());
        }else {
            if (calledFragment != null) {
                /* Check if closure is expected and if an argument exists. */
                boolean closureNotSupplied = false;
                List<VariableInfo> parameters = calledFragment.getAllParameters ();

                L_outer:
                for (VariableInfo parameter : parameters) {
                    if (parameter.getType ().similarTo (Closure.class) && !parameter.isNullable ()) {
                        if (localArgumentExpressions != null) {
                            for (ArgumentExpression argumentExpression : localArgumentExpressions) {
                                String name = argumentExpression.getName ();
                                if (name != null && name.equals (parameter.getName ())) {
                                    continue L_outer;
                                }
                            }
                        }
                        closureNotSupplied = true;
                        break;
                    }
                }

                if (closureNotSupplied) {
                    throw new BrygJitException ("Fragment '" + calledFragment.getOwner ().getFullName () +
                            ":" + calledFragment.getName () + "' expects a closure.", getLine ());
                }
            }
        }
    }

    private void findCalledUnit (BrygParser.TemplateFragmentCallContext ctx) {
        String fullName = ctx.templateId ().getText ().substring (1); /* Omit the AT (@). */

        /* Check if there is a closure variable that can be called. */
        Variable variable = compilationContext.getCurrentScope ().getVariable (fullName);
        if (variable != null && variable.getType ().similarTo (Closure.class)) {
            calledClosure = variable;
            calledFragment = null;
            return;
        }

        /* Check if there is a local fragment function. */
        {
            TemplateType templateType = compilationContext.getUnitType ().getParentTemplateType ();
            FragmentInfo fragmentInfo = templateType.getFragment (fullName);
            if (fragmentInfo != null) {
                calledClosure = null;
                calledFragment = fragmentInfo;
                isFragmentInternal = true;
                return;
            }
        }

        /* Check if the parent package needs to be prepended. */
        if (ctx.templateId ().currentPackage != null) {
            fullName = compilationContext.getUnitType ().getClassPackage () + fullName;
        }else {
            fullName = StandardClassLoader.getPrefixedName(fullName);
        }

        /* Get the name for the fragment. */
        String fragName;
        if (ctx.frag != null) {
            fragName = IdUtil.idToString (ctx.frag);
        }else {
            fragName = "render"; // TODO: Make this a universal constant.
        }

        TemplateType templateType = compilationContext.getEnvironment ().getTemplateTypePrefixed (fullName);
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
        BrygMethodVisitor mv = compilationContext.getMethodVisitor ();

        if (isFragmentInternal) {
            if (isCallInClosure) {
                new VariableExpression (compilationContext, getLine (),
                        compilationContext.getFragmentScope().getVariable (StandardClosure.PARENT_FIELD_NAME),
                        AccessMode.get).compile ();
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

        /* Load arguments. */
        compileArguments (false);
        // -> a0, a1, a2, ...

        /* Call the __direct function. */
        TemplateType owner = calledFragment.getOwner ();
        compilationContext.getUnitType ().getParentTemplateType ().getReferencedTemplates ().add (owner);
        mv.visitMethodInsn (INVOKEVIRTUAL, calledFragment.getOwner ().getInternalName (),
                calledFragment.getDirectName (), calledFragment.getDesc (), false);
    }

    private void compileTemplateFetch () {
        BrygMethodVisitor mv = compilationContext.getMethodVisitor ();
        Type environmentType = Types.fromClass (Environment.class);

        /* Get environment. */
        loadThis ();
        // -> StandardTemplate

        mv.visitFieldInsn (GETFIELD, Types.fromClass (StandardUnit.class).getInternalName (),
                StandardUnit.ENVIRONMENT_FIELD_NAME, environmentType.getDescriptor ());
        // StandardTemplate -> Environment

        /* Get template with environment. (Method owning object) */
        mv.visitInsn (DUP);
        // Environment -> Environment, Environment

        // TODO: Why is this stored here?!
        int environmentVariableId = compilationContext.getCurrentScope ().calculateNextId (environmentType);
        mv.visitVarInsn (ASTORE, environmentVariableId);
        // Environment ->

        mv.visitLdcInsn (calledFragment.getOwner ().getFullName ());
        // -> String

        /* This assumes that the full name of the unit type is already prefixed. */
        mv.visitMethodInsn (INVOKEINTERFACE, environmentType.getInternalName (),
                "getTemplatePrefixed", TypeHelper.generateMethodDesc (
                        new Class[] { String.class },
                        Template.class
                ), true);
        // Environment, String -> Template

        mv.visitTypeInsn (CHECKCAST, calledFragment.getOwner ().getInternalName ());
        // Template -> T extends Template
    }

    private void compileClosureCall () {
        BrygMethodVisitor mv = compilationContext.getMethodVisitor ();

        compileClosureFetch ();

         /* Load writer. (Argument 0) */
        loadWriter ();
        // -> Writer

        /* Create model. (Argument 1) */
        new ObjectCompileHelper (mv, Types.fromClass (MapModel.class)).compileNew ();

        /* Compile arguments and set model variables. */
        compileArguments (true);

        /* Invoke render method. */
        compileFragmentInvocation ("render", Types.fromClass (Unit.class).getInternalName (), true);
    }

    /**
     * ->
     *
     * This method only checks whether the closure is null if the variable is declared optional.
     */
    private void compileClosureFetch () {
        BrygMethodVisitor mv = compilationContext.getMethodVisitor ();

        new VariableExpression (compilationContext, getLine (), calledClosure, AccessMode.get).compile ();
        // -> Closure

        if (calledClosure.isNullable ()) {
            OperationUtil.compileIfNullThrowException (mv, Types.fromClass (NullPointerException.class), "Closure variable '" +
                calledClosure.getName () + "' is null.");
            // ->
        }
    }

    /**
     * useModel == true:
     *      Model -> Model
     *
     * otherwise:
     *      -> argument0, argument1, ...
     *
     * Also compiles the closure argument, if applicable.
     */
    private void compileArguments (boolean useModel) {
        BrygMethodVisitor mv = compilationContext.getMethodVisitor ();

        if (localArgumentExpressions != null) {
            for (ArgumentExpression argument : localArgumentExpressions) {
                /**
                 * The following check is also needed if the direct fragment is called,
                 * to check whether the argument order is correct.
                 */
                if (argument.getName () == null) {
                    throw new BrygJitException ("Argument name was neither supplied nor inferred.", getLine ());
                }

                /* Compile predicate. */
                Label afterArgument = argument.compilePredicate ();
                Label end = new Label ();

                if (useModel) {
                    mv.visitInsn (DUP);
                    // Model -> Model, Model

                    mv.visitLdcInsn (argument.getName ());
                    // -> String

                    /* Possibly box primitives for the model. */
                    Type wrapperType = argument.getType ().getWrapperType ();
                    if (wrapperType != null) {
                        new BoxingExpression (compilationContext, argument, wrapperType).compile ();
                        // -> T
                    } else {
                        argument.compile ();
                        // -> T
                    }

                    mv.visitMethodInsn (INVOKEINTERFACE, Types.fromClass (Model.class).getInternalName (),
                            "setVariable", TypeHelper.generateMethodDesc (
                                    new Class[]{String.class, Object.class},
                                    Void.TYPE
                            ), true);
                    // Model, String, T ->
                }else {
                    argument.compile ();
                    if (afterArgument != null) {
                        mv.visitJumpInsn (GOTO, end);
                    }
                }

                if (afterArgument != null) {
                    mv.visitLabel (afterArgument);
                    if (!useModel) {
                        mv.visitInsn (Opcodes.ACONST_NULL);
                    }
                    mv.visitLabel (end);
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

        BrygMethodVisitor mv = compilationContext.getMethodVisitor ();

        mv.visitInsn (DUP);
        // Model -> Model, Model

        VariableInfo closureParameter = findClosureParameter (calledFragment.getParameters());
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

        mv.visitMethodInsn (INVOKEINTERFACE, Types.fromClass (Model.class).getInternalName (),
                "setVariable", TypeHelper.generateMethodDesc (
                        new Class[]{String.class, Object.class},
                        Void.TYPE
                ), true);
        // Model, String, Closure ->
    }

    private VariableInfo findClosureParameter (List<VariableInfo> parameters) {
        VariableInfo closureParameter = null;
        for (VariableInfo parameter : parameters) {
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
        new VariableExpression (compilationContext, getLine (), compilationContext.getFragmentScope().getVariable ("this"), AccessMode.get).compile ();
    }

    private void loadWriter () {
        new VariableExpression (compilationContext, getLine (), compilationContext.getFragmentScope().getVariable ("writer"), AccessMode.get).compile ();
    }

    private void compileFragmentInvocation (String name, String ownerName, boolean isInterfaceCall) {
        compilationContext.getMethodVisitor ().visitMethodInsn (
                isInterfaceCall ? INVOKEINTERFACE : INVOKEVIRTUAL, ownerName,
                name, TypeHelper.generateMethodDesc (
                        new Class[] { Writer.class, Model.class },
                        Void.TYPE
                ), isInterfaceCall);
        // Template, Writer, Model ->
    }

}
