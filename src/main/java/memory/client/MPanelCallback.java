//
// $Id$

package memory.client;

import com.google.gwt.user.client.ui.Panel;

import com.threerings.gwt.util.PanelCallback;

/**
 * Customizes the panel callback with error translation.
 */
public abstract class MPanelCallback<T> extends PanelCallback<T>
{
    protected MPanelCallback (Panel panel)
    {
        super(panel);
    }

    @Override // from PanelCallback<T>
    protected String formatError (Throwable cause)
    {
        return Errors.formatError(cause);
    }
}
