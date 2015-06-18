# UXBox

## Development

First of all, make sure you have [leiningen](http://leiningen.org/) installed. Grab the code and run:

```
$ lein figwheel
```

This will compile ClojureScript whenever you make changes and serve the application in [localhost](http://localhost:3449/).
Open the page.

### ClojureScript browser-connected REPL

The aforementioned command also starts a [nrepl](https://github.com/clojure/tools.nrepl) (network REPL) in the port 7888.

You can connect to it from a shell using the following command:

```
$ lein repl :connect 7888
```

In Emacs you can use [cider's](https://github.com/clojure-emacs/cider) `M-x cider-connect` command and tell it that nREPL is
running on `localhost:7888` to connect.

After connecting to nREPL, run the following Clojure code in it:

```
user> (use 'figwheel-sidecar.repl-api)
user> (cljs-repl)
```

After that, a figwheel message will appear and the prompt will change to `cljs.user>`. We can now evaluate ClojureScript in the
browser from the REPL.

### Testing

For running the tests from a shell, run the following command:

```
$ lein cljsbuild once test
```

If you want to run the tests from a ClojureScript REPL, you can do it like so (given that you want to test the `uxbox.core` namespace):

```
cljs.user> (require '[cljs.test :as t])
cljs.usre> (t/run-tests 'uxbox.core)
```

Note that the test output will appear in the browser and in the shell where you launched the `lein fighweel` command.

## License

TODO
