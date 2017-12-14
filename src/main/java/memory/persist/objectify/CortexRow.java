//
// $Id$

package memory.persist.objectify;

import com.googlecode.objectify.annotation.*;

import memory.data.Access;

/**
 * Contains the data for a Cortex.
 */
@Entity
public class CortexRow
{
    /** The id of this cortex. */
    @Id public String id;

    /** The id of the root datum for this cortex. */
    public long rootId;

    /** The id of the owner of this cortex. */
    public String ownerId;

    /** The public access settings for this cortex. */
    public Access publicAccess;

    @Override // from Object
    public String toString ()
    {
        return id + ":" + rootId + ":" + ownerId;
    }
}
