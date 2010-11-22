//
// $Id$

package memory.data;

/**
 * Defines access control settings. Note that the owner can always read and write, so these
 * permissions only specify group and world access controls.
 */
public enum Access
{
    /** No access. */
    NONE,
    /** Read access. */
    READ,
    /** Read access, write controls enabled but writes rejected. */
    DEMO,
    /** Read/write access. */
    WRITE;
}
