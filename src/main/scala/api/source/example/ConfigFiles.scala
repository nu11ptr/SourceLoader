/*
 * Copyright 2013 API Technologies, LLC
 *
 * Distributed under the terms of the modified BSD license. See the LICENSE file
 * for further details.
 */

import api.source.{companion, srcFileToClass}
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
  // NOTE: Requires 'MyParentConfig' and 'MyChildConfig' in cwd
  object ConfigFiles extends App {
    val srcDir = System.getProperty("user.dir") + File.separator

    val obj = for {
      _ <- srcFileToClass[ChildConfig](srcDir + "MyChildConfig.cfg").right
      o <- companion(srcFileToClass[ParentConfig](srcDir + "MyParentConfig.cfg")).right
    } yield o

    obj match {
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