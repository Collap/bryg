Variables
=========

Declarations
------------
Variables need to be declared with either ``mut``, which makes the variable mutable, or ``val``, which makes the variable immutable. Only mutable variables can be changed:

.. code-block:: bryg

  val a = 0
  mut b = 5
  a += b      // Illegal, as a is immutable
  b += a      // Legal, as b is mutable

However, if a variable with an object type is immutable, only the reference is immutable, non-final fields of the object can still be changed. Of course, if a field is not exposed via *public* and there is no corresponding setter, the field is publicly immutable itself.

A variable declaration needs a name, which must be a valid identifier. The variable is then declared in the current scope under said name.

You can specify a type. This is necessary when a type can not be inferred from the expression, but specifying a type can also be beneficial to type safety, as the specified and inferred types are checked for compatibility, if both are available:

.. code-block:: bryg

  val Object obj = 0     // This throws an error, because int is not compatible with Object

.. todo::
  The example code is valid in Java (through boxing) and may be allowed in bryg in the future as well.

To allow the compiler to infer a type, you can initialise the variable directly. **Currently**, this is mandatory, as default values are not implemented yet. In the **future**, this will still be mandatory for immutable variables and variables with types that do not have a default value (such as objects without a parameterless constructor).


Variable Expressions
--------------------
A variable declared in the scope can simply be accessed via its name:

.. code-block:: bryg

  val answer = 42
  : The answer is \{answer}.


Field Access
------------
Field access for Java objects is supported by Bryg with Java syntax:

.. code-block:: bryg

  val name = person.name

Assume in the example above that the type of *person* is *Person*. If the field *name* is public, the field is accessed directly. Otherwise, the Bryg compiler tries to find a getter/setter according to the following naming rules:

1. If the field has the type *boolean*:
  1. If the field has an ``is`` prefix: The getter name is assumed to be the field name. The setter name is the capitalized field name without the ``is``, and prefixed by a ``set``. Example: Let the field name be ``isAlive``; The getter name is ``isAlive``, the setter name is ``setAlive``.
  2. Otherwise: The getter name is an ``is`` followed by the capitalized field name. The setter name is equal to the setter name in the first case.
2. Otherwise: The getter name is a ``get`` followed by a capitalized field name. The setter name is a ``set`` followed by a capitalized field name.

The compiler checks whether the getter/setter meets all the criteria for an access method and throws an error should the signature not match the expected signature.

Method invocation is treated in the section about function calls.
