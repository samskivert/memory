//
// $Id$

package memory.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import memory.rpc.DataService;
import memory.rpc.DataServiceAsync;

/**
 * Displays a share notification and allows it to be accepted.
 */
public class AcceptSharePanel extends Composite
{
    public AcceptSharePanel (final String token) {
        initWidget(_binder.createAndBindUi(this));

        _datasvc.getShareInfo(token, new MPanelCallback<DataService.ShareInfo>(_loading) {
            public void onSuccess (DataService.ShareInfo info) {
                gotShareInfo(token, info);
            }
        });
    }

    protected void gotShareInfo (final String token, final DataService.ShareInfo info) {
        _loading.addStyleName(_styles.hidden());
        _info.removeStyleName(_styles.hidden());

        _cortex.setText(info.cortex);
        _nickname.setText(info.nickname);
        _logout.setHref(info.logoutURL);

        new MClickCallback<Void>(_accept) {
            protected boolean callService () {
                _datasvc.acceptShareRequest(token, this);
                return true;
            }
            protected boolean gotResult (Void result) {
                Window.Location.replace("/c/" + info.cortex);
                return false;
            }
        };
    }

    protected interface Styles extends CssResource {
        String hidden ();
    }
    protected @UiField Styles _styles;
    protected @UiField MemoryResources _rsrc;

    protected @UiField HTMLPanel _loading, _info;
    protected @UiField Label _cortex, _nickname;
    protected @UiField Button _accept;
    protected @UiField Anchor _logout;

    protected interface Binder extends UiBinder<Widget, AcceptSharePanel> {}
    protected static final Binder _binder = GWT.create(Binder.class);
    protected static final DataServiceAsync _datasvc = GWT.create(DataService.class);
}
