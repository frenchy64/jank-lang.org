Title: jank dependency management
Date: Jan 27, 2025
Author: Ambrose Bonnaire-Sergeant
Author-Url: https://ambrosebs.com
Description: An overview of how jank dependencies work.

Much like the Java classpath, jank has a module path from which we load code from.
They are similar enough that we can cleanly leverage most of the existing work behind Maven, Leiningen,
and Clojure CLI for jank dependency management.

# Setting the module path

The [`jank`](https://github.com/jank-lang/jank/blob/main/compiler+runtime/doc/install.md)
command is used to develop jank programs. The `--module-path` option
sets the module path.

```
$ jank --help
...
          --module-path TEXT  A : separated list of directories, JAR files, and ZIP files to
                              search for modules
...
```

Whichever `jank` subcommand we use will have those modules available.

```
$ cat src/jank_cli_test/core.jank
(ns jank-cli-test.core)
(defn -main [] (println "Hello, jank!"))

$ jank run-main jank-cli-test.core   # forgot to set module path
Exception: unable to find module: jank-cli-test.core

$ jank --module-path src run-main jank-cli-test.core
Hello, jank!
```

# Clojure source jank and cljc

Much like `.cljs` for ClojureScript and `.cljr` for ClojureCLR,
`.jank` is the extension for jank source files. Reader conditionals
are allowed in `.jank` files, but are most useful in `.cljc` files where
jank uses the `:jank` feature.

# Module to file mapping

Modules resolve to files on the module path similar to how Clojure namespaces
resolve to files on the classpath.
The following form

```
(require 'my-app.entry-point)
```

will load the first of the following files it finds on the module path:

- `my_app/entry_point.jank`
- `my_app/entry_point.cljc`
- `my_app/entry_point.o`
- `my_app/entry_point.cpp`

Object files from external dependencies are ignored and are only recognized
if the corresponding jank source file is locally present.

# C++ files

If a C++ file is a companion to a jank namespace `foo-bar.baz`, it should be 
named `foo_bar/baz_native.cpp` and export a function called `jank_load_foo_bar_baz_native()`.
To load the C++ code, the `foo-bar.baz` ns form should be:

```clojure
(ns foo-bar.baz
  (:require foo-bar.baz-native))
```

This association will become implicit in the future.

# Archives

Jar and ZIP files are treated like directories on the module path.
To package a jank library in a jar, add it to the same directory
you would when distributing a Clojure library.

```
$ jar -tf my-app.jar
...
my_app/entry_point.jank
...

$ jank --module-path my-app.jar run-main my-app.entry-point
... runs the app ...
```

# Leiningen

[lein-jank](https://github.com/jank-lang/jank/blob/main/lein-jank/README.md) 
links lein and jank using a plugin. `lein jank ...` commands automatically sets the module path
in the same way `lein ...` usually sets the JVM classpath.

```clojure
(defproject my-app "v0.0.1"
  :dependencies [... external jank deps ...]
  :plugins [[org.jank-lang/lein-jank "0.0.1-SNAPSHOT"]]
  :main my-app.core
  ...)
```

```
$ lein jank run
... runs your app ...
```

# Clojure CLI

```clojure
{:deps {jank-clojure-cli-test/subproject {:local/root "subproject"}}
 :paths ["src"]
 :aliases
 {:test {:extra-paths ["test"]}}}
```

