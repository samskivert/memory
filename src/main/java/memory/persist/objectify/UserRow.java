//
// $Id$

package memory.persist.objectify;

import com.googlecode.objectify.annotation.*;

/**
 * Contains per-user data.
 */
@Entity
public class UserRow
{
    /** The unique id for this user. */
    @Id public String id;

    /** The first time we saw this user. */
    public long firstSession;
}
