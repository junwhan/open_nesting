package org.deuce.transform;

import static java.lang.annotation.RetentionPolicy.CLASS;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

/*
 * This Annotation allows user to make a method immune to transformation
 */
@Retention(CLASS)
@Target(ElementType.METHOD)
public @interface Resolute
{
}