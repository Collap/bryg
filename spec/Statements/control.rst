Control Flow Statements
=======================

A control flow statement affects the order of execution based on values mostly known at runtime.


Parentheses
-----------
Header information is usually enclosed in parentheses:

.. code-block:: bryg

  if (flag) statement

This is the same for all control flow statements. When the statement takes a block, however, the parentheses may be omitted:

.. code-block:: bryg

  if flag
    statement


If-else
-------
An ``if`` takes a condition and executes its block *if* the condition is true:

.. code-block:: bryg

  if not dead
    : Oh, thank god.

Otherwise, an optional ``else`` block will be executed:

.. code-block:: bryg

  if unicornsExist
    : Yay!
  else
    : Oww. :(


Each
----
An ``each`` iterates an object which class implements the ``Iterable`` interface:

.. code-block:: bryg

  frag listAll (List<Person> persons)
    each person in persons
      li person

It is important to say that the element variable (in the example above, ``person``) is immutable and bound to the local scope of the ``each``. Note that the fields of an object referenced by an immutable variable are not immutable themselves.

If the element type can not be inferred, you need to specify the type:

.. code-block:: bryg

  each Boat boat in harbor.boats()
    li boat.generateDigest()

The type can be specified at any time, and is then checked against the inferred type if possible. The reason why you may need to specify the type is the lack of generic type information in the JVM at runtime, when bryg templates are compiled. Blame type erasure.

.. todo::
  Arrays are currently not supported in bryg.


While
-----
A ``while`` executes its block *while* a condition is true:

.. code-block:: bryg

  while n > 1
    n /= 2


For
---
Bryg also supports traditional ``for`` loops, but they should be used sparsely. An ``each`` loop is often the better option.

.. code-block:: bryg

  p: Even numbers from 0 to 10:
  ul
    for mut i = 0, i <= 10, i += 2
      li i

The generic ``for (init, cond, post) block`` is effectively equal to the following while loop:

.. code-block:: bryg

  discard init
  while cond
    block
    discard post

Note the pseudo-invocations of ``discard``, which ensure that the ``for`` does not print control flow information.
