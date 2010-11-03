//
// $Id$

package memory.client;

import memory.data.Access;

/**
 * Does something extraordinary.
 */
public class Context
{
    /** Whether or not we're the top-most datum. */
    public boolean topLevel;

    /** The id of the cortex we're displaying. */
    public String cortexId;

    /** Whether we have read or write access to this cortex. */
    public Access access;

    public Context (boolean topLevel, String cortexId, Access access)
    {
        this.topLevel = topLevel;
        this.cortexId = cortexId;
        this.access = access;
    }

    /**
     * Returns a context for a child of this context (i.e. not a top-level context).
     */
    public Context getChild ()
    {
        return topLevel ? new Context(false, cortexId, access) : this;
    }

    /**
     * Whether or not we can make modifications in this context.
     */
    public boolean canWrite ()
    {
        return access == Access.WRITE;
    }
}
