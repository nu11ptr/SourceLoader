/*
 * Copyright 2013 API Technologies, LLC
 *
 * Distributed under the terms of the modified BSD license. See the LICENSE file
 * for further details.
 */

package api.source

import java.io.File
import org.scalatest.FunSuite

trait FileTestTrait {
  def test: String
}

trait TestTrait {
  def test: Int
}

class CompileTests extends FunSuite {
  test("srcToObj") {
    val src = """
      import api.source.TestTrait

      class Test(val i: Int) extends TestTrait {
        def test: Int = i
      }
    """

    val testVal = 42

    srcToObj[TestTrait](src, "Test", Seq(testVal)) match {
      case Left(e)     => throw e
      case Right(test) => expectResult(testVal)(test.test)
    }
  }

  // NOTE: This test will only work when in extracted form (not in a JAR)
  test("srcFileToObj") {
    val testVal = "test"
    val dir = System.getProperty("user.dir") +
      List("", "src", "test", "resources", "api", "source", "").mkString(File.separator)
    val name = "api.source.FileTest"

    srcFileToObj[FileTestTrait](dir + "FileTest.scala", name, Seq(testVal), dir) match {
      case Left(e)     => throw e
      case Right(test) => expectResult(testVal)(test.test)
    }
  }
}