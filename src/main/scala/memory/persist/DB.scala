//
// $Id$

package memory.persist

import memory.data.{Access, Datum, FieldValue, Type}

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

  /** Creates a cortex, populates it with the supplied root and contents data and grants the
   * specified owner read/write access.
   * @returns true if the cortex was created, false if said cortex already exists. */
  def createCortex (cortexId :String, ownerId :String, root :Datum, contents :Datum) :Boolean

  /** Loads the userId of the owner of the specified cortex. */
  def loadOwner (cortexId :String) :String

  /** Loads the root datum for the specified cortex. */
  def loadRoot (cortexId: String) :Option[Datum]

  /** Returns the access permissions for the specified user for the specified cortex.
   * @param userId the id of the user to be checked. */
  def loadAccess (userId :String, cortexId :String) :Option[Access]

  /** Returns the access permissions for the specified user for the specified datum.
   * @param userId the id of the user to be checked. */
  def loadAccess (userId :String, cortexId :String, datumId :Long) :Option[Access]

  /** Returns the cortices to which the supplied user has access. */
  def loadCortexAccess (userId :String) :Seq[(Access,String)]

  /** Updates the access permissions for the specified user for the specified cortex. */
  def updateAccess (userId :String, cortexId :String, access :Access) :Unit

  /** Updates the access permissions for the specified user for the specified datum. */
  def updateAccess (userId :String, cortexId :String, datumId :Long, access :Access) :Unit

  /** Loads the specified datum. Throws an excepton if it does not exist. */
  def loadDatum (cortexId :String, id :Long) :Datum

  /** Loads the specified datum. */
  def loadDatum (cortexId :String, parentId :Long, title :String) :Option[Datum]

  /** Loads the children of the specified datum. */
  def loadChildren (cortexId :String, id :Long) :Array[Datum]

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
}
