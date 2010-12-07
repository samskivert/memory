//
// $Id$

package memory.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import com.threerings.gwt.ui.EnumListBox;
import com.threerings.gwt.ui.FluentTable;
import com.threerings.gwt.ui.NumberTextBox;
import com.threerings.gwt.ui.Popups;
import com.threerings.gwt.ui.Widgets;

import memory.data.Datum;
import memory.data.FieldValue;
import memory.data.Type;

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
        _break1Id = _meta.get(BREAK1_KEY, 0L);
        _break2Id = _meta.get(BREAK2_KEY, 0L);

        // if we're on an iPhone etc., always lay out in one column
        long break1Id, break2Id;
        if (Window.getClientWidth() < 500) {
            break1Id = break2Id = 0;
        } else {
            break1Id = _break1Id;
            break2Id = _break2Id;
        }
        int cols = countColumns(break1Id, break2Id), col = 0;

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

    protected void addChildrenEditor (FlowPanel editor)
    {
        // interface for managing existing sections
        final OrderedChildPanel kids = new OrderedChildPanel();
        int cols = countColumns(_break1Id, _break2Id);
        final RadioButton col1 = new RadioButton("cols", "1 column");
        col1.setValue(cols == 1);
        final RadioButton col2 = new RadioButton("cols", "2 columns");
        col2.setValue(cols == 2);
        final RadioButton col3 = new RadioButton("cols", "3 columns");
        col3.setValue(cols == 3);
        editor.add(Widgets.newFlowPanel(_rsrc.styles().editorTitle(),
                                        Widgets.newInlineLabel("Sections: "),
                                        col1, col2, col3));

        ValueChangeHandler<Boolean> onColsChange = new ValueChangeHandler<Boolean>() {
            public void onValueChange (ValueChangeEvent<Boolean> event) {
                if (event.getSource() == col3) {
                    _break1.getParent().setVisible(true);
                    _break2.getParent().setVisible(true);
                } else if (event.getSource() == col2) {
                    _break1.getParent().setVisible(true);
                    _break2.getParent().setVisible(false);
                } else {
                    _break1.getParent().setVisible(false);
                    _break2.getParent().setVisible(false);
                }
                kids.updateChildOrder();
            }
        };
        col1.addValueChangeHandler(onColsChange);
        col2.addValueChangeHandler(onColsChange);
        col3.addValueChangeHandler(onColsChange);

        _break1 = Widgets.newLabel("Column 2", _rsrc.styles().editorChildItem(),
                                   _rsrc.styles().textCenter());
        _break2 = Widgets.newLabel("Column 3", _rsrc.styles().editorChildItem(),
                                   _rsrc.styles().textCenter());

        for (Datum child : getOrderedChildren()) {
            addChildWidget(kids, child);
            if (_meta.get(BREAK1_KEY, 0L) == child.id) {
                kids.addItem(-1L, _break1);
            } else if (_meta.get(BREAK2_KEY, 0L) == child.id) {
                kids.addItem(-2L, _break2);
            }
        }

        // if we haven't already added our column breaks, add them now
        if (_break1.getParent() == null) {
            kids.addItem(-1L, _break1);
        }
        if (_break2.getParent() == null) {
            kids.addItem(-2L, _break2);
        }

        // adjust the visibility of the breaks based on column settings
        _break1.getParent().setVisible(_break1Id != 0L);
        _break2.getParent().setVisible(_break2Id != 0L);

        editor.add(kids);
        editor.add(Widgets.newLabel("Use the drag handle on the right to reorder sections.",
                                    _rsrc.styles().tip()));

        // interface for adding a new page section
        editor.add(Widgets.newLabel("Add section:", _rsrc.styles().editorTitle()));
        final EnumListBox<Type> type = createTypeListBox();
        final TextBox title = Widgets.newTextBox("", Datum.MAX_TITLE_LENGTH, 20);
        final Button add = new Button("Add");
        editor.add(newRow("Title:", title, type, add));

        new MClickCallback<Long>(add) {
            protected boolean callService () {
                _child = createChildDatum(type.getSelectedValue(), title.getText(), "");
                _datasvc.createDatum(_ctx.cortexId, _child, this);
                return true;
            }
            protected boolean gotResult (Long datumId) {
                _child.id = datumId;
                _datum.children.add(_child);
                addChildWidget(kids, _child);
                title.setText("");
                Popups.infoBelow(_msgs.datumCreated(), getPopupNear());
                return true;
            }
            protected Datum _child;
        };
    }

    protected void addChildWidget (OrderedChildPanel kids, Datum child)
    {
        FluentTable stable = new FluentTable(0, 2, _rsrc.styles().editorChildItem());
        stable.setWidget(0, 0, createDeleteButton(child, stable));
        stable.setText(0, 1, getTitle(child));
        stable.getFlexCellFormatter().setWidth(0, 1, "300px");
        stable.setWidget(0, 2, kids.addItem(child.id, stable));
    }

    @Override protected void childOrderUpdated (List<Long> ids, Widget trigger)
    {
        // filter out the break ids and note the changed breaks
        long prevId = 0L;
        _break1Id = _break2Id = 0L;
        for (Iterator<Long> iter = ids.iterator(); iter.hasNext(); ) {
            long nextId = iter.next();
            if (nextId == -1L && _break1.getParent().isVisible()) {
                _break1Id = prevId;
            } else if (nextId == -2 && _break2.getParent().isVisible()) {
                _break2Id = prevId;
            }
            prevId = nextId;
        }
        _meta.set(BREAK1_KEY, _break1Id);
        _meta.set(BREAK2_KEY, _break2Id);
        super.childOrderUpdated(ids, trigger);
    }

    protected static int countColumns (long break1Id, long break2Id)
    {
        return (break2Id != 0) ? 3 : ((break1Id != 0) ? 2 : 1);
    }

    protected Widget _break1, _break2;
    protected long _break1Id, _break2Id;

    protected static final String BREAK1_KEY = "break1";
    protected static final String BREAK2_KEY = "break2";

    protected static final List<Integer> COL_CHOICES = new ArrayList<Integer>();
    static { COL_CHOICES.add(1); COL_CHOICES.add(2); COL_CHOICES.add(3); }

    protected static final String[][] COL_STYLE = {
        { /* nothing for 1 column */ },
        { _rsrc.styles().column12(), _rsrc.styles().column22() },
        { _rsrc.styles().column13(), _rsrc.styles().column23(), _rsrc.styles().column33() },
    };
}
