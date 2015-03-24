Lexical Characteristics
=======================

Keywords
--------
The following keywords are currently defined in bryg:

``and``, ``each``, ``else``, ``false``, ``frag``, ``if``, ``in``, ``is``, ``mut``, ``not``, ``null``, ``opt``, ``or``, ``true``, ``val``, ``while``

Before bryg is ready for production, keywords may be added or removed from the list.


Comments
--------
A comment begins with two slashes (``//``) and ends before a newline token.

There are no multi-line comments.


Identifiers
-----------
An identifier consists of alphanumeric characters. Underscores are **not** supported yet. Identifiers are case sensitive.

Keywords supersede identifiers. If you need an identifier that equals a keyword, you can use backticks:

.. code-block:: bryg

  `and` = 10

Backticks can also be used with non-keyword identifiers. This notation does **not** allow any special characters not supported by the normal identifier notation.


Indentation
-----------
A block is recognized if the indentation on the next line is greater than the indentation on the current line. This indentation is placed on a stack. The end of the block is reached when any of the following lines (lines consisting **only** of whitespace and comments are not considered) has an indentation less than that placed on the stack for the respective block.

Block strings are read in the same way.

In general, a bryg document should be indented with spaces, but tabs are handled as well. In the style of Python, the bryg lexer does the following when it encounters a tab:

A tab counts as 1 or 2 spaces. Is the indentation a multiple of 2, 2 spaces are added to the indentation. Otherwise, only 1 space is added. This means that, for example, a space and a tab at the beginning of a line count only as 2 indents, instead of 3, were we using a fixed tab value. We chose the value of 2, because we expect the standard indentation per block to be 2, instead of 4 or 8, because HTML code (and in consequence, HTML generating bryg code) often relies on deeply nested structures.

If you want to use tabs, set the indentation in your text editor to 2 for bryg files. The algorithm above allows you to mix spaces and tabs freely, but this is generally a bad practice, because other programs (and programmers) might display tabs differently, which will be confusing **especially** if you mix tabs and spaces. We recommend just using spaces.
