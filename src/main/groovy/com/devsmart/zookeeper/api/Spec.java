package com.devsmart.zookeeper.api;

/**
 * Represents some predicate against objects of type T.
 *
 * @param <T> The target type for this Spec
 */
public interface Spec<T> {
    boolean isSatisfiedBy(T element);
}