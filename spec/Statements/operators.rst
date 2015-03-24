Operators
=========

.. toctree::
  :hidden:
  :titlesonly:

  Operators/arithmetic
  Operators/bitandshift
  Operators/test
  Operators/assignment

Most operators are like their Java counterparts, so if you work with Java regularly (or any language similar to Java), you might only want to check how some operators differ from Java:

* The ``instanceof`` operator has been renamed to ``is``.
* The ``!``, ``&&``, ``||`` operators have been renamed to ``not``, ``and``, ``or``, respectively.
* References are compared with ``===`` and ``!==``. Using the (value) equality operators ``==`` and ``!=`` on objects calls the ``equals(Object o)`` method of the **left operand**.
* The unary ``+`` operator has been removed.
* The unary ``-`` needs to be enclosed in parentheses.

All operators apply coercion where possible and needed.

.. todo::
  Possibly remove the renaming of the instanceof operator.


Order of Operations
-------------------
Operators are evaluated from left to right in the following order:

* Postfix (``++``, ``--``)
* Prefix (``++``, ``--``, ``-``)
* Unary Operator (``~``, ``not``)
* Multiplication/Division/Remainder (``*``, ``/``, ``%``)
* Addition/Subtraction (``+``, ``-``)
* Shift (``<<``, ``>>>``, ``>>``)
* Relational (``<=``, ``>=``, ``>``, ``<``)
* Type Test (``is``)
* Value Equality (``==``, ``!=``)
* Referential Equality (``===``, ``!==``)
* Bitwise and (``&``)
* Bitwise xor (``^``)
* Bitwise or (``|``)
* Logical and (``and``)
* Logical or (``or``)
* Assignment (``=``, ``+=``, ``-=``, ``*=``, ``/=``, ``%=``, ``&=``, ``|=``, ``^=``, ``<<=``, ``>>>=``, ``>>=``)

An assignment is evaluated from right to left, but does currently not return a value.

Expressions may be grouped with parentheses to override the order of operations:

.. code-block:: bryg

  a * (b + c)

In the example above, ``b + c`` would be evaluated before ``a * (...)``, although the precedence of a multiplication is higher than the precedence of an addition.
