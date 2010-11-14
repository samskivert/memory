//
// $Id$

package memory.data;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Contains simple identifying information for a datum.
 */
public class DatumId implements IsSerializable
{
    /** The id of the parent of this datum. */
    public long parentId;

    /** The title of the datum. */
    public String title;

    public DatumId (long parentId, String title)
    {
        this.parentId = parentId;
        this.title = title;
    }

    public DatumId () {} // used for unserializing
}
