/*
 * Copyright 2013 API Technologies, LLC
 *
 * Distributed under the terms of the modified BSD license. See the LICENSE file
 * for further details.
 */

package api

import scala.reflect.ClassTag

package object source {
  def srcFileToClass[T: ClassTag](srcFile: String, output: String = "", className: String = ""): Class[T] = {
    ???
  }

  def srcToClass[T: ClassTag](src: String, output: String, className: String): Class[T] = {
      ???
    }

  def srcFileToObj[T: ClassTag](srcFile: String, args: Seq[Any] = Seq.empty, outDir: String = "", className: String = ""): T = {
    ???
  }

  def srcToObj[T: ClassTag](src: String, outDir: String, className: String = "", args: Seq[Any] = Seq.empty): T = {
    ???
  }
}