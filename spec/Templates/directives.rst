Directives
==========

Directives configure different aspects that concern the whole template.

.. todo::
  What about typedefs? The feature would be helpful to reduce verbosity with Closures.


use
---
The ``use`` directive allows the template to use the global variables and functions of a module registered as explicit with an environment.

.. code-block:: bryg

  use html


import
------
The ``import`` directive overrides the environment-wide *simple name* to *full name* mapping, which is constructed by the registered class resolvers.

.. code-block:: bryg

  import some.package.ClassName

This can also be used to import template names.

.. code-block:: bryg

  import @some.package.Template
