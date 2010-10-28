//
// $Id$

package memory.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import com.threerings.gwt.ui.FluentTable;
import com.threerings.gwt.ui.Popups;
import com.threerings.gwt.ui.Widgets;
import com.threerings.gwt.util.ClickCallback;

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
        add(createEditor());
    }

    protected Widget createEditor ()
    {
        FluentTable editor = new FluentTable(0, 5);

        final TextBox title = Widgets.newTextBox(_datum.title, Datum.MAX_TITLE_LENGTH, 40);
        Button update = new Button("Update");
        editor.add().setWidget(title).right().setWidget(update);
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

        return editor;
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

    protected static DatumPanel createPanel (Type type)
    {
        switch (type) {
        case MARKDOWN: return new HTMLDatumPanel();
        default:
        case HTML: return new HTMLDatumPanel();
        case EMBED: return new HTMLDatumPanel();
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
