//
// $Id$

package memory.data;

/**
 * Defines the differing types of data.
 */
public enum Type
{
    // leaf types

    /** A datum that contains wiki formatted text. */
    WIKI(true),
    /** A datum that contains HTML text. */
    HTML(true),
    /** A datum that contains a URL to content to be embedded. */
    EMBED(false),

    // container types

    /** A list of items that can be easily extended. */
    LIST(false),
    /** Like a list but with checkboxes and auto-archival of completed items. */
    CHECKLIST(false),
    /** A log of completed activities that is rolled over daily/weekly/monthly. */
    JOURNAL(false),
    /** A collection of data that is displayed in some number of columns. */
    PAGE(false),

    // other types

    /** A type for a datum that does not yet exist. */
    NONEXISTENT(false);

    public boolean hasText ()
    {
        return _hasText;
    }

    Type (boolean hasText) {
        _hasText = hasText;
    }

    protected boolean _hasText;
}
