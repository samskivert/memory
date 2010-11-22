//
// $Id$

package memory.client;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import com.threerings.gwt.ui.Anchor;
import com.threerings.gwt.ui.EnumListBox;
import com.threerings.gwt.ui.FluentTable;
import com.threerings.gwt.ui.NumberTextBox;
import com.threerings.gwt.ui.Popups;
import com.threerings.gwt.ui.Widgets;
import com.threerings.gwt.util.ClickCallback;
import com.threerings.gwt.util.StringUtil;

import memory.data.Datum;
import memory.data.DatumId;
import memory.data.FieldValue;
import memory.data.MetaData;
import memory.data.Type;
import memory.rpc.DataService;
import memory.rpc.DataServiceAsync;

/**
 * A base panel for displaying a datum.
 */
public abstract class DatumPanel extends FlowPanel
{
    public static Widget create (Context ctx, Datum datum)
    {
        try { // damage control
            DatumPanel panel = createPanel(datum.type);
            panel.init(ctx, datum);
            return panel;
        } catch (Exception e) {
            GWT.log("Error generating UI for " + datum.id, e);
            return Widgets.newLabel("Error [id=" + datum.id + ", errror=" + e + "]");
        }
    }

    public void init (Context ctx, Datum datum)
    {
        _ctx = ctx;
        _datum = datum;
        _meta = new MetaData(_datum.meta);
        showContents();
    }

    protected void showContents ()
    {
        clear();
        removeStyleName(_rsrc.styles().editor());
        addStyleName(_rsrc.styles().view());

        // if we're top-level and a writer, add an access control icon
        if (_ctx.topLevel && _ctx.canWrite()) {
            add(AccessPopup.createAccessIcon(new ClickHandler() {
                public void onClick (ClickEvent event) {
                    DatumAccessPopup.show(_ctx, _datum, (Widget)event.getSource());
                }
            }, _rsrc.styles().floatRight()));
        }

        // this is a twisty maze of header logic; beware static analyses
        FlowPanel header = null;
        if (_ctx.topLevel || !StringUtil.isBlank(_datum.title)) {
            header = Widgets.newFlowPanel(getHeaderStyle());
            add(header);
        }
        if (_ctx.canWrite()) {
            addEditButton(header);
        }
        if (_ctx.topLevel) {
            addNavigation(header);
        }
        addTitle(header);

        try { // more damage control
            addContents();
        } catch (Exception e) {
            add(Widgets.newLabel("Error generating contents for "+ _datum.id + "."));
            add(Widgets.newLabel(e.toString()));
            GWT.log("Error generating UI for " + _datum.id, e);
        }
    }

    protected void showEditor ()
    {
        clear();
        removeStyleName(_rsrc.styles().view());
        addStyleName(_rsrc.styles().editor());
        Image close = Widgets.newImage(_rsrc.closeImage(), _rsrc.styles().floatRight(),
                                       _rsrc.styles().iconButton());
        Widgets.makeActionImage(close, _msgs.closeTip(), new ClickHandler() {
            public void onClick (ClickEvent event) {
                showContents();
            }
        });
        add(close);
        FlowPanel editor = Widgets.newFlowPanel(_rsrc.styles().editorBox());
        addEditor(editor);
        add(editor);
    }

    protected String getHeaderStyle ()
    {
        return _rsrc.styles().textTitle();
    }

    protected void addEditButton (FlowPanel header)
    {
        Image button = Widgets.newImage(_rsrc.editImage(), _rsrc.styles().iconButton());
        Widgets.makeActionImage(button, _msgs.editTip(), new ClickHandler() {
            public void onClick (ClickEvent event) {
                showEditor();
            }
        });
        if (header == null) {
            button.addStyleName(_rsrc.styles().floatLeft());
            add(button);
        } else {
            header.add(button);
        }
    }

    protected void addNavigation (FlowPanel header)
    {
        String cpath = _ctx.cortexId.toLowerCase();
        header.add(newNavAnchor("/c/" + cpath, _ctx.cortexId));
        header.add(Widgets.newLabel(" - ", _rsrc.styles().navigationLink()));

        for (DatumId p : _ctx.parents) {
            if (p.parentId != 0) { // if parentId == 0, it's the root, which we added above
                header.add(newNavAnchor("/c/" + cpath + "/" + p.parentId + "/" + p.title, p.title));
                header.add(Widgets.newLabel(" - ", _rsrc.styles().navigationLink()));
            }
        }
    }

