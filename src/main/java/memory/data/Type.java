//
// $Id$

package memory.data;

/**
 * Defines the differing types of data.
 */
public enum Type
{
    // leaf types

    /** A datum that contains markdown text. */
    MARKDOWN,
    /** A datum that contains HTML text. */
    HTML,
    /** A datum that contains a URL to content to be embedded. */
    EMBED,

    // container types

    /** A list of items that can be checked off. */
    CHECKLIST,
    /** A log of completed activities that is rolled over daily/weekly/monthly. */
    JOURNAL,
    /** A collection of data that is displayed in some number of columns. */
    PAGE;
}
