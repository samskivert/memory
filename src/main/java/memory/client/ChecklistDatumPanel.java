//
// $Id$

package memory.client;

import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

import com.threerings.gwt.ui.Widgets;

import memory.data.Datum;
import memory.data.MetaData;

/**
 * Displays a checklist datum.
 */
public class ChecklistDatumPanel extends ListDatumPanel
{
    protected void addItem (FlowPanel items, Datum item)
    {
        final MetaData data = new MetaData(item.meta);
        final CheckBox box = new CheckBox();
        box.addStyleName("inline");
        box.setValue(data.get("done", false));

        Widget ilabel = createItemLabel(item);
        ilabel.addStyleName("inline");
        items.add(Widgets.newFlowPanel(box, ilabel));

        // TODO: listen for checklist changes and toggle "done" metadata
    }
}
