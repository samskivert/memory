//
// $Id$

package memory.data;

import java.io.Serializable;

/**
 * Contains metadata for a particular cortex.
 */
public class Cortex implements Serializable
{
    /** The id of this cortex. */
    public String id;

    /** The id of the root datum for this cortex. */
    public long rootId;

    /** The id of the owner of this cortex. */
    public String ownerId;

    /** The public access settings for this cortex. */
    public Access publicAccess;
}
