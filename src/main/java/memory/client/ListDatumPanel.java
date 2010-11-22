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

import com.allen_sauer.gwt.dnd.client.DragEndEvent;
import com.allen_sauer.gwt.dnd.client.DragHandlerAdapter;
import com.allen_sauer.gwt.dnd.client.PickupDragController;

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
        if (autoHideAdd()) {
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

            new MClickCallback<Long>(add, _itext) {
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
                    _hideTimer.schedule(10000);
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
        if (autoHideAdd() && _itext.getText().trim().length() == 0) {
            _addui.setVisible(false);
        }
    }

    protected void addItems ()
    {
        for (Datum child : getOrderedChildren()) {
            addItem(_items, child);
        }
    }

    protected Widget addItem (FlowPanel items, Datum item)
    {
        Widget iwidget = new EditableItemLabel(item);
        items.add(iwidget);
        _noitems.setVisible(false);
        return iwidget;
    }

    protected Widget createItemWidget (Datum item)
    {
        Widget ilabel = createItemLabel(item);
        ilabel.setTitle(""+item.id);
        ilabel.addStyleName(_rsrc.styles().listItem());
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

    @Override protected void addChildrenEditor (FlowPanel editor)
    {
        // no children editor, instead we use item editors
        editor.add(Widgets.newLabel("Items:", _rsrc.styles().editorTitle()));

        final FlowPanel items = Widgets.newFlowPanel();
        editor.add(items);
        PickupDragController dragger = DnDUtil.addDnD(items, new DragHandlerAdapter() {
            public void onDragEnd (DragEndEvent event) {
                saveChildOrder(items);
            }
        });

        for (final Datum item : getOrderedChildren()) {
            final Image drag = allowChildReorder() ? DnDUtil.newDragIcon() : null;
            final Widget iedit = new ItemEditor(item, drag);
            items.add(iedit);
            if (allowChildReorder()) {
                dragger.makeDraggable(iedit, drag);
            }
        }
    }

    protected boolean allowChildReorder ()
    {
        return true;
    }

    protected void saveChildOrder (final FlowPanel items)
    {
        List<Long> ids = new ArrayList<Long>();
        for (int ii = 0, ll = items.getWidgetCount(); ii < ll; ii++) {
            ids.add(((ItemEditor)items.getWidget(ii)).item.id);
        }
        _meta.setIds(ORDER_KEY, ids);
        _datasvc.updateDatum(
            _ctx.cortexId, _datum.id, Datum.Field.META, FieldValue.of(_meta.toMetaString()),
            new MPopupCallback<Void>(items) {
            public void onSuccess (Void result) {
                Popups.infoBelow("Order updated.", items);
            }
        });
    }

    protected class EditableItemLabel extends SimplePanel
        implements HasDoubleClickHandlers
    {
        public EditableItemLabel (Datum item) {
            _item = item;
            displayItem();
        }

        // from interface HasDoubleClickHandlers
        public HandlerRegistration addDoubleClickHandler (DoubleClickHandler handler) {
            return addDomHandler(handler, DoubleClickEvent.getType());
        }

        protected void displayItem () {
            setWidget(createItemWidget(_item));
            _dcreg = addDoubleClickHandler(new DoubleClickHandler() {
                public void onDoubleClick (DoubleClickEvent event) {
                    displayEditor();
                }
            });
        }

        protected void displayEditor () {
            _dcreg.removeHandler();
            ItemEditor row = new ItemEditor(_item, null) {
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
        protected HandlerRegistration _dcreg;
    }

    protected class ItemEditor extends StretchBox
    {
        public final Datum item;
        public final Image delete;
        public final TextBox text;
        public final Button update;

        public ItemEditor (Datum eitem, Image drag) {
            item = eitem;
            delete = Widgets.newImage(_rsrc.deleteImage(), _rsrc.styles().iconButton());
            delete.setTitle("Delete item.");
            text = Widgets.newTextBox(item.text, -1, 20);
            update = new Button("Save");

            setWidgets(1, delete, text, update, drag);
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

            // wire up our delete callback
            new MClickCallback<Void>(delete) {
                protected boolean callService () {
                    _datasvc.deleteDatum(_ctx.cortexId, item.id, this);
                    return true;
                }
                protected boolean gotResult (Void result) {
                    getChildData().remove(item);
                    Popups.infoBelow("Item deleted.", getPopupNear()); // TODO: add undo?
                    removeFromParent();
                    return true;
                }
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
