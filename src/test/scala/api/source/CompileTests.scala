/*
 * Copyright 2013 API Technologies, LLC
 *
 * Distributed under the terms of the modified BSD license. See the LICENSE file
 * for further details.
 */

package api.source

import org.scalatest.FunSuite

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

    val testVal: Int = 42
    srcToObj[TestTrait](src, ".", "Test", Seq(testVal)) match {
      case Left(e)     => throw e
      case Right(test) => expectResult(testVal)(test.test)
    }
  }
}