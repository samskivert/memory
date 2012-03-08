//
// $Id$

package memory.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * A base class for popups that drop down from the header.
 */
public abstract class HeaderPopup extends PopupPanel
{
    protected HeaderPopup () {
        super(true);
        addStyleName(_rsrc.styles().popup());
    }

    protected void showNear (Widget near) {
        setVisible(false);
        show();
        setPopupPosition(near.getAbsoluteLeft() + near.getOffsetWidth() - getOffsetWidth(),
                         near.getAbsoluteTop() + near.getOffsetHeight() + 5);
        setVisible(true);
    }

    protected static final MemoryResources _rsrc = GWT.create(MemoryResources.class);
}
