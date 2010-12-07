//
// $Id$

package memory.client;

import com.google.gwt.user.client.ui.HTMLPanel;

import com.threerings.gwt.ui.Widgets;
import com.threerings.gwt.util.StringUtil;

/**
 * Displays a wiki text datum.
 */
public class WikiDatumPanel extends TextDatumPanel
{
    @Override protected void addContents ()
    {
        if (StringUtil.isBlank(_datum.text)) {
            add(Widgets.newLabel("<empty>", _rsrc.styles().noitems()));
        } else {
            HTMLPanel wiki = new HTMLPanel(WikiUtil.format(_ctx.cortexId, _datum, _datum.text));
            wiki.addStyleName(_rsrc.styles().wiki());
            add(wiki);
        }
    }
}
