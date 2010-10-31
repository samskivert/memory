//
// $Id$

package memory.persist.objectify;

import javax.persistence.Id;

import com.googlecode.objectify.annotation.Unindexed;

/**
 * Contains per-user data.
 */
public class UserRow
{
    /** The unique id for this user. */
    @Id public String id;

    /** The first time we saw this user. */
    @Unindexed public long firstSession;
}
