
import com.devsmart.zookeeper.plugins.*
import com.devsmart.zookeeper.Platform

def debugCPPSettings = new CompileSettings()
debugCPPSettings.flags.addAll(['-g', '-O0'])

def debugCSettings = new CompileSettings()
debugCSettings.flags.addAll(['-g', '-O0'])

def staticLibDebug = new GCCStaticLibVisitor()
staticLibDebug.platform = Platform.getNativePlatform()
staticLibDebug.variant = 'debug'
staticLibDebug.cppCmd = 'g++'
staticLibDebug.cppSettings = debugCPPSettings
staticLibDebug.cCmd = 'gcc'
staticLibDebug.cSettings = debugCSettings
staticLibDebug.linkCmd = 'ar'

project.zooKeeper.projectVisitors.add(staticLibDebug)

//////// Shared Libs ///////
debugCPPSettings = new CompileSettings()
debugCPPSettings.flags.addAll(['-g', '-O0', '-fPIC'])

debugCSettings = new CompileSettings()
debugCSettings.flags.addAll(['-g', '-O0', '-fPIC'])

def sharedLibDebug = new GCCSharedLibVisitor()
sharedLibDebug.platform = Platform.getNativePlatform()
sharedLibDebug.variant = 'debug'
sharedLibDebug.cppCmd = 'g++'
sharedLibDebug.cppSettings = debugCPPSettings
sharedLibDebug.cCmd = 'gcc'
sharedLibDebug.cSettings = debugCSettings
sharedLibDebug.linkCmd = 'gcc'

project.zooKeeper.projectVisitors.add(sharedLibDebug)
