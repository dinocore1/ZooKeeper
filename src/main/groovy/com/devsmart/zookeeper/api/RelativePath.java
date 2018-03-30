package com.devsmart.zookeeper.api;

import java.io.File;
import java.nio.CharBuffer;
import java.util.Arrays;

public class RelativePath implements CharSequence, Comparable<RelativePath> {

    public static final RelativePath EMPTY_ROOT = new RelativePath(false);

    private final boolean mIsFile;
    private final String[] mSegments;

    public RelativePath(boolean isFile, String... segments) {
        mIsFile = isFile;
        mSegments = segments;
    }

    private static void copySegments(String[] target, String[] source) {
        copySegments(target, source, target.length);
    }

    private static void copySegments(String[] target, String[] source, int length) {
        // No String instance interning is needed since Strings are from other
        // RelativePath instances which contain only interned String instances
        System.arraycopy(source, 0, target, 0, length);
    }

    public File getFile(File baseDir) {
        return new File(baseDir, getPathString());
    }

    public String getLastName() {
        if (mSegments.length > 0) {
            return mSegments[mSegments.length - 1];
        } else {
            return null;
        }
    }

    public String getPathString() {
        if (mSegments.length == 0) {
            return "";
        }
        StringBuilder path = new StringBuilder(256);
        for (int i = 0, len = mSegments.length; i < len; i++) {
            if (i != 0) {
                path.append('/');
            }
            path.append(mSegments[i]);
        }
        return path.toString();
    }

    @Override
    public int length() {
        if (mSegments.length == 0) {
            return 0;
        }
        int length = mSegments.length - 1;
        for (String segment : mSegments) {
            length += segment.length();
        }
        return length;
    }

    @Override
    public char charAt(int index) {
        if (index < 0) {
            throw new IndexOutOfBoundsException(String.valueOf(index));
        }
        int remaining = index;
        int nextSegment = 0;
        while (nextSegment < mSegments.length) {
            String segment = mSegments[nextSegment];
            int length = segment.length();
            if (remaining < length) {
                return segment.charAt(remaining);
            } else if (remaining == length) {
                return '/';
            } else {
                remaining -= length + 1;
                nextSegment++;
            }
        }
        throw new IndexOutOfBoundsException(String.valueOf(index));
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return CharBuffer.wrap(this, start, end);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        RelativePath that = (RelativePath) o;

        if (mIsFile != that.mIsFile) {
            return false;
        }
        if (!Arrays.equals(mSegments, that.mSegments)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = mIsFile ? 1 : 0;
        result = 31 * result + Arrays.hashCode(mSegments);
        return result;
    }

    @Override
    public String toString() {
        return getPathString();
    }

    @Override
    public int compareTo(RelativePath o) {
        int len1 = mSegments.length;
        int len2 = o.mSegments.length;

        if (len1 != len2) {
            return len1 - len2;
        }

        int lim = Math.min(len1, len2);
        String v1[] = mSegments;
        String v2[] = o.mSegments;

        int k = 0;
        while (k < lim) {
            String c1 = v1[k];
            String c2 = v2[k];
            int compareResult = c1 == c2 ? 0 : c1.compareTo(c2);
            if (compareResult != 0) {
                return compareResult;
            }
            k++;
        }
        return 0;
    }

    /**
     * Returns the parent of this path.
     *
     * @return The parent of this path, or null if this is the root path.
     */
    public RelativePath getParent() {
        switch (mSegments.length) {
            case 0:
                return null;
            case 1:
                return EMPTY_ROOT;
            default:
                String[] parentSegments = new String[mSegments.length - 1];
                copySegments(parentSegments, mSegments);
                return new RelativePath(false, parentSegments);
        }
    }
}
