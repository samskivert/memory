//
// $Id$

package memory.persist.objectify;

import javax.persistence.Id;

import com.googlecode.objectify.annotation.Unindexed;

import memory.data.Access;

/**
 * Contains the data for a share request.
 */
public class ShareRow
{
    /** The id of this share request. */
    @Id public String id;

    /** The randomly generated token for this share request. */
    public String token;

    /** The cortex being shared. */
    @Unindexed public String cortexId;

    /** The access to be granted to the sharee. */
    @Unindexed public Access access;

    @Override // from Object
    public String toString () {
        return id + ":" + cortexId + ":" + token;
    }
}
