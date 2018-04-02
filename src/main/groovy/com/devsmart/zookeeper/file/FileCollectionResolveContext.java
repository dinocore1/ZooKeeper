package com.devsmart.zookeeper.file;

public interface FileCollectionResolveContext {
    /**
     * Adds the given element to be resolved. Handles the following types:
     *
     * <ul>
     *     <li>{@link Iterable} or array - elements are recursively resolved.
     *     <li>{@link groovy.lang.Closure} - return value is recursively resolved, if not null.
     *     <li>{@link java.util.concurrent.Callable} - return value is recursively resolved, if not null.
     *     <li>{@link com.devsmart.zookeeper.api.FileCollection} - resolved as is.
     *     <li>{@link com.devsmart.zookeeper.tasks.BasicTask} - resolved to task.outputs.files
     *
     *     <li>Everything else - resolved to a File and wrapped in a singleton {@link com.devsmart.zookeeper.api.FileCollection}.
     * </ul>
     *
     * Generally, the result of resolution is a composite {@link com.devsmart.zookeeper.api.FileCollection} which contains the union of all files and dependencies add to this context.
     *
     * @param element The element to add.
     * @return this
     */
    FileCollectionResolveContext add(Object element);

    /**
     * Adds a nested context which resolves elements using the given resolver. Any element added to the returned context will be added to this context. Those elements
     * which need to be resolved using a file resolver will use the provided resolver, instead of the default used by this context.
     */
    FileCollectionResolveContext push(PathToFileResolver fileResolver);

}