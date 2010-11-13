//
// $Id$

package memory.client;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import com.threerings.gwt.ui.FluentTable;
import com.threerings.gwt.ui.ItemListBox;
import com.threerings.gwt.ui.NumberTextBox;
import com.threerings.gwt.ui.Popups;
import com.threerings.gwt.ui.Widgets;
import com.threerings.gwt.util.ClickCallback;

import memory.data.Datum;
import memory.data.FieldValue;

/**
 * Displays the datum for a page.
 */
public class PageDatumPanel extends DatumPanel
{
    @Override protected String getHeaderStyle ()
    {
        return _rsrc.styles().pageTitle();
    }

    @Override protected void addContents ()
    {
        // support multiple columns by just splitting things up evenly for now
        long break2Id = _meta.get(BREAK2_KEY, 0L);
        long break1Id = _meta.get(BREAK1_KEY, break2Id);
        if (break1Id == break2Id) {
            break2Id = 0L;
        }
        if (Window.getClientWidth() < 500) {
            break1Id = break2Id = 0; // if we're on an iPhone etc., always lay out in one column
        }
        int cols = (break2Id != 0) ? 3 : ((break1Id != 0) ? 2 : 1), col = 0;

        FlowPanel container = Widgets.newFlowPanel(_rsrc.styles().columnCont());
        FlowPanel column;
        if (cols == 1) {
            column = this;
        } else {
            add(container);
            column = null;
        }

        for (Datum child : getOrderedChildren()) {
            if (column == null) {
                column = Widgets.newFlowPanel(_rsrc.styles().column());
                column.addStyleName(COL_STYLE[cols-1][col]);
                container.add(column);
            }
            Widget cpanel = DatumPanel.create(_ctx.getChild(), child);
            cpanel.addStyleName(_rsrc.styles().pageDatum());
            column.add(cpanel);
            if (child.id == break1Id || child.id == break2Id) {
                column = null;
                col++;
            }
        }
    }

    @Override protected void addBitsEditors (FlowPanel editor, FluentTable bits)
    {
        super.addBitsEditors(editor, bits);

        final NumberTextBox break1 = NumberTextBox.newIntBox(10);
        break1.setNumber(_meta.get(BREAK1_KEY, 0L));
        final NumberTextBox break2 = NumberTextBox.newIntBox(10);
        break2.setNumber(_meta.get(BREAK2_KEY, 0L));
        bits.add().setText("Break:").right().setWidget(break1).
            right().setWidget(break2).setColSpan(2);
        bits.add().right().setText(_msgs.breakTip(), _rsrc.styles().tip()).setColSpan(3);

        // captain temporary!
        // _meta.setIds(ORDER_KEY, getChildOrder()); // turn list into string
        final TextBox order = Widgets.newTextBox(_meta.get(ORDER_KEY, ""), -1, 30);
        order.addStyleName(_rsrc.styles().width100());
        bits.add().setText("Order:").right().setWidget(order).setColSpan(3);
        bits.add().right().setText(_msgs.orderTip(), _rsrc.styles().tip()).setColSpan(3);

        _updaters.add(new BitsUpdater() {
            public void addUpdates (Map<Datum.Field, FieldValue> updates) {
                _meta.set(BREAK1_KEY, break1.getNumber().longValue());
                _meta.set(BREAK2_KEY, break2.getNumber().longValue());
                _meta.set(ORDER_KEY, order.getText().trim());
                updates.put(Datum.Field.META, FieldValue.of(_meta.toMetaString()));
            }
            public void applyUpdates () {
                _datum.meta = _meta.toMetaString();
            }
        });
    }

    protected static final String BREAK1_KEY = "break1";
    protected static final String BREAK2_KEY = "break2";

    protected static final String[][] COL_STYLE = {
        { /* nothing for 1 column */ },
        { _rsrc.styles().column12(), _rsrc.styles().column22() },
        { _rsrc.styles().column13(), _rsrc.styles().column23(), _rsrc.styles().column33() },
    };
}
