//
// $Id$

package memory.client;

import java.util.ArrayList;
import java.util.EnumSet;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.PushButton;
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
    public static Widget create (boolean topLevel, String cortexId, Datum datum)
    {
        try { // damage control
            DatumPanel panel = createPanel(datum.type);
            panel.init(topLevel, cortexId, datum);
            return panel;
        } catch (Exception e) {
            return Widgets.newLabel("Error [id=" + datum.id + ", errror=" + e + "]");
        }
    }

    public void init (boolean topLevel, String cortexId, Datum datum)
    {
        _topLevel = topLevel;
        _cortexId = cortexId;
        _datum = datum;
        _meta = new MetaData(_datum.meta);
        showContents();
    }

    protected void showContents ()
    {
        clear();
        removeStyleName(_rsrc.styles().editor());
        addStyleName(_rsrc.styles().view());

        // this is a twisty maze of header logic; beware static analyses
        FlowPanel header = null;
        if (_topLevel || !StringUtil.isBlank(_datum.title)) {
            header = Widgets.newFlowPanel(getHeaderStyle());
            add(header);
        }
        addEditButton(header);
        if (_topLevel) {
            addNavigation(header);
        }
        addTitle(header);

        try { // more damage control
            addContents();
        } catch (Exception e) {
            add(Widgets.newLabel("Error generating contents for "+ _datum.id + "."));
            add(Widgets.newLabel(e.toString()));
        }
    }

    protected void showEditor ()
    {
        clear();
        removeStyleName(_rsrc.styles().view());
        addStyleName(_rsrc.styles().editor());
        PushButton close = createCornerButton(
            _rsrc.closeImage(), _msgs.closeTip(), new ClickHandler() {
            public void onClick (ClickEvent event) {
                showContents();
            }
        });
        close.addStyleName(_rsrc.styles().floatRight());
        add(close);
        FlowPanel editor = Widgets.newFlowPanel(_rsrc.styles().insetBox());
        addEditor(editor);
        add(editor);
    }

    protected String getHeaderStyle ()
    {
        return _rsrc.styles().textTitle();
    }

    protected void addEditButton (FlowPanel header)
    {
        PushButton button = createCornerButton(
            _rsrc.editImage(), _msgs.editTip(), new ClickHandler() {
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
        Anchor cortex = new Anchor("/c/" + _cortexId.toLowerCase(), _cortexId);
        cortex.addStyleName(_rsrc.styles().navigationLink());
        header.add(cortex);
        header.add(Widgets.newLabel(" - ", _rsrc.styles().navigationLink()));
    }

    protected void addTitle (FlowPanel header)
    {
        if (!StringUtil.isBlank(_datum.title)) {
            Widget title = Widgets.newLabel(_datum.title, _rsrc.styles().title());
            title.setTitle("ID: " + _datum.id);
            header.add(title);
        }
    }

    protected void addEditor (FlowPanel editor)
    {
        addBitsEditor(editor);
        addChildrenEditor(editor);
    }

    protected void addBitsEditor (FlowPanel editor)
    {
        final TextBox title = Widgets.newTextBox(_datum.title, Datum.MAX_TITLE_LENGTH, 20);
        title.addStyleName(_rsrc.styles().width98());
        final EnumListBox<Type> type = createTypeListBox();
        type.setSelectedValue(_datum.type);
        final NumberTextBox parentId = NumberTextBox.newIntBox(10);
        parentId.setNumber(_datum.parentId);
        Button update = new Button("Update");

        new ClickCallback<Void>(update) {
            protected boolean callService () {
                _title = title.getText().trim();
                _type = type.getSelectedValue();
                _parentId = parentId.getNumber().longValue();
                _datasvc.updateDatum(_cortexId, _datum.id,
                                     Datum.Field.TITLE, FieldValue.of(_title),
                                     Datum.Field.TYPE, FieldValue.of(_type),
                                     Datum.Field.PARENT_ID, FieldValue.of(_parentId), this);
                return true;
            }
            protected boolean gotResult (Void result) {
                Popups.infoNear(_msgs.datumUpdated(), getPopupNear());
                title.setText(_title);
                _datum.title = _title;
                _datum.parentId = _parentId;
                if (_datum.type != _type) {
                    _datum.type = _type;
                    showEditor(); // reload the editor as our type changed
                }
                return true;
            }
            protected String _title;
            protected Type _type;
            protected long _parentId;
        };

        FluentTable bits = new FluentTable(0, 5);
        bits.add().setText("Title:").right().setWidget(title).setColSpan(3);
        bits.add().setText("Type:").right().setWidget(type).
            right().setText("Parent:").right().setWidget(parentId).
            right().setWidget(update);
        editor.add(bits);
    }

    protected void addChildrenEditor (FlowPanel editor)
    {
        editor.add(Widgets.newLabel("Children:", _rsrc.styles().editorTitle()));
        final FlowPanel kids = new FlowPanel();
        for (Datum child : _datum.children) {
            addChildWidget(kids, child);
        }
        editor.add(kids);

        editor.add(Widgets.newLabel("Add child:", _rsrc.styles().editorTitle()));
        final EnumListBox<Type> type = createTypeListBox();
        final TextBox title = Widgets.newTextBox("", Datum.MAX_TITLE_LENGTH, 20);
        final Button add = new Button("Add");
        editor.add(Widgets.newRow(Widgets.newLabel("Title:"), title, type, add));

        new ClickCallback<Long>(add) {
            protected boolean callService () {
                _child = createChildDatum(type.getSelectedValue(), title.getText(), null);
                _datasvc.createDatum(_cortexId, _child, this);
                return true;
            }
            protected boolean gotResult (Long datumId) {
                _child.id = datumId;
                _datum.children.add(_child);
                addChildWidget(kids, _child);
                title.setText("");
                Popups.infoNear(_msgs.datumCreated(), getPopupNear());
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
        child.parentId = _datum.id;
        child.type = type;
        child.meta = "";
        child.title = title;
        child.text = text;
        child.when = System.currentTimeMillis();
        child.children = new ArrayList<Datum>();
        return child;
    }

    protected PushButton createCornerButton (ImageResource image, String tip, ClickHandler onClick)
    {
        PushButton button = new PushButton(new Image(image), onClick);
        button.setTitle(tip);
        button.addStyleName(_rsrc.styles().cornerButton());
        return button;
    }

    protected EnumListBox<Type> createTypeListBox ()
    {
        return new EnumListBox<Type>(
            Type.class, EnumSet.complementOf(EnumSet.of(Type.NONEXISTENT)));
    }

    protected abstract void addContents ();

    protected boolean _topLevel;
    protected String _cortexId;
    protected Datum _datum;
    protected MetaData _meta;

    protected static String getTitle (Datum datum)
    {
        return !StringUtil.isBlank(datum.title) ? datum.title :
            (StringUtil.isBlank(datum.text) ? "<no title>" :
             StringUtil.truncate(datum.text, 30, "..."));
    }

    protected static DatumPanel createPanel (Type type)
    {
        switch (type) {
        case WIKI: return new WikiDatumPanel();
        default:
        case HTML: return new HTMLDatumPanel();
        case EMBED: return new HTMLDatumPanel();
        case LIST: return new ListDatumPanel();
        case CHECKLIST: return new ChecklistDatumPanel();
        case JOURNAL: return new HTMLDatumPanel();
        case PAGE: return new PageDatumPanel();
        case NONEXISTENT: return new NonExistentDatumPanel();
        }
    }

    protected static final DataServiceAsync _datasvc = GWT.create(DataService.class);
    protected static final MemoryMessages _msgs = GWT.create(MemoryMessages.class);
    protected static final MemoryResources _rsrc = GWT.create(MemoryResources.class);
    static {
        _rsrc.styles().ensureInjected();
    }
}
