Templates
=========

.. toctree::
  :hidden:
  :titlesonly:

  directives
  fragments
  closures

A template is a group of categorically similar fragments, while a fragment renders text. As an example for a template, there could be a *Table* template that has multiple fragments that deal with rendering a HTML table from a list or other data structure.

The structure of a template is as follows, in this order:

* Zero or more directives.
* Zero or more variable declarations, which are called the *fields* of the template.
* Zero or more fragment definitions.


Names
-----
The name of a template is defined by the template's location according to the environment. Its name also constitutes a type name, which is prefixed with an '@' to distinguish a template type from normal Java types.

For example, the template `Page` has the type `@Page`. The constructor, which creates a new instance of the template, is called the same as the type.

If you want to refer to a template at a relative package path, you can use a dot right after the `@`. For example, we might have templates called `app.App`, `app.Test` and `app.example.Wall`. In `app.App`, the type names can be shortened to `@.Test` and `@.example.Wall`.


Template Fields
---------------
A template field is an immutable variable which is defined throughout the lifetime of a template. It is implicitly initialised by the template constructor when the template is instantiated.

Let's consider the following example:

.. code-block:: bryg

  String heading

  frag head
    title heading

  frag content (String text)
    h1 heading
    div text

The field `heading` is accessible to both fragment functions and stays the same during execution, because of its immutability.

Fields may also have default values:

.. code-block:: bryg

  boolean showName = true

.. todo::

  Allow mutable fields?

.. todo::

  Allow constants?


Template Constructors
---------------------
The template's constructor is automatically generated. Its parameters are declared in the order of the fields' declarations. It simply sets the template's fields to the values supplied via the parameters.

For example, consider a template with two fields: `String name` and `int age`. The generated constructor would look like the following Java code, semantically:

.. code-block:: java

  public ATemplate (String name, int age) {
      this.name = name;
      this.age = age;
  }
