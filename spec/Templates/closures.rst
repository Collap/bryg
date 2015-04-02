Closures
========

Closures are templates with a single default, unnamed fragment.

The closure captures variables declared outside the closure as fields. Captured variables are immutable to discourage changing them, which would have no effect outside the closure. Objects are not cloned, so changing an object’s state in the closure will have effect on the same object outside the closure, since only the reference is copied.


Closure Types
-------------
The type of every closure is `Closure`, with a **mandatory** list of all parameters:

.. code-block:: bryg

  Closure<String name, int count> // A closure with 2 parameters.
  Closure<>                       // A closure with 0 parameters.

The parameters may optionally be named, but note that these names do not need to be equal to the parameter names of each *instance* of the closure. They are merely used to allow named arguments, so if you do not use these, specifying the types is sufficient, although adding names provides an additional form of documentation.

If the programmer does not wish to specify the parameters, they can use the following syntax:

.. code-block:: bryg

  Closure<...>

In this case, all arguments must be named when calling the closure and the compiler can not perform type checking or coercion, due to a lack of sufficient information. It is not recommended to use this feature.

.. todo::
  Are there cases in which it is advantageous to not specify the parameter types of a closure?
