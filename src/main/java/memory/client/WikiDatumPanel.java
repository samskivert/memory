//
// $Id$

package memory.client;

import com.google.gwt.user.client.ui.HTMLPanel;

import com.threerings.gwt.util.WikiParser;

/**
 * Displays a wiki text datum.
 */
public class WikiDatumPanel extends TextDatumPanel
{
    @Override protected void createContents ()
    {
        addTextTitle();
        add(new HTMLPanel(WikiParser.renderXHTML(_datum.text)));
    }
}
