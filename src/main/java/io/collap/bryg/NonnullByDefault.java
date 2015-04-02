package io.collap.bryg;

import javax.annotation.Nonnull;
import javax.annotation.meta.TypeQualifierDefault;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Apply this to a package, class or method to implicitly annotate every
 * variable with @Nonnull. This means that warnings are thrown when setting
 * a variable to null, unless the it is annotated with @Nullable.
 * Does not work recursively, so this should be specified in every
 * package-info.java file.
 */
@Documented
@Nonnull
@TypeQualifierDefault(
        {
                ElementType.ANNOTATION_TYPE,
                ElementType.CONSTRUCTOR,
                ElementType.FIELD,
                ElementType.LOCAL_VARIABLE,
                ElementType.METHOD,
                ElementType.PACKAGE,
                ElementType.PARAMETER,
                ElementType.TYPE
        })
@Retention(RetentionPolicy.RUNTIME)
public @interface NonnullByDefault {

}
