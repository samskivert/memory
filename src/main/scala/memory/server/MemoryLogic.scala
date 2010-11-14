//
// $Id$

package memory.server

import java.util.{Arrays, ArrayList, Calendar, Collections}

import memory.data.{Datum, MetaData, Type}
import memory.persist.DB

/**
 * Contains logic shared by servlets but that doesn't really belong in the database.
 */
object MemoryLogic
{
  val db :DB = memory.persist.objectify.ObjectifyDB

  /** Resolves the children of the supplied datum. */
  def resolveChildren (cortexId :String)(root :Datum) :Datum = {
    root.children = new ArrayList[Datum](root.`type` match {
      case Type.LIST => loadAndResolveChildren(cortexId, root.id)
      case Type.CHECKLIST => loadAndResolveChildren(cortexId, root.id)
      case Type.JOURNAL => resolveJournalChild(cortexId, root.id)
      case Type.PAGE => loadAndResolveChildren(cortexId, root.id)
      case Type.HTML | Type.WIKI => loadMediaChildren(cortexId, root.id)
      case _ => Collections.emptyList
    })
    if (root.`type` == Type.CHECKLIST) {
      archiveExpiredChildren(cortexId, root)
    }
    root
  }

  /** Loads and resolves the children of the specified parent. */
  def loadAndResolveChildren (cortexId :String, parentId :Long) =
    Arrays.asList(db.loadChildren(cortexId, parentId) map(resolveChildren(cortexId)) :_*)

  /** Loads (but does not resolve, as it is not needed) the media children of the parent. */
  def loadMediaChildren (cortexId :String, parentId :Long) =
    Arrays.asList(db.loadChildren(cortexId, parentId, Type.MEDIA) :_*)

  /** Resolves today's child of the supplied journal parent. */
  def resolveJournalChild (cortexId :String, id :Long) =
    Arrays.asList(resolveJournalDatum(cortexId, id, System.currentTimeMillis))

  /** Loads and resolves the journal datum for the specified date, creating it if necessary. */
  def resolveJournalDatum (cortexId :String, journalId :Long, date :Long) :Datum = {
    val title = journalTitle(date)
    resolveChildren(cortexId)(db.loadDatum(cortexId, journalId, title) match {
      case Some(datum) => datum
      case None => {
        val datum = new Datum
        datum.parentId = journalId
        datum.`type` = Type.LIST
        datum.meta = "" // TODO: note journalness here?
        datum.title = title
        db.createDatum(cortexId, datum)
        datum
      }
    })
  }

  /** Returns the title of the journal page that falls on the supplied date. */
  def journalTitle (when :Long) = {
    val cal = Calendar.getInstance
    cal.setTimeInMillis(when)
    cal.set(Calendar.HOUR_OF_DAY, 12) // noon!
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    cal.getTimeInMillis.toString
  }

  /** Marks expired children as archived and removes them from the children list. */
  def archiveExpiredChildren (cortexId :String, checklist :Datum) {
    val iter = checklist.children.iterator
    // TODO: allow checklist metadata to contain custom expiry information
    val expired = System.currentTimeMillis - DEFAULT_EXPIRY_TIME
    while (iter.hasNext) {
      val child = iter.next
      if (child.when < expired && new MetaData(child.meta).get(MetaData.DONE, false)) {
        db.archiveDatum(cortexId, child.id)
        iter.remove
      }
    }
  }

  private val DEFAULT_EXPIRY_TIME = 2 * 24*60*60*1000L
}
