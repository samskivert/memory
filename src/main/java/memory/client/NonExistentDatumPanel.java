//
// $Id$

package memory.client;

import java.util.ArrayList;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Panel;

import com.threerings.gwt.ui.EnumListBox;
import com.threerings.gwt.ui.Widgets;

import memory.data.Datum;
import memory.data.Type;

/**
 * Displays an interface for creating a datum when a non-existent datum is encountered.
 */
public class NonExistentDatumPanel extends DatumPanel
{
    @Override protected void addEditButton (FlowPanel header) {
        // no edit button here
    }

    @Override protected void addContents () {
        add(Widgets.newLabel("This page does not exist. You can create it if you like. " +
                             "Or click the back button to return from whence you came.",
                             _rsrc.styles().nonExistNote()));
        final EnumListBox<Type> type = createTypeListBox();
        final Button create = new Button("Create");
        add(Widgets.newRow(Widgets.newLabel("Type:"), type, create));

        new MClickCallback<Long>(create) {
            protected boolean callService () {
                _datum.type = type.getSelectedValue();
                if (_datum.type.hasText()) {
                    _datum.text = "Click the button above to edit this item.";
                }
                _datasvc.createDatum(_ctx.cortexId, _datum, this);
                return true;
            }
            protected boolean gotResult (Long datumId) {
                _datum.id = datumId;
                _datum.children = new ArrayList<Datum>();
                Panel parent = (Panel)getParent();
                parent.remove(NonExistentDatumPanel.this);
                parent.add(DatumPanel.create(_ctx, _datum));
                return false;
            }
        };
    }
}
