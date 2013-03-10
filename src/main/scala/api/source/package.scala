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

  def srcFileToObj[T: ClassTag](srcFile:  String,
                                name:     String = "",
                                args:     Seq[Any] = Seq.empty,
                                outDir:   String = "",
                                force:    Boolean = false): ObjEither[T] =
    tryCreate(srcFileToClass(srcFile, name, outDir, force), args)

  def srcToObj[T: ClassTag](src:      String,
                            name:     String,
                            args:     Seq[Any] = Seq.empty,
                            outDir:   String = "",
                            modDate:  Date = new Date,
                            force:    Boolean = false): ObjEither[T] =
    tryCreate(srcToClass(src, name, outDir, modDate, force), args)

  def companion[T](cls: Class[T]): ObjEither[T] =
    newInstance(loadClass(companionPath(cls)))

  def companion[T](cls: ClsEither[T]): ObjEither[T] =
    cls match {
      case Left(e)    => Left(e)
      case Right(cl)  => companion(cl)
    }

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
}