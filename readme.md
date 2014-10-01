# Bryg

Bryg is a statically typed and JIT-compiled template language for the JVM.

It is developed with these goals:
- Templates should be **intuitive**. HTML, on the other hand, is not. We reduced the amount of redundancy to a minimum.
- Templates should be **fast**. While our compiler is not as advanced as Java's compiler, it is guaranteed
  to beat other interpreted template engines in terms of speed. Instead of writing our own execution engine, we use
  the powerful JVM to our advantage.
- Templates should be **maintainable**. Wouldn't it be awesome if a compiler checked the types of operations before
  the template is run with different inputs, instead of relying on comments and duck typing? That's why bryg is
  statically typed.
  
**Note:** The language is currently in development, so existing features could and will probably be changed.


## Language Reference

You can find a language reference in the [wiki](https://github.com/Collap/bryg/wiki).


## Compilation

Bryg is built with gradle. Just type

    gradle build test install
    
into your favorite command line while being in the root folder of bryg. This command will build the bryg sources and test sources, execute the tests and install the jar to the local maven repository.

You need to compile the antlr4 sources manually to the folder gen with the package io.collap.bryg.parser.