package nl.knaw.dans.bag.v0

import java.io.InputStream

import better.files.File
import nl.knaw.dans.bag.RelativePath
import nl.knaw.dans.bag.util.{ DatasetAccessCategory, FileAccessCategory }
import nl.knaw.dans.bag.util.DatasetAccessCategory.DatasetAccessCategory
import nl.knaw.dans.bag.util.DcTermsElementType.DcTermsElementType
import nl.knaw.dans.bag.util.FileAccessCategory.FileAccessCategory
import nl.knaw.dans.bag.util.XmlLang.XmlLang

import scala.util.Try

/**
 * FilesXml gives a listing of all payload files.
 * the resulting xml file will be valid according to
 * https://easy.dans.knaw.nl/schemas/bag/metadata/files/files.xsd
 */
case class FilesXml() {

  var files : Map[String, FilesXmlItem] = Map()
  def addFilesXmlItem(item: FilesXmlItem) : Unit = {
    files += (item.filepath -> item)
  }

  /**
   * Saves the serialized FilesXml at the given location
   * @param file the location on the file system to save the FilesXml object to
   * @return `scala.util.Success` if the save was performed successfully,
   *         `scala.util.Failure` otherwise
   */
  def save(file: File): Try[Unit] = ???

}

object FilesXml {

  def empty(): FilesXml = {
    FilesXml()
  }

  def read(file: File): Try[FilesXml] = ???

  def read(inputStream: InputStream): Try[FilesXml] = ???

  /**
   * Add or overwrite the file-item with this relative path.
   * @param item the FilesXmlItem to add or overwrite in the files.xml
   * @return `scala.util.Success` if the add was performed successfully,
   *         `scala.util.Failure` otherwise
   */
  def add(item: FilesXmlItem): Try[Unit] = ???
  //TODO: check that the pathInData exists

  /**
   * Given an access category of a dataset, returns the inferred accessibleToRights FileAccessCategory,
   * according to the DansBagitProfile-v0 rules
   *
   * @param datasetAccessCategory
   * @return returns the inferred accessibleToRights
   */
  def accessCategoryToAccessibleTo(datasetAccessCategory: DatasetAccessCategory): FileAccessCategory = {
     datasetAccessCategory match {
       case DatasetAccessCategory.GROUP_ACCESS => FileAccessCategory.RESTRICTED_GROUP
       case DatasetAccessCategory.NO_ACCESS => FileAccessCategory.NONE
       case DatasetAccessCategory.OPEN_ACCESS => FileAccessCategory.ANONYMOUS
       case DatasetAccessCategory.OPEN_ACCESS_FOR_REGISTERED_USERS => FileAccessCategory.KNOWN
       case DatasetAccessCategory.REQUEST_PERMISSION => FileAccessCategory.RESTRICTED_REQUEST
     }
  }

  /**
   * Given an access category of a dataset, returns the inferred visibleToRights FileAccessCategory,
   * according to the DansBagitProfile-v0 rules
   *
   * @param datasetAccessCategory
   * @return returns the inferred visibleToRights
   */
  def accessCategoryToVisibleTo(datasetAccessCategory: DatasetAccessCategory): FileAccessCategory = {
    FileAccessCategory.ANONYMOUS
  }
}

/**
 *
 * @param pathInData the path to the file in the payload directory of the DansBag
 * @param fileFormat the format according to https://www.iana.org/assignments/media-types/media-types.xhtml
 * @param accessibleToRights the accessibility rights to the file. Required if different from the access rights as inferred from Metadata/dataset.xml
 * @param visibleToRights the visiblity rights to the file. Required if different from the visibility rights as inferred from Metadata/dataset.xml
 * @param dcelements the set containing the DcTermsElements for this file
 */
case class FilesXmlItem(pathInData: RelativePath,
                        fileFormat: FileFormat,
                        accessibleToRights: Option[FileAccessCategory],
                        visibleToRights: Option[FileAccessCategory],
                        dcelements: Set[DcTermsElement] = Set()
                       ) {

  val filepath: String = pathInData.toString()
}

case class FileFormat(fileFormat: String)

case class DcTermsElement(tag: DcTermsElementType, value: String, language: Option[XmlLang] = None)





