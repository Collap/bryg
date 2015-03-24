Instantiation
=============

Templates
---------
A template **in the same environment** can be instantiated according to the following example:

.. code-block:: bryg

  val home = @Page('Home')

The parentheses are *mandatory*. Note that ``@Page`` is also the name of the type of ``home`` in this case.

The constructor function expects arguments for all fields of the instantiated template, in the order of their declaration. You can change the order by specifying the exact name of each parameter, which is useful if you want to omit fields with default values.

.. todo::
  Specify the exact rules regarding default fields and template constructors. Something like this should also be possible: ``@Page('Home', renderFooter = true)``, which should be unambiguous if the fields that takes the 'Home' argument is declared before any fields with default values.


Closures
--------
A closure may be instantiated with the following syntax:

.. code-block:: bryg

  val format = \(int x)
    span x

Note that a block or statement follows the parameter list.

If the closure has no parameters or the parameter types and names can be inferred from the context, the parameter list may be omitted. If only the types can be inferred, the names of the parameters can be specified without the respective types.


Java Types
----------
Instantiating Java classes is a feature planned in the future, but is not considered for v0.5.
