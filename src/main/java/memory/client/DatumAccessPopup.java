//
// $Id$

package memory.client;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

import com.threerings.gwt.util.PopupCallback;

import memory.data.Access;
import memory.data.Datum;
import memory.rpc.DataService;

/**
 * Displays an access editing interface for a datum.
 */
public class DatumAccessPopup extends AccessPopup
{
    public static void show (final Context ctx, final Datum datum, final Widget near)
    {
        _datasvc.loadAccessInfo(
            ctx.cortexId, datum.id, new PopupCallback<DataService.AccessResult>(near) {
            public void onSuccess (DataService.AccessResult access) {
                DatumAccessPopup popup = new DatumAccessPopup(ctx, datum, access);
                popup.setVisible(false);
                popup.show();
                popup.setPopupPosition(
                    near.getAbsoluteLeft() + near.getOffsetWidth() - popup.getOffsetWidth(),
                    near.getAbsoluteTop() + near.getOffsetHeight() + 5);
                popup.setVisible(true);
            }
        });
    }

    protected DatumAccessPopup (Context ctx, final Datum datum, DataService.AccessResult access)
    {
        super(access);
        _ctx = ctx;
        _datum = datum;
    }

    protected void updatePublicAccess (Access access, AsyncCallback<Void> callback)
    {
        _datasvc.updatePublicAccess(_ctx.cortexId, _datum.id, access, callback);
    }

    protected Context _ctx;
    protected Datum _datum;
}
