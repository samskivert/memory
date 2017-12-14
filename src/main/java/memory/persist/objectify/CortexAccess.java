//
// $Id$

package memory.persist.objectify;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.*;

import memory.data.Access;

/**
 * Contains access control information for a cortex.
 */
@Entity
public class CortexAccess
{
    /** The user in question. */
    @Parent public Key<UserRow> userId;

    /** The id of this access row. */
    @Id public Long id;

    /** The cortex in question. */
    @Index public String cortexId;

    /** The email used to grant the user access. */
    public String email;

    /** The user's access to the datum. */
    public Access access;

    @Override // from Object
    public String toString ()
    {
        return userId.getName() + "@" + cortexId + "=" + access;
    }
}
