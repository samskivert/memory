//
// $Id$

package memory.persist.objectify;

import com.googlecode.objectify.annotation.*;

import memory.data.Access;

/**
 * Contains the data for a share request.
 */
@Entity
public class ShareRow
{
    /** The id of this share request. */
    @Id public Long id;

    /** The randomly generated token for this share request. */
    @Index public String token;

    /** The cortex being shared. */
    public String cortexId;

    /** The access to be granted to the sharee. */
    public Access access;

    @Override // from Object
    public String toString () {
        return id + ":" + cortexId + ":" + token;
    }
}
