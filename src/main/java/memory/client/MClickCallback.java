//
// $Id$

package memory.client;

import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.user.client.ui.TextBox;

import com.threerings.gwt.util.ClickCallback;

/**
 * Customizes the standard click callback with error translation.
 */
public abstract class MClickCallback<T> extends ClickCallback<T>
{
    public MClickCallback (HasClickHandlers trigger)
    {
        super(trigger);
    }

    public MClickCallback (HasClickHandlers trigger, TextBox onEnter)
    {
        super(trigger, onEnter);
    }

    @Override // from ClickCallback<T>
    protected String formatError (Throwable cause)
    {
        return Errors.formatError(cause);
    }
}
