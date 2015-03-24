Bitwise and Shift
=================

As bryg is a template language, the operators defined in this section are probably not frequently used, but they are still included to allow powerful expressions.

All shift operations work according to the respective JVM bytecode instructions.


Bitwise Operators
-----------------
The bitwise not (``~``) flips all bytes of an integer expression:

.. code-block:: bryg

  val a = 0
  ~a          // Yields 0xFFFFFFFF

The bitwise and (``&``) applies the bitwise AND operation to two integers:

.. code-block:: bryg

  val a = 3
  val b = 2
  a & b       // Yields 0x00000002

The bitwise xor (``^``) applies the bitwise XOR operation to two integers:

.. code-block:: bryg

  val a = 3
  val b = 2
  a ^ b	      // Yields 0x00000001

The bitwise or (``|``) applies the bitwise OR operation to two integers:

.. code-block:: bryg

  val a = 1
  val b = 2
  a | b       // Yields 0x00000003


Shift Operators
---------------
The left shift operator (``<<``) shifts the left integer operand to the left, by an amount determined by the right integer operand:

.. code-block:: bryg

  val a = 1
  a << 2      // Yields 4

The right signed shift operator (``>>``) shifts the left integer operand to the right, by an amount determined by the right integer operand. Signed means that the sign of the integer is preserved:

.. code-block:: bryg

  val a = -16
  a >> 2      // Yields -4

The right unsigned shift operator (``>>>``) shifts the left integer operand to the right, by an amount determined by the right integer operand. Unsigned means that the sign of the integer is not preserved, which is the contrast to the signed right shift operator:

.. code-block:: bryg

  val a = -16
  a >>> 2     // Yields 1073741820
