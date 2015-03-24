Blocks
======

A block is a list of one or more statements. A block is indicated by indentation:

.. code-block:: bryg

  html
    head title: Hello Bryg
    body
      : Hello Bryg

In the example above, all functions, ``html``, ``body`` and ``title``, accept blocks. Every block opens a new local scope.

Often, to simplify the specification, *block* and *statement* are treated as synonyms. So while the ``title`` function above technically accepts a statement instead of a block, since the semantics are the same regardless of whether a block/statement is indented or not, ``title`` is also a function that accepts a block.
