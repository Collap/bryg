Literals
========

Strings
-------
Bryg features 3 ways to specify string literals.

The basic version features single quotes: ``'Hello Bryg'``. There is no support for double quotes. This is to reduce superfluous features of an already complex syntax.

You can also specify a string literal that goes until the end of the line:

.. code-block:: bryg

  : Hello Bryg

Note that all whitespaces before the first and after the last letter are trimmed, so the string in this example is equivalent to the string in the first example. If you need to preserve all whitespaces, use single quote strings.

Multi-line strings are specified with the help of indentation:

.. code-block:: bryg

  :
    This is the first line of the multi-line string.
        This text is indented.
    More text.

The colon must be followed by whitespaces and a newline, and the text itself must be indented, otherwise the multi-line string is not valid. The indentation of the text is preserved, so the first (non-blank) line of the text determines the indentation baseline. In the example above, the second line of the text would be indented by 4 spaces, while the other two lines would not be indented.

.. todo::
  Are \n chars inserted after a line break in multi-line strings?

Interpolation
~~~~~~~~~~~~~
Expressions can be embedded into strings. It is important to distinguish between statements and expressions here: Only a single expression is allowed as an interpolation.

.. code-block:: bryg

  frag greet (String name)
    : Hello, \{name}!

The example above prints the parameter ``name``. Invoking the template with the name ``Bryg`` leads to ``Hello, Bryg!``.

**Note**: Due to the way how interpolations are currently parsed, a ``}`` is not allowed inside an interpolation at all, *including strings*. This should usually not be a problem, but this limitation will be fixed in a future version.


Numbers
-------
Bryg supports integer, long, float and double literals.

Integers are noted down in decimal form and have the type `int`: ``42``.

Long integers have the type `long` and must have an `l` or `L` suffix:

.. code-block:: bryg

  12468l
  10000000000L

Float literals have the type `float` and must have an `f` or `F` suffix:

.. code-block:: bryg

  0.5f
  0.66F

The fraction portion of a float literal may be omitted: ``42f``.

Double literals have the type `double`: ``0.5``. There is no suffix, neither is it allowed to omit the fraction part of the number.


Null
----
The literal ``null`` is a reference that points to a non-existent object. The type of ``null`` is ``Object``.


Booleans
--------
The keywords ``true`` and ``false`` are boolean values. Their type is ``boolean``.
