//
// $Id$

package memory.persist.objectify;

import javax.persistence.Id;

import com.googlecode.objectify.annotation.Unindexed;

import memory.data.Access;

/**
 * Contains the data for a Cortex.
 */
public class CortexRow
{
    /** The id of this cortex. */
    @Id public String id;

    /** The id of the root datum for this cortex. */
    @Unindexed public long rootId;

    /** The id of the owner of this cortex. */
    @Unindexed public String ownerId;

    /** The public access settings for this cortex. */
    @Unindexed public Access publicAccess;

    @Override // from Object
    public String toString ()
    {
        return id + ":" + rootId + ":" + ownerId;
    }
}
