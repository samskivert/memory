//
// $Id$

package memory.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;

import com.threerings.gwt.ui.EnumListBox;
import com.threerings.gwt.ui.FluentTable;
import com.threerings.gwt.ui.Popups;
import com.threerings.gwt.util.PopupCallback;

import memory.data.Access;
import memory.data.Datum;
import memory.rpc.DataService;
import memory.rpc.DataServiceAsync;

/**
 * Displays an access editing interface for a datum.
 */
public class AccessPopup extends PopupPanel
{
    public static void show (final String cortexId, final Datum datum, final Widget near)
    {
        _datasvc.loadAccessInfo(
            cortexId, datum.id, new PopupCallback<DataService.AccessResult>(near) {
            public void onSuccess (DataService.AccessResult access) {
                AccessPopup popup = new AccessPopup(cortexId, datum, access);
                popup.setVisible(false);
                popup.show();
                popup.setPopupPosition(
                    near.getAbsoluteLeft() + near.getOffsetWidth() - popup.getOffsetWidth(),
                    near.getAbsoluteTop() + near.getOffsetHeight() + 5);
                popup.setVisible(true);
            }
        });
    }

    public AccessPopup (final String cortexId, final Datum datum, DataService.AccessResult access)
    {
        super(true);
        addStyleName(_rsrc.styles().popup());

        final EnumListBox<Access> pubAccess = new EnumListBox<Access>(Access.class);
        pubAccess.setSelectedValue(access.publicAccess);
        pubAccess.addChangeHandler(new ChangeHandler() {
            public void onChange (ChangeEvent event) {
                _datasvc.updateAccess(
                    DataService.NO_USER, cortexId, datum.id, pubAccess.getSelectedValue(),
                    new PopupCallback<Void>(pubAccess) {
                    public void onSuccess (Void result) {
                        Popups.infoNear("Access updated.", pubAccess);
                    }
                });
            }
        });

        FluentTable contents = new FluentTable();
        contents.add().setText("Public access:").right().setWidget(pubAccess);

        // TODO: individual user access
        setWidget(contents);
    }

    protected static final DataServiceAsync _datasvc = GWT.create(DataService.class);
    protected static final MemoryResources _rsrc = GWT.create(MemoryResources.class);
}
