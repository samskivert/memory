//
// $Id$

package memory.data;

import java.io.Serializable;
import java.util.List;

/**
 * Contains information on a single datum.
 */
public class Datum implements Serializable
{
    /** The maximum allowed length of a title. */
    public static final int MAX_TITLE_LENGTH = 256;

    /** An enumeration of the fields of this datum for use in updating. */
    public enum Field { PARENT_ID, TYPE, META, TITLE, TEXT, WHEN };

    /** The unique identifier for this datum. */
    public long id;

    /** The id of this datum's parent, or 0 if it is a root datum. */
    public long parentId;

    /** Indicates the type of this datum. */
    public Type type;

    /** Metadata for this datum. */
    public String meta;

    /** The title of this datum. */
    public String title;

    /** The primary contents of this datum (null for container types). */
    public String text;

    /** A timestamp associated with this datum (usually when it was last modified). */
    public long when;

    /** Descendents of this datum, may be null. */
    public List<Datum> children;
}
