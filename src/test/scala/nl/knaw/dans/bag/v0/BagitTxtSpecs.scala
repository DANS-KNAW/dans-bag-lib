package nl.knaw.dans.bag.v0

import java.nio.charset.StandardCharsets

import gov.loc.repository.bagit.domain.Version
import nl.knaw.dans.bag.SupportedVersions
import nl.knaw.dans.bag.fixtures.{ TestBags, TestSupportFixture }

class BagitTxtSpecs extends TestSupportFixture with TestBags {

  "bagitVersion" should "read the version from bagit.txt" in {
    simpleBagV0().bagitVersion shouldBe new Version(0, 97)
  }

  "withBagitVersion" should "change the version in the bag" in {
    simpleBagV0()
      .withBagitVersion(SupportedVersions.Version_1_0)
      .bagitVersion shouldBe new Version(1, 0)
  }

  "fileEncoding" should "read the file encoding from bagit.txt" in {
    simpleBagV0().fileEncoding shouldBe StandardCharsets.UTF_8
  }

  "withFileEncoding" should "change the file encoding in the bag" in {
    simpleBagV0()
      .withFileEncoding(StandardCharsets.UTF_16LE)
      .fileEncoding shouldBe StandardCharsets.UTF_16LE
  }
}
