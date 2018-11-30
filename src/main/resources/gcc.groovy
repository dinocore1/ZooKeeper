
import com.devsmart.zookeeper.plugins.*
import com.devsmart.zookeeper.Platform

def installDir = 'C:\\Users\\pauls\\.zookeeper\\toolchains\\mingw32\\bin'

def debugLinkSettings = new CompileSettings()
//debugLinkSettings.flags.addAll(['-static-libstdc++'])
debugLinkSettings.env.put('Path', "${installDir};\$(Path)")

def debugCPPSettings = new CompileSettings()
debugCPPSettings.flags.addAll(['-g', '-O0', '-std=c++11'])
debugCPPSettings.env.put('Path', "${installDir};\$(Path)")

def debugCSettings = new CompileSettings()
debugCSettings.flags.addAll(['-g', '-O0'])
debugCSettings.env.put('Path', "${installDir};\$(Path)")

def staticLibDebug = new GCCStaticLibVisitor()
staticLibDebug.platform = Platform.parse('win-x86')
staticLibDebug.filenameExtendtion = '.dll.a'
staticLibDebug.variant = 'debug'
staticLibDebug.cppCmd = "${installDir}\\g++"
staticLibDebug.cppSettings = debugCPPSettings
staticLibDebug.cCmd = "${installDir}\\gcc"
staticLibDebug.cSettings = debugCSettings
staticLibDebug.linkCmd = 'ar'
staticLibDebug.linkSettings = debugLinkSettings

project.zooKeeper.projectVisitors.add(staticLibDebug)

//////// Shared Libs ///////
debugCPPSettings = new CompileSettings()
debugCPPSettings.flags.addAll(['-g', '-O0', '-fPIC', '-std=c++11'])
debugCPPSettings.env.put('Path', "${installDir};\$(Path)")

debugCSettings = new CompileSettings()
debugCSettings.flags.addAll(['-g', '-O0', '-fPIC'])
debugCSettings.env.put('Path', "${installDir};\$(Path)")

def sharedLibDebug = new GCCSharedLibVisitor()
sharedLibDebug.platform = Platform.parse('win-x86')
sharedLibDebug.filenameExtendtion = '.dll'
sharedLibDebug.variant = 'debug'
sharedLibDebug.cppCmd = "${installDir}\\g++"
sharedLibDebug.cppSettings = debugCPPSettings
sharedLibDebug.cCmd = "${installDir}\\gcc"
sharedLibDebug.cSettings = debugCSettings
sharedLibDebug.linkCmd = "${installDir}\\g++"
sharedLibDebug.linkSettings = debugLinkSettings

project.zooKeeper.projectVisitors.add(sharedLibDebug)

////// Exe ///////
debugCPPSettings = new CompileSettings()
debugCPPSettings.flags.addAll(['-g', '-O0', '-std=c++11'])
debugCPPSettings.env.put('Path', "${installDir};\$(Path)")

debugCSettings = new CompileSettings()
debugCSettings.flags.addAll(['-g', '-O0'])
debugCSettings.env.put('Path', "${installDir};\$(Path)")

def exeDebug = new GCCExeVisitor()
exeDebug.platform = Platform.parse('win-x86')
exeDebug.filenameExtendtion = '.exe'
exeDebug.variant = 'debug'
exeDebug.cppCmd = "${installDir}\\g++"
exeDebug.cppSettings = debugCPPSettings
exeDebug.cCmd = "${installDir}\\gcc"
exeDebug.cSettings = debugCSettings
exeDebug.linkCmd = "${installDir}\\g++"
exeDebug.linkSettings = debugLinkSettings

project.zooKeeper.projectVisitors.add(exeDebug)
