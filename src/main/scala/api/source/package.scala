/*
 * Copyright 2013 API Technologies, LLC
 *
 * Distributed under the terms of the modified BSD license. See the LICENSE file
 * for further details.
 */

package api

import scala.reflect.ClassTag
import java.util.Date
import java.io.File
import java.lang.reflect.Constructor
import api.source.util._

package object source {
  type ClsEither[T] = Either[Exception, Class[T]]
  type ObjEither[T] = Either[Exception, T]

  private def tryCompile[T: ClassTag](src:        String,
                                      name:       String,
                                      outDir:     String,
                                      srcLastMod: Long,
                                      force:      Boolean,
                                      isFile:     Boolean): ClsEither[T] = {
    val clsPath = outDir + File.separator + name
    val clsLastMod = lastModified(clsPath + ".class")

    if (force || (clsLastMod == 0L) || (srcLastMod > clsLastMod))
      compile(src, outDir, name, isFile)
    else Right(loadClass(clsPath))
  }

  private def tryCreate[T: ClassTag](clsEither: ClsEither[T], args: Seq[Any])
      : ObjEither[T] =
    clsEither match {
      case Left(obj)  => Left(obj)
      case Right(cls) => newInstance(cls, args)
    }

  // *** Main functions ***
  /**
   * Compiles given Scala source file and returns the class. If class file is
   * newer than source than compilation step is omitted.
   *
   * @param srcFile The file that is compiled
   * @param name    Fully qualified class name to load. If omitted, the class
   *                name to load is taken from the filename portion of srcFile
   * @param outDir  Directory to store the resulting class file. If omitted,
   *                the value of System.getProperty("user.dir") is used
   * @param force   Force compilation, even if class timestamp is newer than
   *                the source file
   * @tparam T      The interface or trait that the resulting class file must
   *                satisfy
   * @return        Either the class (right) or an exception if one occurred (left)
   */
  def srcFileToClass[T: ClassTag](srcFile:  String,
                                  name:     String = "",
                                  outDir:   String = "",
                                  force:    Boolean = false): ClsEither[T] = {
    try {
      val (out, file) = splitPathAndFile(srcFile)
      require(file.nonEmpty, "Must specify file name")
      val newOut = if (outDir.isEmpty) out else outDir
      val clsName = if (name.isEmpty) stripExt(file) else name

      val srcLastMod = lastModified(srcFile)
      require(srcLastMod > 0L, "Source file not found?")

      tryCompile(srcFile, clsName, newOut, srcLastMod, force, isFile = true)
    } catch {
      case e: Exception => Left(e)
    }
  }

  /**
   * Compiles given Scala source string and returns the class. If class file is
   * newer than source 'modDate' than compilation step is omitted.
   *
   * @param src     A string containing the Scala source to compile
   * @param name    Fully qualified class name to load
   * @param outDir  Directory to store the resulting class file. If omitted,
   *                the value of System.getProperty("user.dir") is used
   * @param modDate Synthetic 'src' timestamp to compare with class timestamp.
   *                If omitted, the current date/time is used.
   * @param force   Force compilation, even if class timestamp is newer than
   *                the source timestamp
   * @tparam T      The interface or trait that the resulting class file must
   *                satisfy
   * @return        Either the class (right) or an exception if one occurred (left)
   */
  def srcToClass[T: ClassTag](src:      String,
                              name:     String,
                              outDir:   String = "",
                              modDate:  Date = new Date,
                              force:    Boolean = false): ClsEither[T] = {
    try {
      require(src.nonEmpty, "Must specify source code")
      require(name.nonEmpty, "Must specify name of class/object")
      val newOut = if (outDir.isEmpty) System.getProperty("user.dir") else outDir
      val clsName = name
      val srcLastMod = modDate.getTime

      tryCompile(src, clsName, newOut, srcLastMod, force, isFile = false)
    } catch {
      case e: Exception => Left(e)
    }
  }

  /**
   * Compiles given Scala source file and returns a new class instance. If class
   * file is newer than source than compilation step is omitted. Only classes
   * with a single constructor are supported.
   *
   * @param srcFile The file that is compiled
   * @param name    Fully qualified class name to load. If omitted, the class
   *                name to load is taken from the filename portion of srcFile
   * @param args    Sequence of arguments to pass to class's constructor
   * @param outDir  Directory to store the resulting class file. If omitted,
   *                the value of System.getProperty("user.dir") is used
   * @param force   Force compilation, even if class timestamp is newer than
   *                the source file
   * @tparam T      The interface or trait that the resulting class file must
   *                satisfy
   * @return        Either the object (right) or an exception if one occurred (left)
   */
  def srcFileToObj[T: ClassTag](srcFile:  String,
                                name:     String = "",
                                args:     Seq[Any] = Seq.empty,
                                outDir:   String = "",
                                force:    Boolean = false): ObjEither[T] =
    tryCreate(srcFileToClass(srcFile, name, outDir, force), args)

  /**
   * Compiles given Scala source string and returns a new class instance. If
   * class file is newer than source 'modDate' than compilation step is omitted.
   * Only classes with a single constructor are supported.
   *
   * @param src     A string containing the Scala source to compile
   * @param name    Fully qualified class name to load
   * @param args    Sequence of arguments to pass to class's constructor
   * @param outDir  Directory to store the resulting class file. If omitted,
   *                the value of System.getProperty("user.dir") is used
   * @param modDate Synthetic 'src' timestamp to compare with class timestamp.
   *                If omitted, the current date/time is used.
   * @param force   Force compilation, even if class timestamp is newer than
   *                the source timestamp
   * @tparam T      The interface or trait that the resulting class file must
   *                satisfy
   * @return        Either the object (right) or an exception if one occurred (left)
   */
  def srcToObj[T: ClassTag](src:      String,
                            name:     String,
                            args:     Seq[Any] = Seq.empty,
                            outDir:   String = "",
                            modDate:  Date = new Date,
                            force:    Boolean = false): ObjEither[T] =
    tryCreate(srcToClass(src, name, outDir, modDate, force), args)

  /**
   * Returns the single instance of the given class's associated companion
   * object
   *
   * @param cls A class returned from one of the 'src' functions
   * @tparam T  A trait that the returned object must satisfy
   * @return    Either the object (right) or an exception if one occurred (left)
   */
  def companion[T](cls: Class[T]): ObjEither[T] =
    newInstance(loadClass(companionPath(cls)))

  /**
   * Returns the single instance of the given class's associated companion
   * object. Takes either class or exception as input which is useful for
   * chaining function calls.
   *
   * @param cls Either class returned from one of the 'src' functions (right)
   *            or an exception (left)
   * @tparam T  A trait that the returned object must satisfy
   * @return    Either the object (right) or an exception if one occurred (left)
   */
  def companion[T](cls: ClsEither[T]): ObjEither[T] =
    cls match {
      case Left(e)    => Left(e)
      case Right(cl)  => companion(cl)
    }

  /**
   * Creates a new instance of a class using the passed arguments. Only classes
   * with a single constructor are supported.
   *
   * @param cls   class to instantiate
   * @param args  sequence of arguments to be passed to class's constructor
   * @tparam T    A trait that the returned object must satisfy
   * @return      Either the object (right) or an exception if one occurred (left)
   */
  def newInstance[T](cls: Class[T], args: Seq[Any] = Seq.empty): ObjEither[T] = {
    try {
      Right(if (cls.getName.endsWith("$"))
        cls.getField("MODULE$").get(null).asInstanceOf[T]
      else {
        val constructors =  cls.getConstructors.toList
        require(constructors.size == 1,
          "Constructor not found or ambiguous constructor " +
            "(only single constructor classes are allowed)")
        constructors.head.asInstanceOf[Constructor[T]]
          .newInstance(args.asInstanceOf[Seq[AnyRef]]: _*)
      })
    } catch {
      case e: Exception => Left(e)
    }
  }

  /**
   * Creates a new instance of a class using the passed arguments. Only classes
   * with a single constructor are supported. Takes either class or exception
   * as input which is useful for chaining function calls.
   *
   * @param cls   Either class returned from one of the 'src' functions (right)
   *              or an exception (left)
   * @param args  sequence of arguments to be passed to class's constructor
   * @tparam T    A trait that the returned object must satisfy
   * @return      Either the object (right) or an exception if one occurred (left)
   */
  def newInstEither[T](cls: ClsEither[T], args: Seq[Any] = Seq.empty): ObjEither[T] =
    cls match {
      case Left(e)    => Left(e)
      case Right(cl)  => newInstance(cl, args)
    }
}