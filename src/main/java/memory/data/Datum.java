//
// $Id$

package memory.data;

import java.io.Serializable;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Contains information on a single datum.
 */
public class Datum implements Serializable
{
    /** Used to display a checklist in history mode. */
    public static final String HISTORY_TAG = "*";

    /** The maximum allowed length of a title. */
    public static final int MAX_TITLE_LENGTH = 256;

    /** The characters allowed to occur in tags. */
    public static Set<Character> TAG_CHARS = new HashSet<Character>();
    static {
        for (char c = '0'; c <= '9'; c++) TAG_CHARS.add(c);
        for (char c = 'a'; c <= 'z'; c++) TAG_CHARS.add(c);
        for (char c = 'A'; c <= 'Z'; c++) TAG_CHARS.add(c);
    }

    /** A comparator that sorts datum by "when", least to greatest. */
    public static final Comparator<Datum> BY_WHEN = new Comparator<Datum>() {
        public int compare (Datum one, Datum two) {
            if (one.when < two.when) {
                return -1;
            } else if (one.when > two.when) {
                return 1;
            } else {
                return 0;
            }
        }
    };

    /** An enumeration of the fields of this datum for use in updating. */
    public enum Field { PARENT_ID, TYPE, META, TITLE, TEXT, WHEN, ARCHIVED };

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

    /** Whether or not this datum has been archived (which takes it out of normal circulation). */
    public boolean archived;

    /** Descendents of this datum, may be null. */
    public List<Datum> children;

    /**
     * Extracts the tags in this datum's text into the supplied array.
     * @return this datum's text with the tags removed.
     */
    public String extractTags (Collection<String> tags) {
        return extractTags(text, tags);
    }

    protected String extractTags (String text, Collection<String> tags) {
        int hashIdx = text.lastIndexOf("#");
        if (hashIdx == -1) return text;
        // make sure that there are no spaces between the hash and eol
        for (int ii = hashIdx+1, ll = text.length(); ii < ll; ii++) {
            if (!TAG_CHARS.contains(text.charAt(ii))) return text;
        }
        // check whether there are additional tags to extract
        String rv = extractTags(text.substring(0, hashIdx).trim(), tags);
        // add our tag after the recursive check to keep things correctly ordered
        tags.add(text.substring(hashIdx+1));
        return rv;
    }
}
