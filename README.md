# Clojure Utilities

A general purpose hold-all utilities library.

Iteration one: REPL tools.

## Installation

Clone the repository to a directory of your choosing.

Set up your configuration (see next section) and run

`lein install`

from the root directory of the repo.

There may be some warnings, these can be ignored.
On successful installation you will see the message
'Installed jar and pom into local repo.'

For use in all projects modify your `~/.lein/profiles.clj` `:user` profile.
For use in a specific project only modify `project.clj` for that project.

In your chosen `profiles.clj` or `project.clj`, add or update
the following entries:

```
:dependencies [[unknown-unit/clojure-utils "0.1.0-SNAPSHOT"]]
:injections [(require 'unknown-unit.repl.core)
             (unknown-unit.repl.core/init)]
```

If there may be code in `:injections` which might throw an error,
wrap it in a `try...catch` to be able to continue to use the repl.

Any included functions may not be available in this case.

If you download an updated version of this library, change branches,
or make any code or configuration changes, you will have to reinstall
and restart your repl.

## Configuration

Config is found in config/repl.edn. Copy repl.example.edn to configure
your local installation. At this time you will most likely not need
to make any changes, unless you want to alias the library functions.

Namespaces to be automatically imported into a new namespace when using
`ns-/ns+` to switch namespaces are defined in

`{:namespaces {:user []}}`

The namespace utilities namespace is always included.

`clojure.repl` is included as a default. Defaults are recommended
but not required. Defaults may change from time to time in the example
file.

To import a namespace with an alias specify it as usual in the `:user`
namespaces list.

E.g., `:user [[my.project.core :as my-core]]`

To import a namespace referring only some functions, again nothing unusual.

E.g., `:user [[my.project.core :refer [my-main]]]`

`:as` and `:refer` can be combined as usual.

If an alias or refer is already defined, `ns-` will honor that definition.
If none is defined, e.g.

`[my.project.core]`

it is assumed all functions should be referred locally & this is rewritten as

`[my.project.core :refer :all]`

The utility functions (unknown-unit.repl.ns, unknown-unit.repl.capture, etc.) will generally be referred as local functions unless specified otherwise in the config.

## Usage

Start a repl inside any of your projects.

### Namespace functions

`(reload-ns)` will reload all namespaces from your project src and test
directories, as well as the `unknown-unit.repl.ns` namespace.

`(ns- <namespace>)` change to the requested namespace and import user
specified namespaces, with namespace separation or referred as local
functions (:refer :all).

`(ns+ <namespace>)` does the same as `ns-` except that all defined namespaces
are referred to local functions, whether `:as` or `:refer` were already declared.

### Macro functions

`(expand <macro-call>)` will produce pretty printed output of the macro expansion.

E.g.,

`(expand (and happy days))`
will produce

```
(clojure.core/let
 [and__3973__auto__ happy]
 (if and__3973__auto__ (clojure.core/and days) and__3973__auto__))
```

`(expand :all (and happy days))`
will produce

```
(let*
 [and__3973__auto__ happy]
 (if and__3973__auto__ days and__3973__auto__))
```

An optional first argument specifies the type of expansion.

Available options:

- :0 macroexpand
- :1 macroexpand-1
- :all clojure.walk/macroexpand-all

Leaving out the level option automatically selects level :1. Often this produces the same as level :0, but sometimes level :0 may expand more.
See the documentation for these functions for more details.

### Function capture

`(capture <expression>)` captures the given `def` or `defn` expression and returns it to be evaluated in the current namespace.

E.g., `capture defn airspeed-velocity [swallow] (:airspeed-velocity swallow))` will generate the function `airspeed-velicty` in the current namespace, and store the source code.

Errors:
- :op-not-supported
- :expression-already-exists

`(modify <expression>)` replaces an existing expression with a new version.

Errors:
- :op-not-supported
- :expression-not-found

`(local <name>)` imports the specified named expression into the current namespace.

`(local-multi [<name> & <names>])` imports the specified named expressions into the current namespace.

`local` and `local-multi` return no errors. Output is a list of found expressions. An empty list indicates none of the named expressions was found.

### System

The system is the heart of the session.
If auto-refresh is specified in the configuration, the system controls the reloading thread.

`(configure <key val> & <key val>)` configures allowed values inside the system.

Currently only :ns-refresh can be configured. This is best left to the core loading code.

`(start)` starts the system, including generating an auto-refresh thread when requested.

`(stop)` stops the system. Stopping the system runs all declared operations in the system's `:stop` vector. The `:stop` vector is then cleared. No other changes are made to the system.

`(reset)` stops the system via `(stop)`, clears any captured expressions, and re`(start)`s the system.

`(system)` returns the state of the current system. This can be examined to see if the system is running, what stop operations have been declared, and what expressions have been captured.

## Todo

- replace ns-tracker with tools.namespace
  - with this include the ability to setup certain predefined values?
  - allow retraction of defined values (ones we don't want/need anymore)
- allow loading of user defined functions, also to be
  included when calling `reload-ns` or `ns-/ns+`
- ensure configuration file changes get reloaded
- ensure macros, multi-methods, etc., get reloaded (this probably requires tools.namespace)
- function to import new defaults from repl.edn.example or have defaults in separate config
- repl.edn.example
- can reload only (on request)
  - current namespace
  - specified namespaces
- fix :init conflicts (bypassed by using injections)

- Other functions/macros
- extend macro expander to expand n levels only on request?
- pp shortcut for pprint?

- support excluding functions from the current namespace (if this is possible)
  - so that people can redefine functions they know they don't need direct access to
  - only used if functions are referred locally

- declare the main namespace used, so that you can ->
  - switch namespaces quickly
  - use the shorthand in require statements

- record sequences of commands other than a function
  - commands get wrapped in a do block,
  - or if dependent, in an as->/let

## Issues

- If you have an :init in the same profile or the root of your project.clj
  for the current project, this will conflict with the :init section
  required for the utilities to work.
- For this reason it is recommended you use the `:injections` parameter
  instead of `:repl-options {:init ...}` for loading the utility functions.

## Branches

- At any given time there may be a lot of branches.
- Feature branches may have been merged to develop and paused rather than deleted (this is probably bad.)
- Branches have been merged using fast forward so no merge commits (this may be bad.)
- Experimental branches are things I am trying which have a high level of uncertainty whether they are
  - possible
  - worth doing
- `edge` is for things unknown which have not yet made it to an experimental or feature branch.
  - there be dragons...

## License

The MIT License (MIT)

Copyright (c) 2015 Paul Barrett

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
