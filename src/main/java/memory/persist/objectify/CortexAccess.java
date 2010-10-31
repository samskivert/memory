//
// $Id$

package memory.persist.objectify;

import javax.persistence.Id;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Parent;
import com.googlecode.objectify.annotation.Unindexed;

import memory.data.Access;

/**
 * Contains access control information for a cortex.
 */
public class CortexAccess
{
    /** The user in question. */
    @Parent public Key<UserRow> userId;

    /** The id of this access row. */
    @Id public Long id;

    /** The cortex in question. */
    public String cortexId;

    /** The user's access to the datum. */
    @Unindexed public Access access;
}
