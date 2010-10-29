//
// $Id$

package memory.client;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import com.threerings.gwt.ui.Popups;
import com.threerings.gwt.ui.Widgets;
import com.threerings.gwt.util.ClickCallback;

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

        Map<Long,Datum> cmap = new HashMap<Long,Datum>();
        for (Datum child : _datum.children) {
            cmap.put(child.id, child);
        }

        for (Long id : getChildOrder()) {
            Datum child = cmap.get(id);
            if (child != null) {
                Widget cpanel = DatumPanel.create(child);
                cpanel.addStyleName(_rsrc.styles().pageDatum());
                add(cpanel);
            }
        }
    }

    @Override protected void addEditor (FlowPanel editor)
    {
        addTitleEditor(editor);
        addChildrenEditor(editor);

        // add configuration for the order of the children
        editor.add(Widgets.newLabel("Order:", _rsrc.styles().editorTitle()));
        _meta.setIds("order", getChildOrder()); // turn list into string
        // captain temporary!
        final TextBox order = Widgets.newTextBox(_meta.get("order", ""), -1, 40);
        Button update = new Button("Update");
        new ClickCallback<Void>(update, order) {
            protected boolean callService () {
                _meta.set("order", order.getText().trim());
                _datasvc.updateDatum(_datum.id, null, null, null, _meta.toMetaString(),
                                     null, null, null, null, this);
                return true;
            }
            protected boolean gotResult (Void result) {
                _datum.meta = _meta.toMetaString();
                Popups.infoNear(_msgs.datumUpdated(), getPopupNear());
                return true;
            }
        };
        editor.add(Widgets.newRow(order, update));
    }

    protected List<Long> getChildOrder ()
    {
        List<Long> oids = _meta.getIds("order");
        for (Datum child : _datum.children) {
            if (!oids.contains(child.id)) {
                oids.add(child.id);
            }
        }
        return oids;
    }
}
