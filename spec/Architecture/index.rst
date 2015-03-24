Architecture
============

.. toctree::
  :hidden:
  :titlesonly:

  modules


Environments
~~~~~~~~~~~~
An environment is an object that is responsible for loading, compiling and managing template classes. Multiple environments can be used for different tasks, so there does not have to be, and should not be, only one environment in a complex application.

A newly created environment is in a setup state, where a number of components can be registered with the environment:

* At most one parent may be registered, which is used when the current environment can't find a requested template. For example, this can be used to have a "master environment" that defines common templates used by child environments, which are separated because they handle different areas.
* Modules can be registered, which are then used by the compiler to discover global variables and functions.
* Source loaders can be registered, which find the source to a given template name. The first source loader that finds a source to a name returns it, which means that the first registered source loader takes priority over the second, and so on.
* One or more class resolvers must be registered, which find Java classes belonging to simple type names. For example, a ``String`` would be resolved to ``java.lang.String``.

After the setup is finished, no additional components may be registered.

.. todo::
  In the context of class resolvers, we should introduce an import statement anyways, since there may be rare name conflicts which must be resolved on a per-template basis, which is not possible with the current system.
