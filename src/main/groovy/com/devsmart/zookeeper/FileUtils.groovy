package com.devsmart.zookeeper

import com.google.common.base.Predicate
import com.google.common.collect.Iterables
import com.google.common.collect.Iterators

import java.nio.file.Path
import java.nio.file.Paths
import java.util.regex.Matcher
import java.util.regex.Pattern

class FileUtils {

    private static Pattern REGEX_WILDCARD = Pattern.compile('\\*')

    private static Predicate<File> createFilenamePatternPredicate(Pattern p) {
        return new Predicate<File>() {
            @Override
            boolean apply(File input) {
                final String name = input.getName()
                return p.matcher(name).find()
            }
        }
    }

    static FileCollection from(File rootDir, Pattern filenamePattern) {
        return new FileCollection() {
            @Override
            Iterator<File> iterator() {
                return Iterables.filter(
                        Arrays.asList(rootDir.listFiles()),
                        createFilenamePatternPredicate(filenamePattern)).iterator()
            }
        }
    }

    static FileCollection from(File f) {
        return new FileCollection() {
            @Override
            Iterator<File> iterator() {
                return Iterators.singletonIterator(f)
            }
        }
    }

    static FileCollection from(String str) {
        Path p = Paths.get(str)
        String fileName = p.fileName.toString()

        Matcher m = REGEX_WILDCARD.matcher(fileName)
        if(m.find()) {
            Pattern fileNamePattern = Pattern.compile(m.replaceAll('[a-zA-Z0-9_]*'))
            return from(p.parent.toFile(), fileNamePattern)
        } else {
            return from(p.toFile())
        }
    }

    static FileCollection from(Object path) {
        if(path instanceof File) {
            return from((File) path)
        } else if(path instanceof String) {
            return from((String) path)
        }

    }

    static FileCollection from(Object... paths) {

        FileCollection[] collections = new FileCollection[paths.length]
        for(int i=0;i<collections.length;i++) {
            collections[i] = from(paths[i])
        }

        Iterable<File> concat = Iterables.concat(collections)

        return new FileCollection() {
            @Override
            Iterator<File> iterator() {
                return concat.iterator()
            }
        }
    }
}
