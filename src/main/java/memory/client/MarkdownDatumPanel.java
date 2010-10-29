//
// $Id$

package memory.client;

import com.google.gwt.user.client.ui.HTMLPanel;

/**
 * Displays a Markdown datum.
 */
public class MarkdownDatumPanel extends TextDatumPanel
{
    @Override protected void createContents ()
    {
        addTextTitle();
        add(new HTMLPanel(Showdown.toHTML(_datum.text)));
    }
}
