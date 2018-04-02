package com.devsmart.zookeeper.file;

import com.devsmart.zookeeper.DeferredUtil;
import com.devsmart.zookeeper.api.FileCollection;
import com.devsmart.zookeeper.api.FileTree;
import com.devsmart.zookeeper.tasks.BasicTask;
import com.google.common.collect.Iterables;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.Callable;

public class DefaultFileCollectionResolveContext implements FileCollectionResolveContext {

    private final FileCollectionConverter mFileCollectionConverter;
    private final FileTreeConverter mFileTreeConverter;
    private LinkedList<Object> mQueue = new LinkedList<Object>();
    private PathToFileResolver mFileResolver;

    private interface Converter<T> {
        void convertInto(Object element, Collection<? super T> result, PathToFileResolver resolver);
    }

    private static class FileCollectionConverter implements Converter<FileCollection> {

        @Override
        public void convertInto(Object element, Collection<? super FileCollection> result, PathToFileResolver resolver) {
            if(element instanceof FileCollection) {
                result.add((FileCollection)element);
            } else if(element instanceof MinFileTree) {
                result.add(new FileTreeAdapter((MinFileTree) element));
            } else {
                result.add(new FileListAdapter(resolver.resolve(element)));
            }
        }
    }

    private static class FileTreeConverter implements Converter<FileTree> {

        @Override
        public void convertInto(Object element, Collection<? super FileTree> result, PathToFileResolver resolver) {
            if(element instanceof FileTree){
                result.add((FileTree) element);
            } else if(element instanceof MinFileTree) {
                result.add(new FileTreeAdapter((MinFileTree) element));
            }
        }
    }

    public DefaultFileCollectionResolveContext(PathToFileResolver fileResolver) {
        mFileResolver = fileResolver;

        mFileCollectionConverter = new FileCollectionConverter();
        mFileTreeConverter = new FileTreeConverter();


    }

    @Override
    public FileCollectionResolveContext add(Object element) {
        mQueue.add(element);
        return this;
    }

    @Override
    public FileCollectionResolveContext push(PathToFileResolver fileResolver) {
        mFileResolver = fileResolver;
        return this;
    }

    public List<FileCollection> resolveAsFileCollections() {
       return doResolve(mFileCollectionConverter);
    }

    public List<FileTree> resolveAsFileTrees() {
        return doResolve(mFileTreeConverter);
    }

    private <T> List<T> doResolve(Converter<? extends T> converter) {
        List<T> result = new ArrayList<T>();

        while(!mQueue.isEmpty()) {
            Object element = mQueue.poll();
            if(element instanceof FileCollection){
                converter.convertInto(element, result, mFileResolver);
            } else if(element instanceof Callable) {
                Callable callable = (Callable) element;
                Object callableResult = DeferredUtil.uncheckedCall(callable);
                if (callableResult != null) {
                    mQueue.add(0, callableResult);
                }
            } else if (element instanceof BasicTask) {
                BasicTask task = (BasicTask) element;
                mQueue.add(0, task.getOutput());
            } else if (element instanceof Iterable) {
                Iterables.addAll(mQueue.subList(0, 0), (Iterable) element);
            } else {
                converter.convertInto(element, result, mFileResolver);
            }
        }

        return result;
    }
}
