//
// $Id$

package memory.client;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import com.threerings.gwt.ui.FluentTable;
import com.threerings.gwt.ui.Popups;
import com.threerings.gwt.ui.Widgets;
import com.threerings.gwt.util.ClickCallback;

import memory.data.Datum;
import memory.data.FieldValue;
import memory.data.Type;

/**
 * Displays a list datum.
 */
public class ListDatumPanel extends DatumPanel
{
    protected void addContents ()
    {
        final FlowPanel items = new FlowPanel();
        for (Datum child : _datum.children) {
            addItem(items, child);
        }
        add(items);

        final TextBox item = Widgets.newTextBox("", -1, 20);
        item.addStyleName(_rsrc.styles().width99());
        final Button add = new Button("Add");
        add(new FluentTable(0, 0, _rsrc.styles().width100()).
            add().setWidget(item, _rsrc.styles().width100()).
            right().setWidget(add).table());

        new ClickCallback<Long>(add, item) {
            protected boolean callService () {
                _item = createChildDatum(Type.WIKI, "", item.getText().trim());
                _datasvc.createDatum(_cortexId, _item, this);
                return true;
            }
            protected boolean gotResult (Long itemId) {
                _item.id = itemId;
                _datum.children.add(_item);
                addItem(items, _item);
                item.setText("");
                Popups.infoNear(_msgs.datumCreated(), getPopupNear());
                return true;
            }
            protected Datum _item;
        };
    }

    protected void addItem (FlowPanel items, Datum item)
    {
        Widget ilabel = createItemLabel(item);
        ilabel.setTitle(""+item.id);
        ilabel.addStyleName(_rsrc.styles().listItem());
        items.add(ilabel);
    }

    protected Widget createItemLabel (Datum item)
    {
        switch (item.type) {
        case WIKI:
            return Widgets.newHTML(WikiUtil.formatSnippet(_cortexId, _datum, item.text));
        case HTML:
            return Widgets.newHTML(item.text);
        default:
            return Widgets.newLabel(item.text);
        }
    }

    @Override protected void addChildrenEditor (FlowPanel editor)
    {
        // no children editor, instead we use item editors
        editor.add(Widgets.newLabel("Items:", _rsrc.styles().editorTitle()));
        for (final Datum item : _datum.children) {
            final TextBox text = Widgets.newTextBox(item.text, -1, 20);
            Button update = new Button("Update");
            editor.add(Widgets.newRow(text, update));
            new ClickCallback<Void>(update, text) {
                protected boolean callService () {
                    _text = text.getText().trim();
                    _datasvc.updateDatum(_cortexId, item.id,
                                         Datum.Field.TEXT, FieldValue.of(_text), this);
                    return true;
                }
                protected boolean gotResult (Void result) {
                    item.text = _text;
                    Popups.infoNear("Item updated.", getPopupNear());
                    return true;
                }
                protected String _text;
            };
        }
    }
}
