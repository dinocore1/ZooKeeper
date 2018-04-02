package com.devsmart.zookeeper.file;

import com.devsmart.zookeeper.DeferredUtil;
import com.devsmart.zookeeper.api.FileCollection;
import com.devsmart.zookeeper.tasks.BasicTask;
import com.google.common.collect.Iterables;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.Callable;

public class DefaultFileCollectionResolveContext implements FileCollectionResolveContext {

    private LinkedList<Object> mQueue = new LinkedList<Object>();
    private PathToFileResolver mFileResolver;

    public DefaultFileCollectionResolveContext(PathToFileResolver fileResolver) {
        mFileResolver = fileResolver;

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
        List<FileCollection> result = new ArrayList<FileCollection>();

        while(!mQueue.isEmpty()) {
            Object element = mQueue.poll();
            if(element instanceof Callable) {
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
                result.add(new FileListAdapter(mFileResolver.resolve(element)));
            }
        }


        return result;
    }
}
