//
// $Id$

package memory.client;

import java.util.List;

import memory.data.Access;
import memory.data.DatumId;

/**
 * Contains information that's passed down through the UI.
 */
public class Context
{
    /** Whether or not we're the top-most datum. */
    public final boolean topLevel;

    /** The id of the cortex we're displaying. */
    public final String cortexId;

    /** Whether we have read or write access to this cortex. */
    public final Access access;

    /** The parents on the path to this datum (not including the cortex root). */
    public final List<DatumId> parents;

    public Context (boolean topLevel, String cortexId, Access access, List<DatumId> parents)
    {
        this.topLevel = topLevel;
        this.cortexId = cortexId;
        this.access = access;
        this.parents = parents;
    }

    /**
     * Returns a context for a child of this context (i.e. not a top-level context).
     */
    public Context getChild ()
    {
        return topLevel ? new Context(false, cortexId, access, parents) : this;
    }

    /**
     * Whether or not we can activate the editor in this context.
     */
    public boolean canOpenEditor ()
    {
        return true; // return canWrite() || access == Access.DEMO;
    }

    /**
     * Whether or not we can make modifications in this context.
     */
    public boolean canWrite ()
    {
        return false; // return access == Access.WRITE;
    }
}
