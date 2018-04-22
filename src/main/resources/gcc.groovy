
import com.devsmart.zookeeper.plugins.*
import com.devsmart.zookeeper.Platform

def installDir = 'C:\\Users\\pauls\\.zookeeper\\toolchains\\mingw64\\bin'

def debugCPPSettings = new CompileSettings()
debugCPPSettings.flags.addAll(['-g', '-O0'])
debugCPPSettings.env.put('Path', "${installDir};\$(Path)")

def debugCSettings = new CompileSettings()
debugCSettings.flags.addAll(['-g', '-O0'])
debugCSettings.env.put('Path', "${installDir};\$(Path)")

def staticLibDebug = new GCCStaticLibVisitor()
staticLibDebug.platform = Platform.getNativePlatform()
staticLibDebug.variant = 'debug'
staticLibDebug.cppCmd = "${installDir}\\g++"
staticLibDebug.cppSettings = debugCPPSettings
staticLibDebug.cCmd = 'gcc'
staticLibDebug.cSettings = debugCSettings
staticLibDebug.linkCmd = 'ar'

project.zooKeeper.projectVisitors.add(staticLibDebug)

//////// Shared Libs ///////
debugCPPSettings = new CompileSettings()
debugCPPSettings.flags.addAll(['-g', '-O0', '-fPIC'])
debugCPPSettings.env.put('Path', "${installDir};\$(Path)")

debugCSettings = new CompileSettings()
debugCSettings.flags.addAll(['-g', '-O0', '-fPIC'])
debugCSettings.env.put('Path', "${installDir};\$(Path)")

def sharedLibDebug = new GCCSharedLibVisitor()
sharedLibDebug.platform = Platform.getNativePlatform()
sharedLibDebug.variant = 'debug'
sharedLibDebug.cppCmd = "${installDir}\\g++"
sharedLibDebug.cppSettings = debugCPPSettings
sharedLibDebug.cCmd = 'gcc'
sharedLibDebug.cSettings = debugCSettings
sharedLibDebug.linkCmd = 'gcc'

project.zooKeeper.projectVisitors.add(sharedLibDebug)

////// Exe ///////
debugCPPSettings = new CompileSettings()
debugCPPSettings.flags.addAll(['-g', '-O0'])
debugCPPSettings.env.put('Path', "${installDir};\$(Path)")

debugCSettings = new CompileSettings()
debugCSettings.flags.addAll(['-g', '-O0'])
debugCSettings.env.put('Path', "${installDir};\$(Path)")

def debugLinkSettings = new CompileSettings()
debugLinkSettings.env.put('Path', "${installDir};\$(Path)")

def exeDebug = new GCCExeVisitor()
exeDebug.platform = Platform.getNativePlatform()
exeDebug.filenameExtendtion = '.exe'
exeDebug.variant = 'debug'
exeDebug.cppCmd = "${installDir}\\g++"
exeDebug.cppSettings = debugCPPSettings
exeDebug.cCmd = "${installDir}\\gcc"
exeDebug.cSettings = debugCSettings
exeDebug.linkCmd = "${installDir}\\g++"
exeDebug.linkSettings = debugLinkSettings

project.zooKeeper.projectVisitors.add(exeDebug)
