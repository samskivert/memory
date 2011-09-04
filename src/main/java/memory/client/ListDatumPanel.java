//
// $Id$

package memory.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.event.dom.client.DoubleClickHandler;
import com.google.gwt.event.dom.client.HasDoubleClickHandlers;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import com.threerings.gwt.ui.EnterClickAdapter;
import com.threerings.gwt.ui.Popups;
import com.threerings.gwt.ui.Widgets;

import memory.data.Datum;
import memory.data.FieldValue;
import memory.data.MetaData;
import memory.data.Type;

/**
 * Displays a list datum.
 */
public class ListDatumPanel extends DatumPanel
{
    public ListDatumPanel ()
    {
    }

    @Override protected void addTitle (FlowPanel header)
    {
        super.addTitle(header);
        if (_ctx.canOpenEditor() && autoHideAdd()) {
            addAddIcon(header);
        }
    }

    protected void addAddIcon (FlowPanel header)
    {
        Image add = Widgets.newImage(_rsrc.addImage(), _rsrc.styles().rightIconButton());
        header.add(Widgets.makeActionImage(add, "Add item.", new ClickHandler() {
            public void onClick (ClickEvent event) {
                boolean isViz = _addui.isVisible();
                _addui.setVisible(!isViz);
                _hideTimer.cancel();
                if (!isViz) {
                    _itext.setFocus(true);
                }
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

        add(_noitems = Widgets.newLabel("<no items>", _rsrc.styles().noitems()));
        add(_items = new FlowPanel());
        addItems();

        if (_ctx.canOpenEditor()) {
            _itext = Widgets.newTextBox("", -1, 20);
            _itext.addStyleName(_rsrc.styles().width99());
            final Button add = new Button("Add");
            _addui = new StretchBox(0, _itext, add).gaps(8);
            add(_addui);
            maybeHideAddUI();

            ClickHandler onAdd = new ClickHandler() {
                public void onClick (ClickEvent event) {
                    String text = _itext.getText().trim();
                    if (text.length() == 0) {
                        maybeHideAddUI();
                        return;
                    }

                    // clear out the item text and queue hiding timer
                    _itext.setText("");
                    _hideTimer.schedule(10000);

                    // add the UI row and instruct it to create the item
                    Datum item = createChildDatum(Type.WIKI, "", text);
                    getChildData().add(item);
                    EditableItemLabel row = addItem(_items, item, new MetaData(item.meta));
                    row.createItem();
                }
            };
            add.addClickHandler(onAdd);
            EnterClickAdapter.bind(_itext, onAdd);
        }
    }

    protected boolean autoHideAdd ()
    {
        return !_ctx.topLevel;
    }

    protected void maybeHideAddUI ()
    {
        if (autoHideAdd() && _itext.getText().trim().length() == 0) {
            _addui.setVisible(false);
        }
    }

    protected void addItems ()
    {
        for (Datum child : getOrderedChildren()) {
            addItem(_items, child, _metamap.get(child.id));
        }
    }

    protected EditableItemLabel addItem (FlowPanel items, Datum item, MetaData data)
    {
        EditableItemLabel iwidget = new EditableItemLabel(item, data);
        items.add(iwidget);
        _noitems.setVisible(false);
        return iwidget;
    }

    protected Widget createItemWidget (Datum item, MetaData data)
    {
        Widget ilabel = createItemLabel(item);
        ilabel.setTitle(""+item.id);
        ilabel.addStyleName(_rsrc.styles().listItem());
        if (item.id == 0) {
            ilabel.addStyleName(_rsrc.styles().unsavedItem());
        }
        return ilabel;
    }

    protected Widget createItemLabel (Datum item)
    {
        switch (item.type) {
        case WIKI:
            return Widgets.newHTML(WikiUtil.formatSnippet(_ctx.cortexId, item, item.text));
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

        final OrderedChildPanel items = new OrderedChildPanel();
        editor.add(items);

        for (final Datum item : getOrderedChildren()) {
            ItemEditor row = new ItemEditor(item);
            Image drag = items.addItem(item.id, row);
            if (allowChildReorder()) {
                row.add(drag);
            }
        }
    }

    protected boolean allowChildReorder ()
    {
        return true;
    }

    protected class EditableItemLabel extends SimplePanel
        implements HasDoubleClickHandlers
    {
        public EditableItemLabel (Datum item, MetaData data) {
            _item = item;
            _data = data;
            displayItem();
        }

        public void createItem () {
            _datasvc.createDatum(_ctx.cortexId, _item, new MPopupCallback<Long>(this) {
                public void onSuccess (Long itemId) {
                    _item.id = itemId;
                    _metamap.put(itemId, _data);
                    displayItem(); // redisplay the item now that it's created
                }
                public void onFailure (Throwable cause) {
                    super.onFailure(cause);
                    getWidget().removeStyleName(_rsrc.styles().unsavedItem());
                    getWidget().addStyleName(_rsrc.styles().failedItem());
                    // TODO: display a UI That allows us to reinitiate the save
                }
            });
        }

        // from interface HasDoubleClickHandlers
        public HandlerRegistration addDoubleClickHandler (DoubleClickHandler handler) {
            return addDomHandler(handler, DoubleClickEvent.getType());
        }

        protected void displayItem () {
            setWidget(createItemWidget(_item, _data));
            if (_ctx.canOpenEditor() && _item.id != 0) {
                _dcreg = addDoubleClickHandler(new DoubleClickHandler() {
                    public void onDoubleClick (DoubleClickEvent event) {
                        displayEditor();
                    }
                });
            }
        }

        protected void displayEditor () {
            _dcreg.removeHandler();
            ItemEditor row = new ItemEditor(_item) {
                protected void onUpdated () {
                    displayItem();
                }
            };
            row.text.addKeyDownHandler(new KeyDownHandler() {
                public void onKeyDown (KeyDownEvent event) {
                    if (event.getNativeKeyCode() == KeyCodes.KEY_ESCAPE) {
                        displayItem();
                    }
                }
            });
            setWidget(row);
            row.text.setFocus(true);
        }

        protected Datum _item;
        protected MetaData _data;
        protected HandlerRegistration _dcreg;
    }

    protected class ItemEditor extends StretchBox
    {
        public final Datum item;
        public final TextBox text;
        public final Button update;

        public ItemEditor (Datum eitem) {
            item = eitem;
            text = Widgets.newTextBox(item.text, -1, 20);
            update = new Button("Save");

            setWidgets(1, createDeleteButton(item, this), text, update);
            gaps(9);

            // wire up our update callback
            new MClickCallback<Void>(update, text) {
                protected boolean callService () {
                    _text = text.getText().trim();
                    _datasvc.updateDatum(_ctx.cortexId, item.id,
                                         Datum.Field.TEXT, FieldValue.of(_text), this);
                    return true;
                }
                protected boolean gotResult (Void result) {
                    item.text = _text;
                    onUpdated();
                    return true;
                }
                protected String _text;
            };
        }

        protected void onUpdated () {
            Popups.infoBelow("Item updated.", this);
        }
    }

    /** Used to hide the add UI after some time has elapsed. */
    protected Timer _hideTimer = new Timer() {
        public void run () {
            maybeHideAddUI();
        }
    };

    protected Widget _noitems;
    protected TextBox _itext;
    protected Widget _addui;
    protected FlowPanel _items;
    protected Map<Long, MetaData> _metamap = new HashMap<Long, MetaData>();
}
