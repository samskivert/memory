//
// $Id$

package memory.data;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Contains sharing information.
 */
public class AccessInfo implements IsSerializable
{
    /** The id of the record containing this access information. */
    public long id;

    /** The id of the cortex in question. */
    public String cortexId;

    /** The email used to share access. */
    public String email;

    /** The access in question. */
    public Access access;

    public AccessInfo (long id, String cortexId, String email, Access access)
    {
        this.id = id;
        this.cortexId = cortexId;
        this.email = email;
        this.access = access;
    }

    public AccessInfo () {} // used for unserialization
}
