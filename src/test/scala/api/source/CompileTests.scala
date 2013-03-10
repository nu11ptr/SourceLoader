/*
 * Copyright 2013 API Technologies, LLC
 *
 * Distributed under the terms of the modified BSD license. See the LICENSE file
 * for further details.
 */

import java.io.File
import org.scalatest.FunSuite

trait FileTestTrait {
  def test: String
}

trait TestTrait {
  def test: Int
}

package api.source {
  class CompileTests extends FunSuite {
    private val src = """
      class Test(val i: Int) extends TestTrait {
        def test: Int = i
      }
    """

    private val dir = System.getProperty("user.dir") + File.separator
    private val name = "FileTest"

    test("srcToObj") {
      val testVal = 42

      srcToObj[TestTrait](src, "Test", Seq(testVal)) match {
        case Left(e)     => throw e
        case Right(test) => expectResult(testVal)(test.test)
      }
    }

    // NOTE: Requires 'FileTest' in cwd
    test("srcFileToObj") {
      val testVal = "test"

      srcFileToObj[FileTestTrait](dir + name, name, Seq(testVal), dir) match {
        case Left(e)     => throw e
        case Right(test) => expectResult(testVal)(test.test)
      }
    }

    test("srcToClass + newInstance") {
      val testVal = 42

      val obj = for {
        c <- srcToClass[TestTrait](src, "Test").right
        o <- newInstance(c, Seq(testVal)).right
      } yield o

      obj match {
        case Left(e)  => throw e
        case Right(o) => expectResult(testVal)(o.test)
      }
    }

    // NOTE: Requires 'FileTest' in cwd
    test("srcFileToClass + newInstance") {
      val testVal = "test"

      val obj = for {
        c <- srcFileToClass[FileTestTrait](dir + name, name, dir).right
        o <- newInstance(c, Seq(testVal)).right
      } yield o

      obj match {
        case Left(e)  => throw e
        case Right(o) => expectResult(testVal)(o.test)
      }
    }
  }
}