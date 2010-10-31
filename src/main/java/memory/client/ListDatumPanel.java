//
// $Id$

package memory.client;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import com.threerings.gwt.ui.Popups;
import com.threerings.gwt.ui.Widgets;
import com.threerings.gwt.util.ClickCallback;

import memory.data.Datum;
import memory.data.Type;

/**
 * Displays a list datum.
 */
public class ListDatumPanel extends DatumPanel
{
    protected void createContents ()
    {
        addTextTitle();

        final FlowPanel items = new FlowPanel();
        for (Datum child : _datum.children) {
            addItem(items, child);
        }
        add(items);

        final TextBox item = Widgets.newTextBox("", -1, 20);
        final Button add = new Button("Add");
        add(Widgets.newRow(item, add));
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
        Widget ilabel;
        switch (item.type) {
        case WIKI:
            ilabel = Widgets.newHTML(WikiUtil.formatSnippet(_cortexId, _datum, item.text));
            break;
        case HTML:
            ilabel = Widgets.newHTML(item.text);
            break;
        default:
            ilabel = Widgets.newLabel(item.text);
            break;
        }
        ilabel.addStyleName(_rsrc.styles().listItem());
        items.add(ilabel);
    }

    protected void addEditor (FlowPanel editor)
    {
        addTitleEditor(editor);
    }
}
