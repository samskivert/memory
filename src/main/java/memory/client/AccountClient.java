//
// $Id$

package memory.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.RootPanel;

import com.threerings.gwt.util.PanelCallback;

import memory.rpc.DataService;
import memory.rpc.DataServiceAsync;

/**
 * The main entry point for the account page.
 */
public class AccountClient implements EntryPoint
{
    // from interface EntryPoint
    public void onModuleLoad ()
    {
        final RootPanel client = RootPanel.get(CLIENT_DIV);
        _datasvc.loadAccountInfo(new PanelCallback<DataService.AccountResult>(client) {
            public void onSuccess (DataService.AccountResult data) {
                client.getElement().removeChild(client.getElement().getFirstChild());
                client.add(new AccountPanel(data));
            }
        });
    }

    protected static final String CLIENT_DIV = "client";

    protected static final DataServiceAsync _datasvc = GWT.create(DataService.class);
    protected static final MemoryResources _rsrc = GWT.create(MemoryResources.class);
    static {
        _rsrc.styles().ensureInjected();
    }
}
