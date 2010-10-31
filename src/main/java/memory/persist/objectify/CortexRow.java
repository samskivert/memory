//
// $Id$

package memory.persist.objectify;

import javax.persistence.Id;

/**
 * Contains the data for a Cortex.
 */
public class CortexRow
{
    /** The id of this cortex. */
    @Id public String id;

    /** The id of the root datum for this cortex. */
    public long rootId;

    @Override // from Object
    public String toString ()
    {
        return id + ":" + rootId;
    }
}
