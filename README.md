# Clojure Utilities

A general purpose hold-all utilities library.

Iteration one: REPL tools.

## Installation

Clone the repository to a directory of your choosing.

For use in all projects modify your `~/.lein/profiles.clj` `:user` profile.
For use in a specific project only modify `project.clj` for that project.

You will possibly need to add `[ns-tracker "0.2.2"]` to your dependencies,
since these utilities are not installed as a library.

In your chosen `profiles.clj` or `project.clj`, also add or update
the following entries:

```
:source-paths ["<path/to>/clojure-utilities/src"]
:injections [(require '[unknown-unit.repl.ns :refer :all])]
```

If there may be code in `:injections` which might throw an error,
wrap it in a `try...catch` to be able to continue to use the repl.

Any included functions may not be available in this case.

## Setup

Namespaces to be automatically imported into a new namespace when using
`ns-/ns+` to switch namespaces are defined in

`unknown-unit.repl.ns/user-namespaces`

The utilities namespace is always included.

To import a namespace with an alias specify it as usual in `user-namespaces`.

E.g., `[my.project.core :as my-core]`

To import a namespace referring only some functions, again nothing unusual.

E.g., `[my.project.core :refer [my-main]]`

`:as` and `:refer` can be combined as usual.

If an alias or refer is already defined, `ns-` will honor that definition.
If none is defined, e.g.

`[my.project.core]`

it is assumed all functions should be referred locally & this is rewritten as

`[my.project.core :refer :all]`

The utility functions (unknown-unit.repl.ns) will generally be referred as
local functions unless specified otherwise. At the moment this requires a
code change. In the future this and user defined namespaces will come from
a per installation config.

## Usage

Start a repl inside any of your projects.

`(reload-ns)` will reload all namespaces from your project src and test
directories, as well as the `unknown-unit.repl.ns` namespace.

`(ns- <namespace>)` will call `(reload-ns)` and then change to the requested
namespace and import user specified namespaces, with namespace separation
or referred as local functions (:refer :all).

`(ns+ <namespace>)` does the same as `ns-` except that all defined namespaces
are referred to local functions, whether `:as` or `:refer` were already declared.

## Todo

- configuration for setting alias for core utilities
- configuration for setting user defined namespaces
- allow loading of user defined functions, also to be
  included when calling `reload-ns` or `ns-/ns+`
- make this an installable library, instead of having to use
  file paths
- fix :init conflicts (bypassed by using injections)

## Issues

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
