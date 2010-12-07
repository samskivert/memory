//
// $Id$

package memory.client;

import com.google.gwt.user.client.ui.HTMLPanel;

import com.threerings.gwt.ui.Widgets;
import com.threerings.gwt.util.StringUtil;

/**
 * Displays an HTML datum.
 */
public class HTMLDatumPanel extends TextDatumPanel
{
    @Override protected void addContents ()
    {
        if (StringUtil.isBlank(_datum.text)) {
            add(Widgets.newLabel("<empty>", _rsrc.styles().noitems()));
        } else {
            add(new HTMLPanel(_datum.text));
        }
    }
}
