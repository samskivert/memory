//
// $Id$

package memory.client;

import com.google.gwt.user.client.ui.HTMLPanel;

/**
 * Displays a wiki text datum.
 */
public class WikiDatumPanel extends TextDatumPanel
{
    @Override protected void addContents ()
    {
        HTMLPanel wiki = new HTMLPanel(WikiUtil.format(_ctx.cortexId, _datum, _datum.text));
        wiki.addStyleName(_rsrc.styles().wiki());
        add(wiki);
    }
}
