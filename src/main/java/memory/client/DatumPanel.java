//
// $Id$

package memory.client;

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

import com.threerings.gwt.ui.EnumListBox;
import com.threerings.gwt.ui.FluentTable;
import com.threerings.gwt.ui.Popups;
import com.threerings.gwt.ui.Widgets;
import com.threerings.gwt.util.ClickCallback;
import com.threerings.gwt.util.StringUtil;

import memory.data.Datum;
import memory.data.Type;
import memory.rpc.DataService;
import memory.rpc.DataServiceAsync;

/**
 * A base panel for displaying a datum.
 */
public abstract class DatumPanel extends FlowPanel
{
    public static DatumPanel create (Datum datum)
    {
        DatumPanel panel = createPanel(datum.type);
        panel.init(datum);
        return panel;
    }

    public void init (Datum datum)
    {
        _datum = datum;
        showContents();
    }

    protected void showContents ()
    {
        clear();
        removeStyleName(_rsrc.styles().editor());
        addStyleName(_rsrc.styles().view());
        add(createCornerButton(_rsrc.editImage(), _msgs.editTip(), new ClickHandler() {
            public void onClick (ClickEvent event) {
                showEditor();
            }
        }));
        createContents();
    }

    protected void addTextTitle ()
    {
        if (!StringUtil.isBlank(_datum.title)) {
            add(Widgets.newLabel(_datum.title, _rsrc.styles().textTitle()));
        }
    }

    protected void showEditor ()
    {
        clear();
        removeStyleName(_rsrc.styles().view());
        addStyleName(_rsrc.styles().editor());
        add(createCornerButton(_rsrc.closeImage(), _msgs.closeTip(), new ClickHandler() {
            public void onClick (ClickEvent event) {
                showContents();
            }
        }));
        FlowPanel editor = Widgets.newFlowPanel(_rsrc.styles().insetBox());
        addEditor(editor);
        add(editor);
    }

    protected void addEditor (FlowPanel editor)
    {
        addTitleEditor(editor);
        addChildrenEditor(editor);
    }

    protected void addTitleEditor (FlowPanel editor)
    {
        final TextBox title = Widgets.newTextBox(_datum.title, Datum.MAX_TITLE_LENGTH, 20);
        Button update = new Button("Update");
        new ClickCallback<Void>(update, title) {
            protected boolean callService () {
                _title = title.getText().trim();
                _datasvc.updateDatum(
                    _datum.id, null, null, null, null, _title, null, null, null, this);
                return true;
            }
            protected boolean gotResult (Void result) {
                title.setText(_title);
                _datum.title = _title;
                Popups.infoNear(_msgs.datumUpdated(), getPopupNear());
                return true;
            }
            protected String _title;
        };

        editor.add(Widgets.newRow(Widgets.newLabel("Title:"), title, update));
    }

    protected void addChildrenEditor (FlowPanel editor)
    {
        editor.add(Widgets.newLabel("Children:", _rsrc.styles().editorTitle()));
        final FlowPanel kids = new FlowPanel();
        for (Datum child : _datum.children) {
            kids.add(Widgets.newLabel(getTitle(child)));
        }
        editor.add(kids);

        editor.add(Widgets.newLabel("Add child:", _rsrc.styles().editorTitle()));
        FluentTable table = new FluentTable(0, 5);
        final EnumListBox<Type> type = new EnumListBox<Type>(Type.class);
        table.add().setText("Type:").right().setWidget(type);
        final TextBox title = Widgets.newTextBox("", Datum.MAX_TITLE_LENGTH, 20);
        table.add().setText("Title:").right().setWidget(title);
        final Button add = new Button("Add");
        table.add().right().setWidget(add);
        editor.add(table);

        new ClickCallback<Long>(add) {
            protected boolean callService () {
                _child = createChildDatum(type.getSelectedValue(), title.getText(), null);
                _datasvc.createDatum(_child, this);
                return true;
            }
            protected boolean gotResult (Long datumId) {
                _child.id = datumId;
                _datum.children.add(_child);
                kids.add(Widgets.newLabel(getTitle(_child)));
                title.setText("");
                Popups.infoNear(_msgs.datumCreated(), getPopupNear());
                return true;
            }
            protected Datum _child;
        };
    }

    protected Datum createChildDatum (Type type, String title, String text)
    {
        Datum child = new Datum();
        child.parentId = _datum.id;
        child.type = type;
        child.access = _datum.access;
        child.meta = "";
        child.title = title;
        child.text = text;
        child.when = System.currentTimeMillis();
        return child;
    }

    protected PushButton createCornerButton (ImageResource image, String tip, ClickHandler onClick)
    {
        PushButton button = new PushButton(new Image(image), onClick);
        button.setTitle(tip);
        button.addStyleName(_rsrc.styles().cornerButton());
        return button;
    }

    protected abstract void createContents ();

    protected Datum _datum;

    protected static String getTitle (Datum datum)
    {
        return StringUtil.isBlank(datum.title) ? "<no title>" : datum.title;
    }

    protected static DatumPanel createPanel (Type type)
    {
        switch (type) {
        case WIKI: return new WikiDatumPanel();
        default:
        case HTML: return new HTMLDatumPanel();
        case EMBED: return new HTMLDatumPanel();
        case LIST: return new ListDatumPanel();
        case CHECKLIST: return new HTMLDatumPanel();
        case JOURNAL: return new HTMLDatumPanel();
        case PAGE: return new PageDatumPanel();
        }
    }

    protected static final DataServiceAsync _datasvc = GWT.create(DataService.class);
    protected static final MemoryMessages _msgs = GWT.create(MemoryMessages.class);
    protected static final MemoryResources _rsrc = GWT.create(MemoryResources.class);
    static {
        _rsrc.styles().ensureInjected();
    }
}