    protected Anchor newNavAnchor (String path, String text)
    {
        Anchor anchor = new Anchor(WikiUtil.appendQuery(path), text);
        anchor.addStyleName(_rsrc.styles().navigationLink());
        return anchor;
    }

    protected void addTitle (FlowPanel header)
    {
        if (!StringUtil.isBlank(_datum.title)) {
            Widget title = Widgets.newLabel(_datum.title, _rsrc.styles().title());
            title.setTitle("ID: " + _datum.id + " Meta: " + _datum.meta);
            header.add(title);
        }
    }

    protected void addEditor (FlowPanel editor)
    {
        FluentTable bits = new FluentTable(0, 1);
        editor.add(bits);
        addBitsEditors(editor, bits);

        final Type otype = _datum.type;
        Button update = new Button("Update");
        update.addStyleName(_rsrc.styles().editorUpdateButton());
        new ClickCallback<Void>(update) {
            protected boolean callService () {
                Map<Datum.Field, FieldValue> updates = new HashMap<Datum.Field, FieldValue>();
                for (BitsUpdater updater : _updaters) {
                    updater.addUpdates(updates);
                }
                _datasvc.updateDatum(_ctx.cortexId, _datum.id, updates, this);
                return true;
            }
            protected boolean gotResult (Void result) {
                for (BitsUpdater updater : _updaters) {
                    updater.applyUpdates();
                }
                showContents();
                return true;
            }
            protected String _title;
        };
        editor.add(update);

        addChildrenEditor(editor);
    }

    protected void addBitsEditors (FlowPanel editor, FluentTable bits)
    {
        addTitleEditor(bits);
        addTypeParentEditor(bits);
    }

    protected void addTitleEditor (FluentTable bits)
    {
        final TextBox title = Widgets.newTextBox(_datum.title, Datum.MAX_TITLE_LENGTH, 20);
        title.addStyleName(_rsrc.styles().width100());
        bits.add().setText("Title:", _rsrc.styles().editorLabel()).
            right().setWidget(title).setColSpan(3);

        _updaters.add(new BitsUpdater() {
            public void addUpdates (Map<Datum.Field, FieldValue> updates) {
                _title = title.getText().trim();
                updates.put(Datum.Field.TITLE, FieldValue.of(_title));
            }
            public void applyUpdates () {
                title.setText(_title);
                _datum.title = _title;
            }
            protected String _title;
        });
    }

    protected void addTypeParentEditor (FluentTable bits)
    {
        final EnumListBox<Type> type = createTypeListBox();
        type.setSelectedValue(_datum.type);
        final NumberTextBox parentId = NumberTextBox.newIntBox(10);
        parentId.addStyleName(_rsrc.styles().width100());
        parentId.setNumber(_datum.parentId);
        bits.add().setText("Type:", _rsrc.styles().editorLabel()).right().setWidget(type).
            right().setText("Parent:", _rsrc.styles().editorLabel()).right().setWidget(parentId);

        _updaters.add(new BitsUpdater() {
            public void addUpdates (Map<Datum.Field, FieldValue> updates) {
                _type = type.getSelectedValue();
                updates.put(Datum.Field.TYPE, FieldValue.of(_type));
                _parentId = parentId.getNumber().longValue();
                updates.put(Datum.Field.PARENT_ID, FieldValue.of(_parentId));
            }
            public void applyUpdates () {
                _datum.parentId = _parentId;
                _datum.type = _type;
            }
            protected Type _type;
            protected long _parentId;
        });
    }

