//
// $Id$

package memory.client;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import com.threerings.gwt.ui.ItemListBox;
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
    @Override protected void createContents ()
    {
        add(Widgets.newLabel(_datum.title, _rsrc.styles().pageTitle()));

        // map the children by id as we need that to handle custom ordering
        Map<Long,Datum> cmap = new HashMap<Long,Datum>();
        for (Datum child : _datum.children) {
            cmap.put(child.id, child);
        }

        // support multiple columns by just splitting things up evenly for now
        int cols = _meta.get(COLS_KEY, DEF_COLS);
        int added = 0, col = 0, ccount = _datum.children.size(), cpc = ccount / cols;
        // handle overflow by putting extras in the first column
        added -= (ccount - cpc * cols);

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
                Widget cpanel = DatumPanel.create(_cortexId, child);
                cpanel.addStyleName(_rsrc.styles().pageDatum());
                column.add(cpanel);
            }
            if (++added == cpc) {
                added = 0;
                col++;
                column = null;
            }
        }

    }

    @Override protected void addEditor (FlowPanel editor)
    {
        addTitleEditor(editor);
        addChildrenEditor(editor);
        addColumnsEditor(editor);
        addOrderEditor(editor);
    }

    protected void addColumnsEditor (FlowPanel editor)
    {
        editor.add(Widgets.newLabel("Columns:", _rsrc.styles().editorTitle()));
        final ItemListBox<Integer> columns = new ItemListBox<Integer>(COL_CHOICES);
        columns.setSelectedItem(_meta.get(COLS_KEY, DEF_COLS));
        Button update = new Button("Update");
        new ClickCallback<Void>(update) {
            protected boolean callService () {
                _meta.set(COLS_KEY, columns.getSelectedItem());
                _datasvc.updateDatum(_cortexId, _datum.id,
                                     Datum.Field.META, FieldValue.of(_meta.toMetaString()), this);
                return true;
            }
            protected boolean gotResult (Void result) {
                _datum.meta = _meta.toMetaString();
                Popups.infoNear(_msgs.datumUpdated(), getPopupNear());
                return true;
            }
        };
        editor.add(Widgets.newRow(columns, update));
    }

    protected void addOrderEditor (FlowPanel editor)
    {
        editor.add(Widgets.newLabel("Order:", _rsrc.styles().editorTitle()));
        _meta.setIds("order", getChildOrder()); // turn list into string
        // captain temporary!
        final TextBox order = Widgets.newTextBox(_meta.get("order", ""), -1, 40);
        Button update = new Button("Update");
        new ClickCallback<Void>(update, order) {
            protected boolean callService () {
                _meta.set("order", order.getText().trim());
                _datasvc.updateDatum(_cortexId, _datum.id,
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

    protected static final String COLS_KEY = "columns";
    protected static final int DEF_COLS = 1;
    protected static final List<Integer> COL_CHOICES = Arrays.asList(1, 2, 3);

    protected static final String[][] COL_STYLE = {
        { /* nothing for 1 column */ },
        { _rsrc.styles().column12(), _rsrc.styles().column22() },
        { _rsrc.styles().column13(), _rsrc.styles().column23(), _rsrc.styles().column33() },
    };
}
