package com.devsmart.zookeeper.plugins;

import com.devsmart.zookeeper.tasks.CompileChildProcessTask;

public class SettingWrapper implements CompileProcessModifier {


    private final CompileSettings mSettings;

    public SettingWrapper(CompileSettings settings) {
        mSettings = settings;
    }

    @Override
    public void apply(CompileChildProcessTask ctx) {

        CompileContext compileSettings = ctx.getCompileContext();
        compileSettings.flags.addAll(mSettings.getFlags());
        compileSettings.includes.addAll(mSettings.getIncludes());
        compileSettings.staticLinkedLibs.addAll(mSettings.getStaticLinkedLibs());
        compileSettings.sharedLinkedLibs.addAll(mSettings.getSharedLinkedLibs());

    }
}
