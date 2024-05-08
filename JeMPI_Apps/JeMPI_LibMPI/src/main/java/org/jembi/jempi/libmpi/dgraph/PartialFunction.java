package org.jembi.jempi.libmpi.dgraph;

/**
 * The interface Partial function.
 *
 * @param <T> the type parameter
 * @param <R> the type parameter
 */
@FunctionalInterface
interface PartialFunction<T, R> {
   /**
    * Apply r.
    *
    * @param t the t
    * @return the r
    */
   R apply(T t);
}
