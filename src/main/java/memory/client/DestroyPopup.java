//
// $Id$

package memory.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;

import com.threerings.gwt.ui.FluentTable;
import com.threerings.gwt.ui.Popups;
import com.threerings.gwt.ui.Widgets;

import memory.rpc.DataService;
import memory.rpc.DataServiceAsync;

/**
 * Displays a confirmation dialog for destroying a cortex.
 */
public class DestroyPopup extends PopupPanel
{
    public static Image createIcon (final String cortexId, final Widget cortexRow) {
        Image accessIcon = Widgets.newImage(_rsrc.deleteImage(), _rsrc.styles().iconButton());
        Widgets.makeActionImage(accessIcon, _msgs.destroyTip(), new ClickHandler() {
            public void onClick (ClickEvent event) {
                Popups.showBelow(new DestroyPopup(cortexId, cortexRow), (Widget)event.getSource());
            }
        });
        return accessIcon;
    }

    protected DestroyPopup (String cortexId, Widget cortexRow) {
        super(true);
        addStyleName(_rsrc.styles().popup());
        addStyleName(_rsrc.styles().destroyPopup());
        _cortexId = cortexId;
        _cortexRow = cortexRow;

        final Button delete = new Button("Delete");

        setWidget(new FluentTable().
                  add().setText(_msgs.destroyTitle(_cortexId), _rsrc.styles().textTitle()).
                  add().setText(_msgs.destroyDetails()).
                  add().setWidget(delete).right().
                  add().setText(_msgs.destroyDismiss(), _rsrc.styles().tip()).
                  table());

        new MClickCallback<Void>(delete) {
            protected boolean callService () {
                _datasvc.deleteCortex(_cortexId, this);
                return true;
            }
            protected boolean gotResult (Void result) {
                Popups.infoBelow(_msgs.destroyDone(), delete);
                _cortexRow.removeFromParent();
                hide();
                return true;
            }
        };

        // the click callback will have enabled delete, so now disable it and set a timer
        delete.setEnabled(false);
        new Timer() {
            public void run () {
                delete.setEnabled(true);
            }
        }.schedule(5000);
    }

    protected final String _cortexId;
    protected final Widget _cortexRow;

    protected static final MemoryResources _rsrc = GWT.create(MemoryResources.class);
    protected static final MemoryMessages _msgs = GWT.create(MemoryMessages.class);
    protected static final DataServiceAsync _datasvc = GWT.create(DataService.class);
}
