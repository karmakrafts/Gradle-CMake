# Gradle CMake
This plugin allows to configure and build using CMake. 

This plugin should work as documented, but is in an early development phase. 
If you have requests or find bugs, please create an issue.

## Prerequisites

* `CMake` installed on the system. Available [here](https://www.cmake.org).

## To apply the plugin:

```groovy
buildscript {
    repositories {
        maven { url = 'https://git.karmakrafts.dev/api/v4/projects/177/packages/maven' }
    }
    dependencies {
        classpath group: 'io.karma.gradlecm', name: 'gradle-cmake', version: '1.1.0.23'
    }
}

apply plugin: "io.karma.gradlecm.gradle-cmake"
```

and configure by:

```groovy
cmake {
    // ---------- Shared Parameters
    
    // Optional configration to path of cmake. Not required if cmake is on the path.
    executable = '/my/path/to/cmake'
    // Optional working folder. default is ./build/cmake
    workingFolder = file("$buildDir/cmake")
    // Optional source folder. This is where the main CMakeLists.txt file resides. Default is ./src/main/cpp
    sourceFolder = file("$projectDir/src/main/cpp")
    // Select a generator (optional, otherwise cmake's default generator is used)
    generator = 'Visual Studio 17 2022' // For MSVC
    // generator = 'Unix Makefiles' // For Clang/GCC on Linux to properly use multi-threading
    
    // ---------- cmakeConfigure Parameters
    
    // Optional install prefix. By default, install prefix is empty.
    installPrefix = "${System.properties['user.home']}"
    // Set a platform for generators that support it (usually Visual Studio)
    platform = 'x64'
    // Set a toolset generators that support it (usually only Visual Studio)
    toolset = 'v143'
    // Optionally set to build static libs
    buildStaticLibs = true
    // Optionally set to build shared libs
    buildSharedLibs = true
    // Define arbitrary CMake parameters. The below adds -Dtest=hello to cmake command line.
    defs.test = 'hello'
    // Define arbitrary temporary environment variables for the build
    env.SOME_ENV_VAR = 'TestingTesting'
    
    // ---------- cmakeBuild Parameters
    
    // Optional configuration to build
    buildConfig = 'Release'
    // Optional build target
    buildTarget = 'install'
    // Optional build clean. if set to true, calls cmake --build with --clean-first
    buildClean = false
}
```

## Auto-created tasks

* **cmakeClean**: Cleans the workingFolder.

* **cmakeConfigure**: Calls CMake to generate your build scripts in the folder selected by workingFolder.

* **cmakeBuild**: Calls CMake --build in the folder selected by workingFolder to actually build.

* **cmakeGenerators**: Trys to list the generators available on the current platform by parsing `cmake --help`'s output.

## Examples

clean, configure and build:

```bash
./gradlew cmakeClean cmakeConfigure cmakebBuild
```

if you have assemble and clean tasks in your gradle project already you can also use:
	
```bash
assemble.dependsOn cmakeBuild
cmakeBuild.dependsOn cmakeConfigure
clean.dependsOn cmakeClean
```

and just call

```bash
./gradlew clean assemble
```

If you want to get the output of cmake, add -i to your gradle call, for example:
	
```bash
./gradlew cmakeConfigure -i
```

## Custom tasks

You can create custom tasks the following way:

```groovy
task configureFoo(type: io.karma.gradlecm.CMakeConfigureTask) {
    sourceFolder = file("$projectDir/src/main/cpp/foo")
    workingFolder = file("$buildDir/cmake/foo")
    // ..other parameters you need, see above, except the ones listed under cmakeBuild Parameters
}

task buildFoo(type: io.karma.gradlecm.CMakeBuildTask) {
    workingFolder = file("$buildDir/cmake/foo")
    // ..other parameters you need, see above, except the ones listed under cmakeConfigure parameters
}

buildFoo.dependsOn configureFoo // optional --- make sure its configured when you run the build task
```

### Custom tasks using main configuration

You can also "import" the settings you've made in the main configuration "cmake" using the 'configureFromProject()' call:

```groovy
cmake {
    executable = '/my/path/to/cmake'
    workingFolder = file("$buildDir/cmake")
    sourceFolder = file("$projectDir/src/main/cpp")
    installPrefix = "${System.properties['user.home']}"
    generator = 'Visual Studio 15 2017'
    platform = 'x64'
}

task cmakeConfigureX86(type: io.karma.gradlecm.CMakeConfigureTask) {
    // Uses everything in the cmake { ... } section.
    configureFromProject()
    // Overwrite target platform
    platform = 'x86'
    // Set a different working folder to not collide with default task
    workingFolder = file("$buildDir/cmake_x86")
}

task cmakeBuildX86(type: io.karma.gradlecm.CMakeBuildTask) {
    configureFromProject()
    // Uses everything in the cmake { ... } section.
    workingFolder = file("$buildDir/cmake_x86")
}

cmakeBuildX86.dependsOn cmakeConfigureX86
```

## License

All these plugins are licensed under the Apache License, Version 2.0 with no warranty (expressed or implied) for any purpose.
