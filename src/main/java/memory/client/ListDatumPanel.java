//
// $Id$

package memory.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
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
    public ListDatumPanel () {
    }

    @Override protected void addTitle (FlowPanel header) {
        super.addTitle(header);

        if (_ctx.canOpenEditor() && autoHideAdd()) {
            addAddIcon(header);
        }
    }

    protected void addAddIcon (FlowPanel header) {
        Image add = Widgets.newImage(_rsrc.addImage(), _rsrc.styles().rightIconButton());
        header.add(Widgets.makeActionImage(add, "Add item.", new ClickHandler() {
            public void onClick (ClickEvent event) {
                if (_addui.isVisible()) _addui.setVisible(false);
                else if (System.currentTimeMillis() - _hideTime > HIDE_HYST)
                    showAddUIBefore(-1, null);
                _hideTimer.cancel();
            }
        }));
    }

    @Override protected void addContents () {
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
            _itext.addKeyDownHandler(new KeyDownHandler() {
                public void onKeyDown (KeyDownEvent event) {
                    if (event.getNativeKeyCode() == KeyCodes.KEY_ESCAPE) maybeHideAddUI();
                }
            });
            _itext.addBlurHandler(new BlurHandler() { public void onBlur (BlurEvent event) {
                maybeHideAddUI();
            }});
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
                    EditableItemLabel row;
                    if (_beforeIdx >= 0) {
                        row = addItemAt(_items, item, new MetaData(item.meta), _beforeIdx);
                    } else {
                        row = addItem(_items, item, new MetaData(item.meta));
                    }
                    row.createItem();
                    _itext.setFocus(true);
                }
            };
            add.addClickHandler(onAdd);
            EnterClickAdapter.bind(_itext, onAdd);
        }
    }

    protected boolean autoHideAdd () {
        return !_ctx.topLevel;
    }

    protected void maybeHideAddUI () {
        if (autoHideAdd() && _itext.getText().trim().length() == 0 && !getChildData().isEmpty()) {
            hideAddUI();
        }
    }

    protected void hideAddUI () {
        _addui.setVisible(false);
        _hideTime = System.currentTimeMillis();
    }

    protected void showAddUIBefore (long itemId, FlowPanel item) {
        _addui.removeFromParent();
        if (itemId > 0) {
            int idx = _items.getWidgetIndex(item);
            if (_beforeItemId != itemId && idx >= 0) {
                _items.insert(_addui, idx);
                _beforeItemId = itemId;
                _beforeIdx = idx;
                _addui.setVisible(true);
                _itext.setFocus(true);
                return;
            }
        }

        if (_beforeItemId != itemId) _addui.setVisible(true);
        else hideAddUI();
        _beforeItemId = 0;
        _beforeIdx = -1;
        add(_addui);
        if (_addui.isVisible()) _itext.setFocus(true);
    }

    protected void addItems () {
        for (Datum child : getOrderedChildren()) {
            addItem(_items, child, _metamap.get(child.id));
        }
    }

    protected EditableItemLabel addItem (FlowPanel items, Datum item, MetaData data) {
        return addItemAt(items, item, data, items.getWidgetCount());
    }

    protected EditableItemLabel addItemAt (FlowPanel items, Datum item, MetaData data, int idx) {
        EditableItemLabel iwidget = new EditableItemLabel(item, data);
        items.insert(iwidget, idx);
        _noitems.setVisible(false);
        return iwidget;
    }

    protected void addItemWidget (FlowPanel parent, Datum item, MetaData data) {
        Widget ilabel = addItemLabel(parent, item);
        ilabel.setTitle(""+item.id);
        if (item.id == 0) {
            parent.addStyleName(_rsrc.styles().unsavedItem());
        }
    }

    protected Widget addItemLabel (FlowPanel parent, Datum item) {
        // if the item contains tags, extract those into separate right-floating divs
        List<String> tags = new ArrayList<String>();
        String text = item.extractTags(tags);

        Widget label;
        switch (item.type) {
        case WIKI: label = Widgets.newHTML(WikiUtil.formatSnippet(_ctx.cortexId, item, text)); break;
        case HTML: label = Widgets.newHTML(text); break;
        default: label = Widgets.newLabel(text); break;
        }
        parent.add(label);

        // now add divs for the tags
        for (String tag : tags) {
            parent.add(Widgets.newLabel(tag, _rsrc.styles().itemTag()));
        }

        return label;
    }

    @Override protected void addChildrenEditor (FlowPanel editor) {
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

    protected void flushChildOrder () {
        List<Long> ids = new ArrayList<Long>();
        for (Widget w : _items) {
            if (w instanceof EditableItemLabel) ids.add(((EditableItemLabel)w).id());
        }
        childOrderUpdated(ids, null);
    }

    protected boolean allowChildReorder () {
        return true;
    }

    protected class EditableItemLabel extends FlowPanel
        implements HasClickHandlers
    {
        public EditableItemLabel (Datum item, MetaData data) {
            _item = item;
            _data = data;
            displayItem();
        }

        public long id () {
            return _item.id;
        }

        public void createItem () {
            final long beforeId = _beforeItemId;
            _datasvc.createDatum(_ctx.cortexId, _item, new MPopupCallback<Long>(this) {
                public void onSuccess (Long itemId) {
                    _item.id = itemId;
                    _metamap.put(itemId, _data);
                    // if we inserted this item before an existing item, update our order
                    if (beforeId > 0) flushChildOrder();
                    removeStyleName(_rsrc.styles().unsavedItem());
                    displayItem(); // redisplay the item now that it's created
                }
                public void onFailure (Throwable cause) {
                    super.onFailure(cause);
                    removeStyleName(_rsrc.styles().unsavedItem());
                    addStyleName(_rsrc.styles().failedItem());
                    // TODO: display a UI That allows us to reinitiate the save
                }
            });
        }

        // from interface HasMouseDownHandlers
        public HandlerRegistration addMouseDownHandler (MouseDownHandler handler) {
            return addDomHandler(handler, MouseDownEvent.getType());
        }

        // from interface HasClickHandlers
        public HandlerRegistration addClickHandler (ClickHandler handler) {
            return addDomHandler(handler, ClickEvent.getType());
        }

        protected void displayItem () {
            clear();
            addStyleName(_rsrc.styles().bulleted());
            addStyleName(_rsrc.styles().itemContainer());
            addItemWidget(this, _item, _data);
            if (_ctx.canOpenEditor() && _item.id != 0) {
                _mdreg = addMouseDownHandler(new MouseDownHandler() {
                    public void onMouseDown(MouseDownEvent event) {
                        if (!allowChildReorder()) return;
                        if (_editTimer == null) {
                            _editTimer = new Timer() { public void run () {
                                showAddUIBefore(_item.id, EditableItemLabel.this);
                            }};
                        }
                        _editTimer.schedule(500);
                    }
                });
                _creg = addClickHandler(new ClickHandler() {
                    public void onClick (ClickEvent event) {
                        // cancel our long-press show-add-box interaction
                        if (_editTimer != null) _editTimer.cancel();
                        // if they shift-clicked, show the editor
                        if (event.isShiftKeyDown()) displayEditor();
                    }
                });
            }
        }

        protected void displayEditor () {
            _creg.removeHandler();
            _mdreg.removeHandler();
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
            clear();
            removeStyleName(_rsrc.styles().bulleted());
            removeStyleName(_rsrc.styles().itemContainer());
            add(row);
            row.text.setFocus(true);
        }

        protected Datum _item;
        protected MetaData _data;
        protected Timer _editTimer;
        protected HandlerRegistration _creg, _mdreg;
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

    protected long _beforeItemId;
    protected int _beforeIdx = -1;
    protected long _hideTime;

    protected static final long HIDE_HYST = 200;
}
