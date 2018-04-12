
import com.devsmart.zookeeper.plugins.DefaultGCCCompiler
import com.devsmart.zookeeper.Platform
import com.devsmart.zookeeper.projectmodel.BuildableExecutable

import java.io.File;


def debugVariant = new DefaultGCCCompiler()
debugVariant.platform = Platform.getNativePlatform()
debugVariant.cmd = 'gcc'
debugVariant.variant = 'debug'
debugVariant.cflags = ['-g', '-O0']
debugVariant.includes = project.files('src', 'include')

project.zooKeeper.projectVisitors.add(debugVariant)
