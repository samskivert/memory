//
// $Id$

package memory.data;

import java.io.Serializable;

/**
 * Contains information on a single datum.
 */
public class Datum implements Serializable
{
    /** The unique identifier for this datum. */
    public long id;

    /** The id of this datum's parent, or 0 if it is a root datum. */
    public long parentId;

    /** Indicates the access controls for this datum. */
    public Access access;

    /** Indicates the type of this datum. */
    public Type type;

    /** Metadata for this datum. */
    public String meta;

    /** The primary contents of this datum. */
    public String text;

    /** A timestamp associated with this datum (usually when it was last modified). */
    public long when;

    /** Descendents of this datum, may be null. */
    public Datum[] children;
}
