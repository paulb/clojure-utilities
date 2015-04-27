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
:injections [(require 'unknown-unit.repl.ns)
             (unknown-unit.repl.ns/init)]
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

The utility functions (unknown-unit.repl.ns) will generally be referred as
local functions unless specified otherwise in the config.

## Usage

Start a repl inside any of your projects.

`(reload-ns)` will reload all namespaces from your project src and test
directories, as well as the `unknown-unit.repl.ns` namespace.

`(ns- <namespace>)` change to the requested namespace and import user
specified namespaces, with namespace separation or referred as local
functions (:refer :all).

`(ns+ <namespace>)` does the same as `ns-` except that all defined namespaces
are referred to local functions, whether `:as` or `:refer` were already declared.

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
- make this an installable library, instead of having to use
  file paths
- fix :init conflicts (bypassed by using injections)

- Other functions/macros
- macro to print evaluation of macro (recursive if requested to n levels)
- pp shortcut for pprint

## Issues

- ns-alias not applied in initially loaded namespace
- If you have an :init in the same profile or the root of your project.clj
  for the current project, this will conflict with the :init section
  required for the utilities to work.
- For this reason it is recommended you use the `:injections` parameter
  instead of `:repl-options {:init ...}` for loading the utility functions.

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
