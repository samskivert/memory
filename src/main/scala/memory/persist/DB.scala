//
// $Id$

package memory.persist

import java.io.{InputStream, OutputStream}
import memory.data.{Access, AccessInfo, Cortex, Datum, FieldValue, Type}

/**
 * Defines the interface to our persistence services.
 */
trait DB
{
  /** Initializes the database component. */
  def init :Unit

  /** Shuts down the database component. */
  def shutdown :Unit

  /** Notes that the user in question has accessed the service. */
  def noteUser (userId :String) :Unit

  /** Creates a cortex with the supplied root datum and grants the owner read/write access. The id
   * of the root datum will be filled in if creation succeeds.
   * @returns true if the cortex was created, false if said cortex already exists. */
  def createCortex (cortexId :String, ownerId :String, root :Datum) :Boolean

  /** Loads the metadata for the specified cortex. */
  def loadCortex (cortexId: String) :Option[Cortex]

  /** Loads the root datum for the specified cortex. */
  def loadRoot (cortexId: String) :Option[Datum]

  /** Returns the access permissions for the specified user for the specified cortex.
   * @param userId the id of the user to be checked. */
  def loadAccess (userId :String, cortexId :String) :Option[Access]

  /** Returns the access permissions for the specified user for the specified datum.
   * @param userId the id of the user to be checked. */
  def loadAccess (userId :String, cortexId :String, datumId :Long) :Option[Access]

  /** Returns the cortices to which the supplied user has access as (access, cortexId). */
  def loadAccessibleCortices (userId :String) :Seq[AccessInfo]

  /** Returns a list of users that have custom access to the supplied cortex. */
  def loadCortexAccess (cortexId :String) :Seq[AccessInfo]

  /** Updates the public access permissions for the specified cortex. */
  def updateCortexPublicAccess (cortexId :String, access :Access) :Unit

  /** Updates the permissions for the specified cortex access row. */
  def updateCortexAccess (id :Long, callerId :String, access :Access) :Boolean

  /** Updates the access permissions for the specified user for the specified datum. */
  def updateDatumAccess (userId :String, cortexId :String, datumId :Long, access :Access) :Unit

  /** Loads the specified datum. Throws an excepton if it does not exist. */
  def loadDatum (cortexId :String, id :Long) :Datum

  /** Loads the specified datum. */
  def loadDatum (cortexId :String, parentId :Long, title :String) :Option[Datum]

  /** Loads the children of the specified datum. */
  def loadChildren (cortexId :String, id :Long, includeArchived :Boolean = false) :Array[Datum]

  /** Loads the children of the specified datum that are of the specified type. */
  def loadChildren (cortexId :String, id :Long, typ :Type) :Array[Datum]

  /** Loads the specified data. */
  def loadData (cortexId :String, ids :Set[Long]) :Array[Datum]

  /** Updates the specified fields of the specified datum. */
  def updateDatum (cortexId :String, id :Long, updates :Seq[(Datum.Field, FieldValue)]) :Unit

  /** Marks the specified datum as archived. */
  def archiveDatum (cortexId :String, id :Long) :Unit

  /** Creates a new datum and fills in its {@link Datum#id} field.
   * @return the newly assigned id. */
  def createDatum (cortexId :String, datum :Datum) :Long

  /** Deletes the specified datum. */
  def deleteDatum (cortexId :String, id :Long) :Unit

  /** Clones the children (and grandchildren, and so forth) of `fromId` to `toId`. Used when
   * forking a datum into a new cortex. */
  def cloneChildren (fromCortexId :String, fromId :Long, toCortexId :String, toId :Long)

  /** Stores a share request with the supplied metadata.
   * @return the id of the share request. */
  def createShareRequest (token :String, cortexId :String, access :Access) :Long

  /** Loads the name of the cortex being shared by the specified request. */
  def loadShareInfo (token :String) :Option[String]

  /** Accepts the specified share request on behalf of the specified user.
   * @return true if access was granted, false if the request no longer exists. */
  def acceptShareRequest (token :String, userId :String, email :String) :Boolean

  /** Deletes the share request with the specified id. */
  def deleteShareRequest (id :Long) :Unit

  /** Dumps the contents of the database in text form to the supplied writer. Note that media is
   * not included in the dump. */
  def dump (out :OutputStream) :Unit

  /** Loads a database dump from the supplied reader. This is only guaranteed to work on a blank
   * database. Otherwise key conflicts may occur. */
  def undump (in :InputStream) :Unit
}
