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
import api.source.util._

package object source {
  type ClsEither[T] = Either[Exception, Class[T]]
  type ObjEither[T] = Either[Exception, T]

  private def tryCompile[T: ClassTag](src:        String,
                                      outDir:     String,
                                      name:       String,
                                      srcLastMod: Long,
                                      force:      Boolean,
                                      isFile:     Boolean): ClsEither[T] = {
    val clsPath = outDir + File.separator + name
    val clsLastMod = lastModified(clsPath + ".class")

    if (force || (clsLastMod == 0L) || (srcLastMod > clsLastMod)) {
      compile(src, outDir, name, isFile)
    } else loadClass(clsPath)
  }

  private def tryCreate[T: ClassTag](clsEither: ClsEither[T], args: Seq[Any])
      : ObjEither[T] =
    clsEither match {
      case Left(obj)  => Left(obj)
      case Right(cls) => newInstance(cls, args)
    }

  // *** Main functions ***
  def srcFileToClass[T: ClassTag](srcFile:  String,
                                  outDir:   String = "",
                                  name:     String = "",
                                  force:    Boolean = false): ClsEither[T] = {
    val (out, file) = splitPathAndFile(srcFile)
    require(file.nonEmpty, "Must specify file name")
    val newOut = if (outDir.isEmpty) out else outDir
    val clsName = if (name.isEmpty) stripExt(file) else name

    val srcLastMod = lastModified(srcFile)
    require(srcLastMod > 0L, "Source file not found?")

    tryCompile(srcFile, newOut, clsName, srcLastMod, force, isFile = true)
  }

  def srcToClass[T: ClassTag](src:      String,
                              outDir:   String,
                              name:     String,
                              modDate:  Date = new Date,
                              force:    Boolean = false): ClsEither[T] = {
    require(src.nonEmpty, "Must specify source code")
    require(name.nonEmpty, "Must specify name of class/object")
    val newOut = outDir
    val clsName = name
    val srcLastMod = modDate.getTime

    tryCompile(src, newOut, clsName, srcLastMod, force, isFile = false)
  }

  def srcFileToObj[T: ClassTag](srcFile:  String,
                                args:     Seq[Any] = Seq.empty,
                                outDir:   String = "",
                                name:     String = "",
                                force:    Boolean = false): ObjEither[T] =
    tryCreate(srcFileToClass(srcFile, outDir, name, force), args)

  def srcToObj[T: ClassTag](src:      String,
                            outDir:   String,
                            name:     String,
                            args:     Seq[Any] = Seq.empty,
                            modDate:  Date = new Date,
                            force:    Boolean = false): ObjEither[T] =
    tryCreate(srcToClass(src, outDir, name, modDate, force), args)
}