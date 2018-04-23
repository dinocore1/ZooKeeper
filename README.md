



windows:
.dll is dynamic lib should be installed to /bin
.lib file is static lib should be installed to /lib

mingw
.dll.a is refered to as in "import library" It is really a static library, but used by the linker to describe load-time linking to a dll. should be installed to /lib


ZooKeeper MingW:
.dll and .dll.a installed to /shared
.lib installed to /static

ZooKeeper Window (MSVC)
.dll and import libs (.lib) installed to /shared
.lib (static libs) installed to /static

ZooKeeper *nix:
.so installed to /shared
.a installed to /static

