//
// $Id$

package memory.client;

import com.google.gwt.user.client.ui.Widget;

import com.threerings.gwt.ui.Widgets;

import memory.data.Datum;

/**
 * Displays the datum for a page.
 */
public class PageDatumPanel extends DatumPanel
{
    @Override protected void createContents ()
    {
        add(Widgets.newLabel(_datum.title, _rsrc.styles().pageTitle()));
        // TODO: support multiple columns
        for (Datum child : _datum.children) {
            Widget cpanel = DatumPanel.create(child);
            cpanel.addStyleName(_rsrc.styles().pageDatum());
            add(cpanel);
        }
    }
}
