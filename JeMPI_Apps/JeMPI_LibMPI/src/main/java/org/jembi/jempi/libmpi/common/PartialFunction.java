package org.jembi.jempi.libmpi.common;

/**
 * The interface Partial function.
 *
 * @param <T> the type parameter
 * @param <R> the type parameter
 */
@FunctionalInterface
public interface PartialFunction<T, R> {
   /**
    * Apply r.
    *
    * @param t the t
    * @return the r
    */
   R apply(T t);
}
