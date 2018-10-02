/**
 * Copyright (C) 2018 DANS - Data Archiving and Networked Services (info@dans.knaw.nl)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.knaw.dans.bag.v0

import java.nio.file.{ AtomicMoveNotSupportedException, FileAlreadyExistsException, Paths }

import better.files.File
import better.files.File.temporaryFile
import nl.knaw.dans.bag.ImportOption
import nl.knaw.dans.bag.fixtures.{ TestBags, TestSupportFixture }

import scala.util.{ Failure, Success }

class AddStagedPayloadSpec extends TestSupportFixture with TestBags {

  "addPayloadFile ATOMIC_MOVE" should "fail if the staged file is a directory" in {
    val stagedDir = (testDir / "some/dir").createDirectories()
    simpleBagV0().addPayloadFile(stagedDir, Paths.get("do-not-care"), ImportOption.ATOMIC_MOVE) should matchPattern {
      case Failure(e: IllegalArgumentException) if e.getMessage.startsWith("src cannot be moved, as it is not a regular file:") =>
    }
    stagedDir should exist
  }

  it should "fail if the destination exists" in {
    val stagedFile = (testDir / "some.file").createFile()
    val bag = simpleBagV0()
    bag.addPayloadFile(stagedFile, Paths.get("sub/u"), ImportOption.ATOMIC_MOVE) should matchPattern {
      case Failure(e: FileAlreadyExistsException) if e.getMessage == s"${ bag.data / "sub/u" }" =>
    }
    stagedFile should exist
  }

  it should "fail if the destination is not inside the bag/data directory" in {
    val stagedFile = (testDir / "some.file").createFile()
    simpleBagV0().addPayloadFile(stagedFile, Paths.get("../../sub/u"), ImportOption.ATOMIC_MOVE) should matchPattern {
      case Failure(e: IllegalArgumentException) if e.getMessage.endsWith(" is supposed to point to a file that is a child of the bag/data directory") =>
    }
    stagedFile should exist
  }

  it should "fail if the destination is present as fetch file" in {
    val stagedFile = (testDir / "some.file").createFile()
    fetchBagV0().addPayloadFile(stagedFile, Paths.get("sub/u"), ImportOption.ATOMIC_MOVE) should matchPattern {
      case Failure(e: FileAlreadyExistsException) if e.getMessage.endsWith("file already present in bag as a fetch file") =>
    }
    stagedFile should exist
  }

  it should "fail in case of different providers or mounts" in pendingUntilFixed {
    val bag = fetchBagV0()
    // TODO pick another mount than the one for the bag
    val managedFile = temporaryFile(parent = Some(File("/tmp")))
    managedFile.apply { stagedFile =>
      assume(stagedFile.exists)
      bag.addPayloadFile(stagedFile, Paths.get("new/file"), ImportOption.ATOMIC_MOVE) should matchPattern {
        case Failure(e: AtomicMoveNotSupportedException) if e.getMessage.endsWith("???") =>
      }
      stagedFile should exist
    }
  }

  it should "move the staged file" in {
    val stagedFile = (testDir / "some.file").createFile()
    inside(simpleBagV0().addPayloadFile(stagedFile, Paths.get("new/file"), ImportOption.ATOMIC_MOVE)) {
      case Failure(e) => fail(s"Expected success but got: $e")
      case Success(resultBag) =>
        stagedFile shouldNot exist
        (resultBag.data / "new/file") should exist
        resultBag.payloadManifests.flatMap(_._2.keys.toList) should contain(resultBag.data / "new/file")
    }
  }
}
