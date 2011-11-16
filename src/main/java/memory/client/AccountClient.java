//
// $Id$

package memory.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

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
        String htoken = History.getToken();
        if (htoken.startsWith("S")) {
            setPanel(new AcceptSharePanel(htoken.substring(1)));

        } else {
            RootPanel client = RootPanel.get(CLIENT_DIV);
            _datasvc.loadAccountInfo(new MPanelCallback<DataService.AccountResult>(client) {
                public void onSuccess (DataService.AccountResult data) {
                    setPanel(new AccountPanel(data));
                }
            });
        }
    }

    protected void setPanel (Widget panel) {
        RootPanel client = RootPanel.get(CLIENT_DIV);
        client.getElement().removeChild(client.getElement().getFirstChild());
        client.add(panel);
    }

    protected static final String CLIENT_DIV = "client";
    protected static final DataServiceAsync _datasvc = GWT.create(DataService.class);
    protected static final MemoryResources _rsrc = GWT.create(MemoryResources.class);
    static {
        _rsrc.styles().ensureInjected();
    }
}
