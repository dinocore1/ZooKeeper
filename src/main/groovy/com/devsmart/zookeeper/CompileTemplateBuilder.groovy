package com.devsmart.zookeeper

class CompileTemplateBuilder {

    private List<CompileTemplate> mList
    private CompileTemplate mBase = new CompileTemplate()
    private CompileTemplate mDebugVariant
    private CompileTemplate mReleaseVariant

    CompileTemplateBuilder(List<CompileTemplate> list) {
        mList = list
    }

    def debug(Closure cl) {
        mDebugVariant = new CompileTemplate(mBase)

        cl.resolveStrategy = Closure.DELEGATE_ONLY
        cl.delegate = mDebugVariant
        cl()
    }

    def release(Closure cl) {
        mReleaseVariant = new CompileTemplate(mBase)

        cl.resolveStrategy = Closure.DELEGATE_ONLY
        cl.delegate = mReleaseVariant
        cl()
    }

    CompileTemplate getDebugVariant() {
        return mDebugVariant
    }

    CompileTemplate getReleaseVarian() {
        return mReleaseVariant
    }

    CompileTemplate getDefaultTemplate() {
        if(mDebugVariant != null) {
            return mDebugVariant
        } else {
            return mBase
        }
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
