/*
 * Copyright 2013 API Technologies, LLC
 *
 * Distributed under the terms of the modified BSD license. See the LICENSE file
 * for further details.
 */

package api.source

import scala.reflect.io.VirtualFile
import scala.tools.nsc.Settings
import scala.tools.nsc.reporters.ConsoleReporter
import scala.tools.nsc.Global
import java.io._
import java.net.URLClassLoader
import java.lang.reflect.Constructor

package object util {
  def lastModified(fileName: String): Long = new File(fileName).lastModified

  def makeDirs(path: String): Boolean = {
    val dir = new File(path)
    if (dir.exists && dir.isDirectory) true else dir.mkdirs()
  }

  def stripExt(name: String): String = {
    val index = name.lastIndexOf(".")
    if (index != -1) name.substring(0,index) else name
  }

  def splitPathAndFile(path: String): (String, String) = {
    val index = path.lastIndexOf(File.separator)

    if (index+1 == path.length) (path, "")
    else if (index != -1) (path.substring(0, index+1), path.substring(index+1))
    else ("", path)
  }

  def loadClass[T](fileName: String): ClsEither[T] = {
    val (path, file) = splitPathAndFile(fileName)
    val url = new File(path).toURI.toURL
    val classLoader = new URLClassLoader(Array(url), getClass.getClassLoader)

    try Right(classLoader.loadClass(file).asInstanceOf[Class[T]]) catch {
      case e: Exception => Left(e)
    }
  }

  def getFullPath(classPath: String): String =
    Option(getClass.getResource(classPath)) match {
      case None      => ""
      case Some(url) => url.getPath
    }

  def pathOfClass(className: String): String = {
    val resource = className.split('.').mkString("/", "/", ".class")

    getFullPath(resource) match {
      case s @ ""   => s
      case path @ _ =>
        val indexOfFile = path.indexOf("file:")
        val indexOfSeparator = path.lastIndexOf('!')

        // regular file path - keep the full URL path minus the resource
        if (indexOfFile == -1 || indexOfSeparator == -1) {
          val indexOfResource = path.lastIndexOf(resource)
          path.substring(0, indexOfResource)
        // jar/war/etc - keep the full URL to archive minus the resource
        } else {
          path.substring(indexOfFile, indexOfSeparator)
        }
    }
  }

  def newInstance[T](cls: Class[T], args: Seq[Any] = Seq.empty): ObjEither[T] = {
    val constructors =  cls.getConstructors.toList
    require(constructors.size == 1,
      "Ambiguous constructor, only single constructor classes are allowed.")
    try {
      Right(constructors.head.asInstanceOf[Constructor[T]].newInstance(args))
    } catch {
      case e: Exception => Left(e)
    }
  }

  def compile[T](src: String, outDir: String, name: String, isFile: Boolean = true)
      : ClsEither[T] = {
    if (!makeDirs(outDir)) throw new IOException(s"Unable to create dir '$outDir'")

    val settings = {
      val settings = new Settings

      val bootPathList = List(pathOfClass("scala.tools.nsc.Global"),
        pathOfClass("scala.ScalaObject"))
      // Make sure both scala library and compiler were found
      assume(!bootPathList.contains(""), "Scala lang. libraries could not be found.")
      settings.bootclasspath.value =
        (settings.bootclasspath.value :: bootPathList).mkString(File.pathSeparator)

      //settings.classpath.value += File.pathSeparator + pathOfClass("")

      settings.outdir.value = outDir
      settings
    }

    val strWriter = new StringWriter
    val reporter = new ConsoleReporter(settings,
      new BufferedReader(new StringReader("")), new PrintWriter(strWriter))

    val compiler = new Global(settings, reporter)
    val run = new compiler.Run

    if (isFile) run.compile(List(src))
    else {
      val vFile = new VirtualFile(name + ".scala")
      val os = vFile.output
      os.write(src.getBytes("UTF-8"))
      os.close()
      run.compileFiles(List(vFile))
    }

    reporter.printSummary()
    if (reporter.hasErrors) Left(new RuntimeException(strWriter.toString))
    else loadClass(outDir + name)
  }
}