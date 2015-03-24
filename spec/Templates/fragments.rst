Fragments
=========

A fragment is a callable function of a template. You can define zero or more fragments per template:

.. code-block:: bryg

  frag heading
    h1: Hello Fragment

A fragment can specify input parameters:

.. code-block:: bryg

  frag heading (String name)
    h1: Hello \{name}

It should be clarified here that fragments **do not** return a value. The sole purpose of fragments is to generate text.


Default Fragments
-----------------
Default fragments can act as the entry point to a template, which is advantageous if the template fulfills one specific task. At most one fragment per template may be specified to be the ``default`` fragment:

.. code-block:: bryg

  // With a name
  default frag render
    : Hi!

  // Without a name
  default frag
    : Hi!

Above are two different alternatives to specify a default fragment, since its name is optional. This fragment can be called via a default function call, or via its optional name.


Parameters
----------
Fragment parameters must be declared with a type and an alphanumeric identifier:

.. code-block:: bryg

  frag greet (String name)

Default Values
~~~~~~~~~~~~~~
You can give a parameter a default value, which makes that parameter **optional**:

.. code-block:: bryg

  frag greet (String name = 'World')

Optional parameters can be omitted when calling the fragment, so you should try to specify them after any mandatory parameters. This also means that argument predicates are only allowed with optional parameters.

Nullable
~~~~~~~~
Only ``nullable`` parameters are allowed to be ``null``.

.. code-block:: bryg

  frag profile (Person person, nullable Closure<> content)
    // ...
    if content !== null
      content()

Primitive parameters (e.g. ``int`` or ``float``) may not be nullable, since they can not be null.

Implicit
~~~~~~~~
A **single** closure parameter may be declared ``implicit``, which is required to pass closures directly with a function call. An example is given as follows:

.. code-block:: bryg

  frag greet (implicit Closure who)
    b: Hello, \{who()}!

  frag greetEveryone
    greet \
      : World
