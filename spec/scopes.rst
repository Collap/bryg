Scopes
======

A scope determines the existence of variable and function declarations. If a name is reserved in two scopes, the ambiguously named variable declared in the scope closest to the current scope is chosen. For example, a variable declared in a local scope is always preferred over a global variable. Fields also overshadow global variables, since the template scope is below the global scope. If a function and a variable with the same name are defined in the same scope, the variable is preferred.


Global Scope
------------
The global scope exists in the whole environment, hence across all templates. Variables and functions from modules are declared here.


Template Scope
--------------
The template scope exists across the whole template and includes its fields and fragments. The parent of a template scope is the global scope.


Local Scopes
------------
Local scopes range across blocks. Each block defines a new local scope, with the local scope the block was placed in as its parent. The exception is the local scope that corresponds to a fragment: Its parent is the template scope.
