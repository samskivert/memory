//
// $Id$

package memory.persist.objectify;

import javax.persistence.Id;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Parent;
import com.googlecode.objectify.annotation.Unindexed;

import memory.data.Type;

/**
 * Represents a single datum in the Objectify store.
 */
public class DatumRow
    implements Cloneable
{
    /** The cortex to which this datum belongs. */
    @Parent public Key<CortexRow> cortex;

    /** A unique identifier for this datum (1 or higher). */
    @Id public Long id;

    /** The id of this datum's parent, or 0 if it is a root datum. */
    public long parentId;

    /** Indicates the type of this datum. */
    public Type type;

    /** Metadata for this datum. */
    @Unindexed public String meta;

    /** The title of this datum converted to lowercase because GAE doesn't support case-insensitive
     * indexes. Sheesh. */
    public String titleKey;

    /** The title of this datum. */
    @Unindexed public String title;

    /** The primary contents of this datum. */
    @Unindexed public String text;

    /** A timestamp associated with this datum (usually when it was last modified). */
    public long when;

    /** True if this datum is archived, false otherwise. */
    public boolean archived;

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
        return cortex.getName() + ":" + id + ":" + parentId;
    }
}
