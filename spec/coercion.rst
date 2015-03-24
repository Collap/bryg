Coercion
========

Coercion implicitly converts a value to a different expected type. Coercion can only happen between certain types. Coercion rules in Bryg mostly follow Java rules.


Primitive Types
---------------
Boolean and character values are not subject to coercion. Byte and short count as integers.

* If integer types differ, the values are promoted to long if at least one type is long, otherwise to int.
* If floating point types differ, the values are promoted to double.
* A floating point value and an integer value are handled according to these rules:
* Long values are always promoted to double values. If the floating point value is a float, it is promoted to double as well.
* If the floating point value is a float, the integer value is promoted to float. Otherwise, both values are promoted to double.

Boxing and Unboxing
~~~~~~~~~~~~~~~~~~~
When an object is expected, a primitive value is automatically boxed into the corresponding object type.

When a primitive value is expected and a boxed primitive is supplied, the object is automatically unboxed. For example, input parameters are automatically unboxed.

Boxing and unboxing happens in cases where coercion is currently supported.


Class Types
-----------
An object can be coerced to a superclass or (super-)interface type.

Currently, this type of coercion is only supported in method calls.
