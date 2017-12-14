//
// $Id$

package memory.persist.objectify;

import com.google.common.base.MoreObjects;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.*;

import memory.data.Type;

/**
 * Represents a single datum in the Objectify store.
 */
@Entity
public class DatumRow
    implements Cloneable
{
    /** The cortex to which this datum belongs. */
    @Parent public Key<CortexRow> cortex;

    /** A unique identifier for this datum (1 or higher). */
    @Id public Long id;

    /** The id of this datum's parent, or 0 if it is a root datum. */
    @Index public long parentId;

    /** Indicates the type of this datum. */
    @Index public Type type;

    /** Metadata for this datum. */
    public String meta;

    /** The title of this datum converted to lowercase because GAE doesn't support case-insensitive
     * indexes. Sheesh. */
    @Index public String titleKey;

    /** The title of this datum. */
    public String title;

    /** The primary contents of this datum. */
    public String text;

    /** A timestamp associated with this datum (usually when it was last modified). */
    @Index public long when;

    /** True if this datum is archived, false otherwise. */
    @Index public boolean archived;

    @Override
    public DatumRow clone () {
        try {
            return (DatumRow)super.clone();
        } catch (CloneNotSupportedException cnse) {
            throw new AssertionError(cnse);
        }
    }

    @Override // from Object
    public String toString ()
    {
        return MoreObjects.toStringHelper(this).
            add("cortex", cortex.getName()).
            add("id", id).
            add("parentId", parentId).
            add("type", type).
            add("meta", meta).
            add("title", title).
            add("text#", text == null ? 0 : text.length()).
            add("when", when).
            add("archived", archived).
            toString();
    }
}
