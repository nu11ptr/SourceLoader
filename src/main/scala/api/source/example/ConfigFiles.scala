/*
 * Copyright 2013 API Technologies, LLC
 *
 * Distributed under the terms of the modified BSD license. See the LICENSE file
 * for further details.
 */

import api.source.{srcFileToObj, srcFileToClass}
import java.io.File

trait ParentConfig {
  def optList: List[Int] = Nil

  def childConfig: ChildConfig
}

trait ChildConfig {
  def reqString: String

  def optString: Option[String] = None
}

package api.source.example {
  // NOTE: This example will only work when extracted (not in a JAR file)
  object ConfigFiles extends App {
    val srcDir = System.getProperty("user.dir") +
      List("", "src", "main", "resources", "").mkString(File.separator)

    srcFileToClass[ChildConfig](srcDir + "MyChildConfig.cfg", "MyChildConfig$")
    srcFileToObj[ParentConfig](srcDir + "MyParentConfig.cfg", "MyParentConfig$") match {
      case Left(e)        =>
        throw e
      case Right(config)  =>
        println(s"Parent: Opt. list is: ${config.optList}")

        val childConfig = config.childConfig
        println(s"Child: Req. string is: ${childConfig.reqString}")
        println(s"Child: Opt. string is: ${childConfig.optString}")
    }
  }
}