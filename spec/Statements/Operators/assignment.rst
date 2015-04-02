Assignment
==========

The assignment operator (``=``) assigns the right operand to the left mutable variable or field:

.. code-block:: bryg

  a = 15
  human.age = 55

Assignment operators can not be chained, because they do not yield a value. This technically disqualifies them as expressions, but the grammar clearly sees them as expressions.

.. todo::
  This is a formal issue that will be addressed, but the current state can be seen as working as intended, when not looking too closely.


Composites
----------

As listed above in the order of operations, there are a few operators that can be combined with an assignment. In general, ``a op= b`` is the same as ``a = a op b``. For example, ``a += b`` is sematically equal to ``a = a + b``.

The following operators can be combined with an assignment in this way:

* Arithmetic Operators (``+=``, ``-=``, ``*=``, ``/=``, ``%=``)
* Bitwise Operators (``&=``, ``|=``, ``^=``)
* Shift Operators (``<<=``, ``>>>=``, ``>>=``)

Note that for example the ``+=`` operator does not behave exactly like the equivalent in Java when it comes to coercion. For example, this Java code is valid:

.. code-block:: bryg

  int i = 5
  double d = 37.0
  i += d

The equivalent code in bryg would not be valid, because we can't cast a ``double`` down to an ``int`` implicitly. We believe that every conversion beyond basic coercion should be visible at all times, so this is an intentional "fix" for the Java equivalent, where those composites are defined as ``a = (A)(a op b)``.
