//
// $Id$

package memory.client;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

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
        // map the children by id as we need that to handle custom ordering
        Map<Long,Datum> cmap = new HashMap<Long,Datum>();
        for (Datum child : _datum.children) {
            cmap.put(child.id, child);
        }

        // support multiple columns by just splitting things up evenly for now
        long break2Id = _meta.get(BREAK2_KEY, 0L);
        long break1Id = _meta.get(BREAK1_KEY, break2Id);
        if (break1Id == break2Id) {
            break2Id = 0L;
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
        for (Long id : getChildOrder()) {
            if (column == null) {
                column = Widgets.newFlowPanel(_rsrc.styles().column());
                column.addStyleName(COL_STYLE[cols-1][col]);
                container.add(column);
            }
            Datum child = cmap.get(id);
            if (child != null) {
                Widget cpanel = DatumPanel.create(_ctx.getChild(), child);
                cpanel.addStyleName(_rsrc.styles().pageDatum());
                column.add(cpanel);
                if (id == break1Id || id == break2Id) {
                    column = null;
                    col++;
                }
            }
        }
    }

    @Override protected void addEditor (FlowPanel editor)
    {
        super.addEditor(editor);
        addColumnsEditor(editor);
        addOrderEditor(editor);
    }

    protected void addColumnsEditor (FlowPanel editor)
    {
        editor.add(Widgets.newLabel("Break after:", _rsrc.styles().editorTitle()));
        final NumberTextBox break1 = NumberTextBox.newIntBox(10);
        break1.setNumber(_meta.get(BREAK1_KEY, 0L));
        final NumberTextBox break2 = NumberTextBox.newIntBox(10);
        break2.setNumber(_meta.get(BREAK2_KEY, 0L));
        Button update = new Button("Update");
        new ClickCallback<Void>(update) {
            protected boolean callService () {
                _meta.set(BREAK1_KEY, break1.getNumber().longValue());
                _meta.set(BREAK2_KEY, break2.getNumber().longValue());
                _datasvc.updateDatum(_ctx.cortexId, _datum.id,
                                     Datum.Field.META, FieldValue.of(_meta.toMetaString()), this);
                return true;
            }
            protected boolean gotResult (Void result) {
                _datum.meta = _meta.toMetaString();
                Popups.infoNear(_msgs.datumUpdated(), getPopupNear());
                return true;
            }
        };
        editor.add(Widgets.newRow(break1, break2, update));
        editor.add(Widgets.newLabel(_msgs.breakTip()));
    }

    protected void addOrderEditor (FlowPanel editor)
    {
        editor.add(Widgets.newLabel("Order:", _rsrc.styles().editorTitle()));
        _meta.setIds(ORDER_KEY, getChildOrder()); // turn list into string
        // captain temporary!
        final TextBox order = Widgets.newTextBox(_meta.get(ORDER_KEY, ""), -1, 40);
        Button update = new Button("Update");
        new ClickCallback<Void>(update, order) {
            protected boolean callService () {
                _meta.set(ORDER_KEY, order.getText().trim());
                _datasvc.updateDatum(_ctx.cortexId, _datum.id,
                                     Datum.Field.META, FieldValue.of(_meta.toMetaString()), this);
                return true;
            }
            protected boolean gotResult (Void result) {
                _datum.meta = _meta.toMetaString();
                Popups.infoNear(_msgs.datumUpdated(), getPopupNear());
                return true;
            }
        };
        editor.add(Widgets.newRow(order, update));
        editor.add(Widgets.newLabel(_msgs.orderTip()));
    }

    protected List<Long> getChildOrder ()
    {
        List<Long> oids = _meta.getIds(ORDER_KEY);
        for (Datum child : _datum.children) {
            if (!oids.contains(child.id)) {
                oids.add(child.id);
            }
        }
        return oids;
    }

    protected static final String BREAK1_KEY = "break1";
    protected static final String BREAK2_KEY = "break2";

    // protected static final String COLS_KEY = "columns";
    // protected static final int DEF_COLS = 1;
    // protected static final List<Integer> COL_CHOICES = Arrays.asList(1, 2, 3);

    protected static final String ORDER_KEY = "order";

    protected static final String[][] COL_STYLE = {
        { /* nothing for 1 column */ },
        { _rsrc.styles().column12(), _rsrc.styles().column22() },
        { _rsrc.styles().column13(), _rsrc.styles().column23(), _rsrc.styles().column33() },
    };
}