    protected void addChildrenEditor (FlowPanel editor)
    {
        editor.add(Widgets.newLabel("Children (id):", _rsrc.styles().editorTitle()));
        final FlowPanel kids = new FlowPanel();
        for (Datum child : _datum.children) {
            addChildWidget(kids, child);
        }
        editor.add(kids);

        editor.add(Widgets.newLabel("Add child:", _rsrc.styles().editorTitle()));
        final EnumListBox<Type> type = createTypeListBox();
        final TextBox title = Widgets.newTextBox("", Datum.MAX_TITLE_LENGTH, 20);
        final Button add = new Button("Add");
        editor.add(newRow("Title:", title, type, add));

        new ClickCallback<Long>(add) {
            protected boolean callService () {
                _child = createChildDatum(type.getSelectedValue(), title.getText(), null);
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

    protected void addChildWidget (FlowPanel kids, Datum child)
    {
        kids.add(Widgets.newLabel(child.type + " (" + child.id + "): " + getTitle(child),
                                  _rsrc.styles().listItem()));
    }

    protected Datum createChildDatum (Type type, String title, String text)
    {
        Datum child = new Datum();
        child.parentId = getParentIdForChild();
        child.type = type;
        child.meta = "";
        child.title = title;
        child.text = text;
        child.when = System.currentTimeMillis();
        child.children = new ArrayList<Datum>();
        return child;
    }

    protected long getParentIdForChild ()
    {
        return _datum.id;
    }

    protected EnumListBox<Type> createTypeListBox ()
    {
        return new EnumListBox<Type>(
            Type.class, EnumSet.complementOf(EnumSet.of(Type.MEDIA, Type.NONEXISTENT)));
    }

    protected Widget newRow (String label, Widget... contents)
    {
        FlowPanel row = new FlowPanel();
        row.add(Widgets.newInlineLabel(label + " "));
        for (int ii = 0; ii < contents.length; ii++) {
            if (ii > 0) {
                row.add(Widgets.newInlineLabel(" "));
            }
            row.add(contents[ii]);
        }
        return row;
    }

    /**
     * Returns the list of children of this datum. Usually this is just what you'd expect, but for
     * a journal it's something sneaky.
     */
    protected List<Datum> getChildData ()
    {
        return _datum.children;
    }

    // used by PageDatumPanel and ListDatumPanel
    protected List<Datum> getOrderedChildren ()
    {
        // map the children by id
        Map<Long,Datum> cmap = new HashMap<Long,Datum>();
        for (Datum child : getChildData()) {
            cmap.put(child.id, child);
        }

        // put them in a new list by order
        List<Datum> ordered = new ArrayList<Datum>();
        for (Long id : _meta.getIds(ORDER_KEY)) {
            Datum child = cmap.remove(id);
            if (child != null) {
                ordered.add(child);
            }
        }

        // finally add any that weren't mapped
        for (Datum child : getChildData()) {
            if (cmap.containsKey(child.id)) {
                ordered.add(child);
            }
        }

        return ordered;
    }

    protected abstract void addContents ();

    protected Context _ctx;
    protected Datum _datum;
    protected MetaData _meta;
    protected List<BitsUpdater> _updaters = new ArrayList<BitsUpdater>();

    protected static interface BitsUpdater {
        void addUpdates (Map<Datum.Field, FieldValue> updates);
        void applyUpdates ();
    }

    protected static String getTitle (Datum datum)
    {
        return !StringUtil.isBlank(datum.title) ? datum.title :
            (StringUtil.isBlank(datum.text) ? "<no title>" :
             StringUtil.truncate(datum.text, 30, "..."));
    }

    protected static DatumPanel createPanel (Type type)
    {
        switch (type) {
        default:
        case WIKI: return new WikiDatumPanel();
        case HTML: return new HTMLDatumPanel();
        case LIST: return new ListDatumPanel();
        case CHECKLIST: return new ChecklistDatumPanel();
        case JOURNAL: return new JournalDatumPanel();
        case PAGE: return new PageDatumPanel();
        case NONEXISTENT: return new NonExistentDatumPanel();
        case LINK: return null; // should never exist (TODO: does exist right after creating?)
        }
    }

    protected static final String ORDER_KEY = "order";

    protected static final DataServiceAsync _datasvc = GWT.create(DataService.class);
    protected static final MemoryMessages _msgs = GWT.create(MemoryMessages.class);
    protected static final MemoryResources _rsrc = GWT.create(MemoryResources.class);
    static {
        _rsrc.styles().ensureInjected();
    }
}
