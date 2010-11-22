//
// $Id$

package memory.client;

// import java.util.MissingResourceException;

import com.google.gwt.core.client.GWT;

/**
 * Handles the translation of server errors.
 */
public class Errors
{
    /**
     * Returns a friendly string that explains the supplied error.
     */
    public static String formatError (Throwable error)
    {
        return formatError(error.getMessage());
    }

    /**
     * Returns a friendly string that explains the supplied error code.
     */
    public static String formatError (String errcode)
    {
        if ("e.in_demo_mode".equals(errcode)) {
            return _msgs.demoNoEdit();
        } else {
            return errcode;
        }
        // if (errcode.startsWith("e.")) {
        //     try {
        //         return _dmsgs.xlate(errcode.substring(2));
        //     } catch (MissingResourceException mre) {
        //         // fall through and return the raw string
        //     }
        // }
        // return errcode;
    }

    // protected static final DynamicLookup _dmsgs = GWT.create(DynamicLookup.class);
    protected static final MemoryMessages _msgs = GWT.create(MemoryMessages.class);
}
