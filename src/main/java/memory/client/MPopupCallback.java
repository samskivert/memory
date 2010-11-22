//
// $Id$

package memory.client;

import com.google.gwt.user.client.ui.Widget;

import com.threerings.gwt.util.PopupCallback;

/**
 * Customizes the popup callback with error translation.
 */
public abstract class MPopupCallback<T> extends PopupCallback<T>
{
    protected MPopupCallback ()
    {
    }

    protected MPopupCallback (Widget errorNear)
    {
        super(errorNear);
    }

    @Override // from PopupCallback<T>
    protected String formatError (Throwable cause)
    {
        return Errors.formatError(cause);
    }
}
