//
// $Id$

package memory.persist.objectify;

import javax.persistence.Id;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Parent;
import com.googlecode.objectify.annotation.Unindexed;

import memory.data.Access;

/**
 * Contains access control information for a datum.
 */
public class DatumAccess
{
    /** The user in question. */
    @Parent public Key<UserRow> userId;

    /** The id of this access row. */
    @Id public Long id;

    /** The cortex in which the datum in question lives. */
    public String cortexId;

    /** The datum in question. */
    public long datumId;

    /** The user's access to the datum. */
    @Unindexed public Access access;
}
