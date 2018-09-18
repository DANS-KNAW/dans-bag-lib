package nl.knaw.dans.bag.v0

import java.nio.file.{ AtomicMoveNotSupportedException, FileAlreadyExistsException, Paths }

import better.files.File
import better.files.File.temporaryFile
import nl.knaw.dans.bag.fixtures.{ TestBags, TestSupportFixture }

import scala.util.{ Failure, Success }

class AddStagedPayloadSpec extends TestSupportFixture with TestBags {

  "addStagedPayloadFile" should "fail if the staged file is a directory" in {
    val stagedDir = (testDir / "some/dir").createDirectories()
    simpleBagV0().addStagedPayloadFile(
      stagedDir,
      Paths.get("do-not-care")
    ) should matchPattern {
      case Failure(e) if e.isInstanceOf[IllegalArgumentException] &&
        e.getMessage.startsWith("StagedPayloadFile is not a regular file:") =>
    }
    stagedDir should exist
  }

  it should "fail if the destination exists" in {
    val stagedFile = (testDir / "some.file").createFile()
    val bag = simpleBagV0()
    bag.addStagedPayloadFile(
      stagedFile,
      Paths.get("sub/u")
    ) should matchPattern {
      case Failure(e) if e.isInstanceOf[FileAlreadyExistsException] &&
        e.getMessage == s"${ bag.data / "sub/u" }" =>
    }
    stagedFile should exist
  }

  it should "fail if the destination is not inside the bag/data directory" in {
    val stagedFile = (testDir / "some.file").createFile()
    simpleBagV0().addStagedPayloadFile(
      stagedFile,
      Paths.get("../../sub/u")
    ) should matchPattern {
      case Failure(e) if e.isInstanceOf[IllegalArgumentException] &&
        e.getMessage.endsWith(" is supposed to point to a file that is a child of the bag/data directory") =>
    }
    stagedFile should exist
  }

  it should "fail if the destination is present as fetch file" in {
    val stagedFile = (testDir / "some.file").createFile()
    fetchBagV0().addStagedPayloadFile(
      stagedFile,
      Paths.get("sub/u")
    ) should matchPattern {
      case Failure(e) if e.isInstanceOf[FileAlreadyExistsException] &&
        e.getMessage.endsWith("file already present in bag as a fetch file") =>
    }
    stagedFile should exist
  }

  it should "fail in case of different providers or mounts" in pendingUntilFixed {
    val bag = fetchBagV0()
    // TODO pick another mount than the one for the bag
    val managedFile = temporaryFile(parent = Some(File("/tmp")))
    managedFile.apply { stagedFile =>
      assume(stagedFile.exists)
      bag.addStagedPayloadFile(
        stagedFile,
        Paths.get("new/file")
      ) should matchPattern {
        case Failure(e) if e.isInstanceOf[AtomicMoveNotSupportedException] &&
          e.getMessage.endsWith("???") =>
      }
      stagedFile should exist
    }
  }

  it should "move the staged file" in {
    val stagedFile = (testDir / "some.file").createFile()
    inside(simpleBagV0().addStagedPayloadFile(
      stagedFile,
      Paths.get("new/file")
    )) {
      case Failure(e) => fail(s"Expected success but got: $e")
      case Success(resultBag) =>
        stagedFile shouldNot exist
        (resultBag.data / "new/file") should exist
        resultBag.payloadManifests.flatMap(_._2.keys.toList) should contain(resultBag.data / "new/file")
    }
  }
}
