//
// $Id$

package memory.client;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

import com.threerings.gwt.ui.Popups;
import com.threerings.gwt.util.PopupCallback;

import memory.data.Access;
import memory.rpc.DataService;

/**
 * Displays access editing for a cortex.
 */
public class CortexAccessPopup extends AccessPopup
{
    public static void show (final String cortexId, final Widget near)
    {
        _datasvc.loadAccessInfo(cortexId, new PopupCallback<DataService.AccessResult>(near) {
            public void onSuccess (DataService.AccessResult access) {
                Popups.showBelow(new CortexAccessPopup(cortexId, access), near);
            }
        });
    }

    protected CortexAccessPopup (String cortexId, DataService.AccessResult access)
    {
        super(access);
        _cortexId = cortexId;
    }

    protected void updatePublicAccess (Access access, AsyncCallback<Void> callback)
    {
        _datasvc.updateCortexPublicAccess(_cortexId, access, callback);
    }

    protected String _cortexId;
}
