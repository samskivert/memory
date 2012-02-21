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

    /** The id of this access row; of the form {@code cortexId:datumId}. */
    @Id public String id;

    /** The user's access to the datum. */
    @Unindexed public Access access;
}
