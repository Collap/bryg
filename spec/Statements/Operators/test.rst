Relational and Logical
======================

Equality
--------

Value Equality
~~~~~~~~~~~~~~
Value equality (``==``) compares two objects or primitives by value. For objects this means that they are compared with the standard ``equals`` method. Primitive values are compared with their respective instructions.

.. code-block:: bryg

  1 == 2  // Yields false

The value inequality operator (``!=``) is effectively equal to ``not (a == b)``.

Referencial Equality
~~~~~~~~~~~~~~~~~~~~
Referential equality (``===``) can only apply to objects, as it compares the references directly. Referential equality is a stronger form of value equality, because it is guaranteed that the value of two objects is equal when both references point to the same object. Hence the *stronger* operator syntax.

.. code-block:: bryg

  a === b

The referential inequality operator (``!==``) is effectively equal to ``not (a === b)``.


Relational Operators
--------------------
The less than or equals operator (``<=``) returns ``true`` if the left numeric operand is less than or equal to the right numeric operand:

.. code-block:: bryg

  1 <= 4  // Yields true

The greater than or equals operator (``>=``) returns ``true`` if the left numeric operand is greater than or equal to the right numeric operand:

.. code-block:: bryg

  1 >= 4  // Yields false

The greater than operator (``>``) returns ``true`` if the left numeric operand is greater than the right numeric operand:

.. code-block:: bryg

  1 > 4   // Yields false

The less than operator (``<``) returns ``true`` if the left numeric operand is less than the right numeric operand:

.. code-block:: bryg

  1 < 4   // Yields true


Type Test
---------
The ``is`` operator does the same as the Java ``instanceof`` operator. It has not been implemented yet.


Logical Operators
-----------------
Logical operators are lexically keywords, in contrast to other operators, to distinguish these operators further.

The logical ``not`` inverts a boolean expression:

.. code-block:: bryg

  not true  // Yields false

The logical ``and`` takes two boolean expressions:

.. code-block:: bryg

  true and false  // Yields false

The ``and`` operator does not evaluate the right expression if the left expression already yielded ``false``.

The logical ``or`` takes two boolean expressions:

.. code-block:: bryg

  true or false   // Yields true

The ``or`` operator does not evaluate the right expression if the left expression already yielded ``true``.
