package com.devsmart.zookeeper

class CompileTemplateBuilder {

    enum Variants {
        exe,
        sharedlib,
        debug,
        release
    }

    private List<CompileTemplate> mList
    private CompileTemplate mBase = new CompileTemplate()
    private CompileTemplate mDebugVariant
    private CompileTemplate mReleaseVariant
    private CompileTemplate mExeVariant
    private CompileTemplate mSharedLibVariant

    CompileTemplateBuilder(List<CompileTemplate> list) {
        mList = list
    }

    def debug(Closure cl) {
        mDebugVariant = new CompileTemplate()

        cl.resolveStrategy = Closure.DELEGATE_ONLY
        cl.delegate = mDebugVariant
        cl()
    }

    def release(Closure cl) {
        mReleaseVariant = new CompileTemplate()

        cl.resolveStrategy = Closure.DELEGATE_ONLY
        cl.delegate = mReleaseVariant
        cl()
    }

    def exe(Closure cl) {
        mExeVariant = new CompileTemplate()

        cl.resolveStrategy = Closure.DELEGATE_ONLY
        cl.delegate = mExeVariant
        cl()
    }

    def sharedlib(Closure cl) {
        mSharedLibVariant = new CompileTemplate()

        cl.resolveStrategy = Closure.DELEGATE_ONLY
        cl.delegate = mSharedLibVariant
        cl()
    }

    CompileTemplate buildTemplate(Variants... variants) {
        CompileTemplate retval = new CompileTemplate(mBase, null)
        for(int i=variants.length-1;i>=0;i--) {
            switch(variants[i]){
                case Variants.exe:
                    if(mExeVariant != null) {
                        retval = new CompileTemplate(mExeVariant, retval)
                    }
                    break

                case Variants.sharedlib:
                    if(mSharedLibVariant != null) {
                        retval = new CompileTemplate(mSharedLibVariant, retval)
                    }
                    break

                case Variants.debug:
                    if(mDebugVariant != null) {
                        retval = new CompileTemplate(mDebugVariant, retval)
                    }
                    break

                case Variants.release:
                    if(mReleaseVariant != null) {
                        retval = new CompileTemplate(mReleaseVariant, retval)
                    }
                    break
            }
        }

        return retval

    }

    static def prefix(String pre, List l) {
        return { l.collect({"$pre$it"}) }
    }

    static def prefix(String pre, Closure<List> cl) {
        return { CompileTarget t ->
            List l = cl(t)
            l.collect({"$pre$it"})
        }
    }

    static CompileTemplateBuilder make(Closure cl) {
        CompileTemplateBuilder builder = new CompileTemplateBuilder()
        Closure code = cl.rehydrate(builder.mBase, builder, this)
        code()

        return builder
    }

}