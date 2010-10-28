//
// $Id$

package memory.data;

/**
 * Defines access control settings. Note that the owner can always read and write, so these
 * permissions only specify group and world access controls.
 */
public enum Access
{
    /** Groups have no access, world has no access. */
    GNONE_WNONE,
    /** Groups can read, world has no access. */
    GREAD_WNONE,
    /** Groups can write, world has no access. */
    GWRITE_WNONE,
    /** Groups can read, world can read. */
    GREAD_WREAD,
    /** Groups can write, world can read. */
    GWRITE_WREAD,
    /** Groups can write, world can write. */
    GWRITE_WWRITE;
}
