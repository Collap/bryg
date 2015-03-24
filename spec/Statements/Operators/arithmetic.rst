Arithmetic
==========

All arithmetic operands work like the corresponding JVM bytecode instructions.

There are no operators for incrementation and decrementation.


Negation
--------
The ``-`` prefix negates a numeric expression. In contrast to Java, a negation has to be enclosed in parentheses:

.. code-block:: bryg

  val b = (-1) * a

This design decision was made to `avoid serious ambiguities <http://pennekamp.me/2014/11/18/negative-ambiguities.html>`_.


Addition
--------
The addition operator (``+``) adds two numeric values together:

.. code-block:: bryg

  5 + 10  // Yields 15

The operator is overloaded to concatenate Strings. If one of the two operands is a String, the other operand is converted to a String:

.. code-block:: bryg

  'Hello, ' + name + '!'

The conversion is performed with a ``StringBuilder``, which stringifies primitives directly and uses the ``toString()`` method of the operand for all other types.


Subtraction
-----------
The subtraction operator (``-``) subtracts the right numeric operand from the left numeric operand:

.. code-block:: bryg

  5 - 10  // Yields -5


Multiplication
--------------
The multiplication operator (``*``) multiplies two numeric values:

.. code-block:: bryg

  5 * 10  // Yields 50


Division
--------
The division operator (``/``) divides the left numeric value by the right numeric value:

.. code-block:: bryg

  12 / 3  // Yields 2
  5 / 10  // Yields 0, because all operands are integers, and the result is rounded down in this case.


Remainder
---------
The remainder operator (``%``) computes the remainder of the left operand when divided by the right operand:

.. code-block:: bryg

  5 % 10  // Yields 5
  11 % 3  // Yields 2
