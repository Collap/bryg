Modules
=======

Modules are a way to extend bryg with global functions and variables.

A module can be registered as implicit or explicit. *Implicit* modules make their functions and variables visible to all templates in the environment. The usage of *explicit* modules needs to be specified by each template, with the ``use`` directive. Strictly speaking, the module itself is neither explicit or implicit, so the same module may be implicit in one environment, while explicit in another. 


Standard
--------
The standard module defines standard globals which can be used by any bryg template. This module is registered by default as implicit.

Discard
~~~~~~~
The ``discard`` function suppresses printing expression results:

.. code-block:: bryg

  mut i = 0
  while i < 10
    : Hi!
    discard ++i

In the example above, the index `i` would not be printed. Discards can be nested, and printing only resumes when **all** discard blocks have been closed.


HTML
----
The HTML module implements all HTML5 tags as functions. HTML tags with content (e.g. ``body``, ``div``) take a block. While additional tags must be registered as global functions, invalid attributes merely cause a warning.

Here is a usage example:

.. code-block:: bryg

  html
    head title: Greetings
    body
      b: Hi!

The example above would be compiled to text similar to the following HTML:

.. code-block:: html

  <html>
    <head>
      <title>Greetings</title>
    </head>
    <body>
      <b>Hi!</b>
    </body>
  </html>

As you can see, the structure of the nested blocks in bryg reflects the nested structure of the HTML output.
