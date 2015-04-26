Function Calls
==============

Parentheses
-----------
Function calls in bryg are not required to have parentheses if they take no arguments:

.. code-block:: bryg

  br

In the example, ``br`` could be a variable or a function. If ``br`` is both a function and a variable, the variable supersedes the function, as specified in the Scopes section.

.. todo::
    This should probably only apply to member functions.


Named Arguments
---------------
Arguments may be named:

.. code-block:: bryg

  section(id 'content', class 'orange-box')

Note that the argument name and the argument are simply separated by a space.

If all arguments are named, the order of arguments is not important. If at least one argument is unnamed, all arguments have to be passed in the order in which the parameters are declared. The programmer should decide whether a name should be specified or not, if the choice is given by the compiler. Generally, we recommend specifying the name for boolean arguments, even when calling Java methods.

Argument names enclosed in backticks are allowed to have hyphens:

.. code-block:: bryg

  meta(`http-equiv` 'Content-Type', content 'text/html; charset=UTF-8')

This is mandatory to support all HTML attributes, such as ``http-equiv`` and ``data-*``, which was obviously a concern when designing Bryg.

.. todo::
  Currently in the grammar, names with hyphens are not required nor allowed to be enclosed in backticks. Make sure to change that.


Argument Predicates
-------------------
An argument predicate is a boolean expression. An argument is not passed to the callee if the predicate is ``false``:

.. code-block:: bryg

  head('hey' if flag)

Different functions handle the missing argument differently. More on that in the _Kinds_ section.


Default Function Calls
----------------------
Templates with a default fragment can be called without specifying a fragment name:

.. code-block:: bryg

  template(...)

Where `template` is an object with a Template type. Since closures are defined as templates with a single default fragment, this syntax applies to closures in particular.


Instantiating Implicit Closures
-------------------------------
A closure argument for one implicit parameter can be instantiated directly with a function call, by instantiating a closure after the call's arguments. Here is an example:

.. code-block:: bryg

  @Table.box(id 'content') \
    b: Hi!

The ``box`` fragment takes a closure as an argument, while the HTML function ``b`` takes a statement.


Blocks
------
Some module functions (e.g. functions defined in the HTML module) may take a block or statement after the formal call syntax, instead of a closure. Different module functions handle blocks differently, so you need to consult their specific documentation to find out how exactly.


Kinds
-----
Since v0.5, Bryg has a single, unified syntax for function calls. On the language level, the semantics are the same, but under the hood, functions of different kinds (java methods, fragments, module functions, etc.) are compiled very differently.

Fragments
~~~~~~~~~
**Predicates**: Parameters of template constructors and fragments are assigned their default value when the argument is missing. Note that only optional parameters and fields may have such a default value. An argument predicate for a mandatory parameter is not allowed.

Java Method Calls
~~~~~~~~~~~~~~~~~
Java methods are called directly.

FIXME: What about predicates.

The Java method is selected in two steps:
1. A method is searched that exactly matches the signature of the requested methods (name and argument types).
2. A method with the same name, but parameter types that can be reached by argument coercion is searched. If two or more possible methods are found, an ambiguity error is thrown.

Module Functions
~~~~~~~~~~~~~~~~~~
How a certain module function handles a missing argument due to a predicate has to be gathered from its documentation. To give a specific example, though, all HTML functions simply do not include the attribute (which is represented by a parameter to the function):

.. code-block:: bryg

  tr(class 'odd' if i % 2 == 1)

This would be compiled to ``<tr></tr>`` or ``<tr class="odd"></tr>``, depending on the predicate.
