package com.devsmart.zookeeper;

import com.google.common.base.Joiner;

public class TemplateKey {

    public final Platform platform;
    public final String language;
    public final String stage;

    public TemplateKey(Platform target, String language, String stage) {
        this.platform = target;
        this.language = language;
        this.stage = stage;
    }

    @Override
    public int hashCode() {
        return platform.hashCode() ^ stage.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if(o == null || o.getClass() != TemplateKey.class) {
            return false;
        }

        TemplateKey other = (TemplateKey) o;

        boolean retval = platform.equals(other.platform);
        retval &= language.equals(other.language);
        retval &= stage.equals(other.stage);


        return retval;
    }

    @Override
    public String toString() {
        return Joiner.on(" ").join(platform, language, stage);
    }
}
