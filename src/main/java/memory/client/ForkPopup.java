//
// $Id$

package memory.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.TextBox;

import com.threerings.gwt.ui.FluentTable;
import com.threerings.gwt.ui.Widgets;

import memory.data.Datum;
import memory.rpc.DataService;
import memory.rpc.DataServiceAsync;

/**
 * Displays an interface for forking a page into a new cortex.
 */
public class ForkPopup extends HeaderPopup
{
    public static Image createIcon (ClickHandler onClick, String... styleNames)
    {
        Image icon = Widgets.newImage(_rsrc.forkImage(), _rsrc.styles().iconButton());
        for (String styleName : styleNames) {
            icon.addStyleName(styleName);
        }
        Widgets.makeActionImage(icon, _msgs.forkTip(), onClick);
        return icon;
    }

    public ForkPopup (Context ctx, Datum datum)
    {
        _ctx = ctx;
        _datum = datum;

        final TextBox name = Widgets.newTextBox("", 40, 20);
        HTML tip = Widgets.newHTML(_msgs.forkDismiss(), _rsrc.styles().tip());
        Button fork = new Button("Fork");
        _helper = new CortexNameHelper(name, tip, fork);

        FluentTable contents = new FluentTable();
        contents.add().setText(_msgs.forkTitle(), _rsrc.styles().textTitle()).setColSpan(3).
            add().setText(_msgs.forkDetails()).setColSpan(3).
            add().setText("New Cortex Name:").right().setWidget(name).right().setWidget(fork).
            add().setWidget(tip).setColSpan(3);
        setWidget(contents);

        new MClickCallback<Void>(fork, name) {
            protected boolean callService () {
                if (!_helper.processName()) return false;
                _nname = name.getText().trim();
                _datasvc.forkCortex(_ctx.cortexId, _datum.id, _nname, this);
                return true;
            }
            protected boolean gotResult (Void result) {
                setWidget(Widgets.newFlowPanel(
                              Widgets.newLabel(_msgs.forkCreated(), _rsrc.styles().textTitle()),
                              Widgets.newHTML(_msgs.forkVisit("/c/" + _nname))));
                return false;
            }
            protected String _nname;
        };

    }

    protected final Context _ctx;
    protected final Datum _datum;
    protected final CortexNameHelper _helper;

    protected static final DataServiceAsync _datasvc = GWT.create(DataService.class);
    protected static final MemoryMessages _msgs = GWT.create(MemoryMessages.class);
}
