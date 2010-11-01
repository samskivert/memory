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
        add(new HTMLPanel(WikiUtil.format(_cortexId, _datum, _datum.text)));
    }
}
