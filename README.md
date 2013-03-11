SourceLoader
============

A simple source code compiler library for Scala. It has the ability to load
independent Scala classes or objects from either source string or file form.
This allows use cases such as plugins and configuration files, and allows us
to omit compilation before distribution.

Installation
============

* Requires Scala 2.10 (which requires Java 1.6)

**Option 1: SBT**

    libraryDependencies += "com.api-tech" % "sourceloader_2.10" % "0.1.0"

**Option 2: Copy the files into your project**

Copy both 'package.scala' files into the folders for package api.source and
api.source.util in your project. Optionally, change the package to match your
organization.

**Option 3: [Download](https://bitbucket.org/apitech/sourceloader/downloads/sourceloader_2.10-0.1.0.jar)**

Examples
========

Hello World - Plugin
--------------------

NOTE: Does not work in the REPL. It seems traits/classes defined there have no
actual path/location, and we need it to add to the classpath.

First, the obligatory 'hello world'. This example loads a simple plugin from
a source string.

    import api.source.srcToObj

    trait Plugin { def print() }

    val myPlugin = """
      class MyPlugin extends Plugin { def print() { "Hello World!" } }
    """

    srcToObj[Plugin](myPlugin, "MyPlugin") match {
      case Right(plugin) => plugin.print()
      case Left(e)       => throw e
    }

The output is:

    Hello World!

Config Files
------------

This example shows how we can use Scala sources as our config files to save us
the need of using a config library. Since we don't have to explicitly compile,
users can edit as needed, and the source is automatically recompiled at load
time.

NOTE: Does not work in the REPL. This example must be run using multiple files.
It has not been tested, it should work as is or with very minor corrections.

    // MyConfig.txt
    object MyConfig extends Config {
      val nameList = List("Smith", "Doe", "Williams", "Jones")
    }

    // ConfigExample.scala
    import api.source.{srcFileToClass, companion}

    trait Config {
      def nameList: List[String]
    }

    companion(srcFileToClass[Config]("MyConfig.txt")) match {
      case Right(config) => println(s"Name list: ${config.nameList}")
      case Left(e)       => throw e
    }

The output is:

    Name list: List(Smith, Doe, Williams, Jones)

Features
=========

* Extremely small and lightweight
* Learn the API in minutes - no need to know Scala compiler API
* No dependencies outside Scala standard libraries
* Compile and load classes, instances of classes, and objects
* Distribute simple classes in source form - they are compiled when loaded
* Exceptions are caught and wrapped in Either objects for easier composability.

Status
======

Currently the code should be considered beta quality. It works, but has not
received rigorous testing.

Links
=====

Primary: <https://bitbucket.org/apitech/SourceLoader>

Mirror: <https://github.com/nu11ptr/SourceLoader>

ScalaDoc: <http://apitech.bitbucket.org/sourceloader/scaladoc/#api.source.package>

Downloads: <https://bitbucket.org/apitech/sourceloader/downloads>


License
=======

SourceLoader is released under a modified BSD license. This means you can use it
in open source or commerical programs without the need to release your code. All
we ask is that you maintain our copyright notice.

Contributions
=============

Regretfully, we are unable to accept code contributions at this time. The
library currently does what we need it to do, and we wish to retain the sole
copyright on the the code. Please do submit bug reports, fork, and change the
code as you need, but please understand that pull requests will be refused.