package org.deuce.transform;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.CLASS;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/*
 * This annotation Allows to make whole class immune to instrumentation
 */
@Target(TYPE)
@Retention(CLASS)
public @interface Exclude {

}
