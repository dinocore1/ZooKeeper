package com.devsmart.zookeeper.plugins;

import com.devsmart.zookeeper.tasks.CompileChildProcessTask;

public interface CompileProcessModifier {

    void apply(CompileChildProcessTask ctx);
}
