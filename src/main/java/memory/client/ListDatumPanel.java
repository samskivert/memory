//
// $Id$

package memory.client;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import com.threerings.gwt.ui.Popups;
import com.threerings.gwt.ui.Widgets;
import com.threerings.gwt.util.ClickCallback;

import memory.data.Datum;
import memory.data.FieldValue;
import memory.data.MetaData;
import memory.data.Type;

/**
 * Displays a list datum.
 */
public class ListDatumPanel extends DatumPanel
{
    @Override protected void addTitle (FlowPanel header)
    {
        super.addTitle(header);
        if (autoHideAdd()) {
            addAddIcon(header);
        }
    }

    protected void addAddIcon (FlowPanel header)
    {
        Image add = Widgets.newImage(_rsrc.addImage(), _rsrc.styles().rightIconButton());
        header.add(Widgets.makeActionImage(add, "Add item.", new ClickHandler() {
            public void onClick (ClickEvent event) {
                _addui.setVisible(true);
                _itext.setFocus(true);
            }
        }));
    }

    @Override protected void addContents ()
    {
        // sort the data and add a metadata record for each child
        Collections.sort(getChildData(), Datum.BY_WHEN);
        for (Datum child : getChildData()) {
            _metamap.put(child.id, new MetaData(child.meta));
        }

        addItems();

        if (_ctx.canWrite()) {
            _itext = Widgets.newTextBox("", -1, 20);
            _itext.addStyleName(_rsrc.styles().width99());
            final Button add = new Button("Add");
            _addui = new StretchBox(0, _itext, add).gaps(6);
            add(_addui);
            maybeHideAddUI();

            new ClickCallback<Long>(add, _itext) {
                protected boolean callService () {
                    String text = _itext.getText().trim();
                    if (text.length() == 0) {
                        maybeHideAddUI();
                        return false;
                    }
                    _item = createChildDatum(Type.WIKI, "", text);
                    _datasvc.createDatum(_ctx.cortexId, _item, this);
                    return true;
                }
                protected boolean gotResult (Long itemId) {
                    _item.id = itemId;
                    getChildData().add(_item);
                    _metamap.put(_item.id, new MetaData(_item.meta));
                    Widget row = addItem(_items, _item);
                    _itext.setText("");
                    Popups.infoNear(_msgs.datumCreated(), row);
                    maybeHideAddUI();
                    return true;
                }
                protected Datum _item;
            };
        }
    }

    protected boolean autoHideAdd ()
    {
        return !_ctx.topLevel;
    }

    protected void maybeHideAddUI ()
    {
        if (autoHideAdd()) {
            _addui.setVisible(false);
        }
    }

    protected void addItems ()
    {
        add(_noitems = Widgets.newLabel("<no items>", _rsrc.styles().noitems()));
        _items = new FlowPanel();
        for (Datum child : getChildData()) {
            addItem(_items, child);
        }
        add(_items);
    }

    protected Widget addItem (FlowPanel items, Datum item)
    {
        Widget ilabel = createItemLabel(item);
        ilabel.setTitle(""+item.id);
        ilabel.addStyleName(_rsrc.styles().listItem());
        items.add(ilabel);
        _noitems.setVisible(false);
        return ilabel;
    }

    protected Widget createItemLabel (Datum item)
    {
        switch (item.type) {
        case WIKI:
            return Widgets.newHTML(WikiUtil.formatSnippet(_ctx.cortexId, _datum, item.text));
        case HTML:
            return Widgets.newHTML(item.text);
        default:
            return Widgets.newLabel(item.text);
        }
    }

    protected List<Datum> getChildData ()
    {
        return _datum.children;
    }

    @Override protected void addChildrenEditor (FlowPanel editor)
    {
        // no children editor, instead we use item editors
        editor.add(Widgets.newLabel("Items:", _rsrc.styles().editorTitle()));
        for (final Datum item : getChildData()) {
            final Image delete = Widgets.newImage(_rsrc.deleteImage(), _rsrc.styles().iconButton());
            delete.setTitle("Delete item.");
            final TextBox text = Widgets.newTextBox(item.text, -1, 20);
            final Button update = new Button("Save");
            final StretchBox box = new StretchBox(1, delete, text, update).gaps(9);
            editor.add(box);

            // wire up our update callback
            new ClickCallback<Void>(update, text) {
                protected boolean callService () {
                    _text = text.getText().trim();
                    _datasvc.updateDatum(_ctx.cortexId, item.id,
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

            // wire up our delete callback
            new ClickCallback<Void>(delete) {
                protected boolean callService () {
                    _datasvc.deleteDatum(_ctx.cortexId, item.id, this);
                    return true;
                }
                protected boolean gotResult (Void result) {
                    Popups.infoNear("Item deleted.", delete); // TODO: add undo?
                    getChildData().remove(item);
                    box.removeFromParent();
                    return true;
                }
            };
        }
    }

    protected Widget _noitems;
    protected TextBox _itext;
    protected Widget _addui;
    protected FlowPanel _items;
    protected Map<Long, MetaData> _metamap = new HashMap<Long, MetaData>();
}
