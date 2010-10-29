//
// $Id$

package memory.client;

import com.google.gwt.user.client.ui.HTMLPanel;

/**
 * Displays an HTML datum.
 */
public class HTMLDatumPanel extends TextDatumPanel
{
    @Override protected void createContents ()
    {
        addTextTitle();
        add(new HTMLPanel(_datum.text));
    }
}
