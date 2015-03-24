Statements
==========

.. toctree::
  :hidden:
  :titlesonly:

  blocks
  literals
  variables
  instantiation
  calls
  control
  operators


In other template languages, statements need to be indicated, while normal text can just be written down. Bryg is the exact opposite: Text needs to be indicated, while statements can be written without any special syntax. The important difference to general-purpose languages like Java is that every statement that is also an expression is printed out to an implicit writer object, unless the value is discarded. This makes it easy to print text, while it takes away the awkward syntax of other template languages.

Statements are terminated by newlines.
