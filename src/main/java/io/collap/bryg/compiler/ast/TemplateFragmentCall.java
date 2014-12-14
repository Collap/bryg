package io.collap.bryg.compiler.ast;

import bryg.org.objectweb.asm.Label;
import io.collap.bryg.Unit;
import io.collap.bryg.closure.Closure;
import io.collap.bryg.closure.ClosureType;
import io.collap.bryg.closure.StandardClosure;
import io.collap.bryg.compiler.ast.expression.VariableExpression;
import io.collap.bryg.compiler.helper.ObjectCompileHelper;
import io.collap.bryg.compiler.scope.Variable;
import io.collap.bryg.compiler.scope.VariableInfo;
import io.collap.bryg.compiler.type.Types;
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

// TODO: Add coercion to arguments.

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
                    List<VariableInfo> parameters;
                    if (isFragmentInternal) {
                        parameters = calledFragment.getLocalParameters ();
                    }else {
                        parameters = calledFragment.getAllParameters ();
                    }

                    if (parameters.size () < argumentExpressions.size ()) {
                        throw new BrygJitException ("The fragment call has more arguments than parameters expected by" +
                                " the fragment", getLine ());
                    }

                    for (int i = 0; i < argumentExpressions.size (); ++i) {
                        ArgumentExpression argumentExpression = argumentExpressions.get (i);
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
            closure = new ClosureDeclarationNode (context, ctx.closure ());
        }else {
            if (calledFragment != null) {
                /* Check if closure is expected and if an argument exists. */
                boolean closureNotSupplied = false;
                List<VariableInfo> parameters = calledFragment.getAllParameters ();

                L_outer:
                for (VariableInfo parameter : parameters) {
                    if (parameter.getType ().similarTo (Closure.class) && !parameter.isNullable ()) {
                        if (argumentExpressions != null) {
                            for (ArgumentExpression argumentExpression : argumentExpressions) {
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
        Variable variable = context.getCurrentScope ().getVariable (fullName);
        if (variable != null && variable.getType ().similarTo (Closure.class)) {
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
                new VariableExpression (context, getLine (),
                        context.getHighestLocalScope ().getVariable (StandardClosure.PARENT_FIELD_NAME), AccessMode.get);
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
            new ObjectCompileHelper (mv, Types.fromClass (BasicModel.class)).compileNew (
                    TypeHelper.generateMethodDesc (
                            new Class[]{Model.class},
                            Void.TYPE
                    ),
                    new ArrayList<Node> () {{
                        Variable model = context.getHighestLocalScope ().getVariable ("model");
                        /*
                        TODO: Create new model when call from closure.
                        if (isCallInClosure) {
                            model = context.getHighestLocalScope ().getVariable (ClosureType.PARENT_MODEL_FIELD_NAME);
                        } else {
                            model
                        }
                        */
                        add (new VariableExpression (context, getLine (), model, AccessMode.get));
                    }}
            );
        }else {
            /* Just create a new model. */
            new ObjectCompileHelper (mv, Types.fromClass (BasicModel.class)).compileNew ();
        }
        // -> BasicModel

        compileArguments ();
        // Model -> Model

        TemplateType owner = calledFragment.getOwner ();
        context.getUnitType ().getParentTemplateType ().getReferencedTemplates ().add (owner);
        compileFragmentInvocation (calledFragment.getName (), owner.getInternalName (), false);
    }

    private void compileTemplateFetch () {
        BrygMethodVisitor mv = context.getMethodVisitor ();
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

        int environmentVariableId = context.getCurrentScope ().calculateNextId (environmentType);
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
        BrygMethodVisitor mv = context.getMethodVisitor ();

        compileClosureFetch ();

         /* Load writer. (Argument 0) */
        loadWriter ();
        // -> Writer

        /* Create model. (Argument 1) */
        new ObjectCompileHelper (mv, Types.fromClass (BasicModel.class)).compileNew ();

        /* Compile arguments and set model variables. */
        compileArguments ();

        /* Invoke render method. */
        compileFragmentInvocation ("render", Types.fromClass (Unit.class).getInternalName (), true);
    }

    /**
     * ->
     *
     * This method only checks whether the closure is null if the variable is declared optional.
     */
    private void compileClosureFetch () {
        BrygMethodVisitor mv = context.getMethodVisitor ();

        new VariableExpression (context, getLine (), calledClosure, AccessMode.get).compile ();
        // -> Closure

        if (calledClosure.isNullable ()) {
            OperationUtil.compileIfNullThrowException (mv, Types.fromClass (NullPointerException.class), "Closure variable '" +
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
                Type wrapperType = argument.getType ().getWrapperType ();
                if (wrapperType != null) {
                    new BoxingExpression (context, argument, wrapperType).compile ();
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

        VariableInfo closureParameter = findClosureParameter (calledFragment.getLocalParameters ());
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
        new VariableExpression (context, getLine (), context.getHighestLocalScope ().getVariable ("this"), AccessMode.get).compile ();
    }

    private void loadWriter () {
        new VariableExpression (context, getLine (), context.getHighestLocalScope ().getVariable ("writer"), AccessMode.get).compile ();
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
