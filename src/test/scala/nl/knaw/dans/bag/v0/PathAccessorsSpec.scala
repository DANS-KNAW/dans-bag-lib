package nl.knaw.dans.bag.v0

import nl.knaw.dans.bag.fixtures.{ TestBags, TestSupportFixture }

import scala.language.postfixOps

class PathAccessorsSpec extends TestSupportFixture with TestBags {

  "baseDir" should "return the root directory of the bag" in {
    simpleBagV0().baseDir shouldBe simpleBagDirV0
  }

  "data" should "point to the root of the bag/data directory" in {
    val bag = simpleBagV0()
    bag.data.toJava shouldBe (bag.baseDir / "data" toJava)
    bag.data.listRecursively.toList should contain only(
      bag.data / "x",
      bag.data / "y",
      bag.data / "z",
      bag.data / "sub",
      bag.data / "sub" / "u",
      bag.data / "sub" / "v",
      bag.data / "sub" / "w",
    )
  }
}
