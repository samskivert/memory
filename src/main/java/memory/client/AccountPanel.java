//
// $Id$

package memory.client;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.http.client.URL;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import com.threerings.gwt.ui.Popups;
import com.threerings.gwt.ui.Widgets;

import memory.data.Access;
import memory.data.AccessInfo;
import memory.rpc.DataService;
import memory.rpc.DataServiceAsync;

/**
 * Displays account information and allows for cortex management.
 */
public class AccountPanel extends Composite
{
    public AccountPanel (final DataService.AccountResult data)
    {
        initWidget(_binder.createAndBindUi(this));

        _nickname.setText(data.nickname);
        _nickname.setTitle(data.userId);
        _logout.setHref(data.logoutURL);

        _name.addKeyUpHandler(new KeyUpHandler() {
            public void onKeyUp (KeyUpEvent event) {
                processName();
            }
        });
        _name.addChangeHandler(new ChangeHandler() {
            public void onChange (ChangeEvent event) {
                processName();
            }
        });

        addOwnedLinks(data.owned);
        addSharedLinks(data.shared);

        new MClickCallback<Void>(_create, _name) {
            protected boolean callService () {
                if (!processName()) {
                    return false;
                }
                _nname = _name.getText().trim();
                _datasvc.createCortex(_nname, this);
                return true;
            }
            protected boolean gotResult (Void result) {
                data.owned.add(_nname);
                addOwnedLinks(data.owned);
                Popups.infoBelow("Cortex created!", getPopupNear());
                _name.setText("");
                processName();
                return false;
            }
            protected String _nname;
        };
        _create.setEnabled(false);
    }

    protected void addOwnedLinks (List<String> cortexen)
    {
        _owned.clear();
        if (cortexen != null) {
            for (final String cortex : cortexen) {
                FlowPanel bits = new FlowPanel();
                bits.add(AccessPopup.createAccessIcon(new ClickHandler() {
                    public void onClick (ClickEvent event) {
                        CortexAccessPopup.show(cortex, (Widget)event.getSource());
                    }
                }));
                bits.add(new Anchor(cortex, "/c/" + cortex.toLowerCase()));
                _owned.add(bits);
            }
        }
        if (_owned.getWidgetCount() == 0) {
            _owned.add(Widgets.newLabel("None", _styles.none()));
        }
    }

    protected void addSharedLinks (List<AccessInfo> cortexen)
    {
        _shared.clear();
        if (cortexen != null) {
            for (AccessInfo info : cortexen) {
                _shared.add(new Anchor("/c/" + info.cortexId.toLowerCase(), info.cortexId));
            }
        }
        if (_shared.getWidgetCount() == 0) {
            _shared.add(Widgets.newLabel("None", _styles.none()));
        }
    }

    protected boolean processName ()
    {
        String cid = _name.getText().trim().toLowerCase().replace(
            ' ', '-').replaceAll("[&\\?]", "");
        _name.setText(cid);
        _create.setEnabled(cid.length() >= 4);
        if (cid.length() == 0) {
            _urltip.setText("");
            return false;
        } else if (cid.length() < 4) {
            _urltip.setText("The name must be at least 4 characters long.");
            return false;
        } else {
            _urltip.setHTML("Your cortex URL will be:<br/>\n" +
                            "http://www.sparecortex.com/c/" + URL.encode(cid));
            return true;
        }
    }

    protected interface Styles extends CssResource
    {
        String none ();
    }
    protected @UiField Styles _styles;

    protected @UiField Label _nickname;
    protected @UiField Anchor _logout;
    protected @UiField FlowPanel _owned;
    protected @UiField FlowPanel _shared;
    protected @UiField HTML _urltip;

    protected @UiField TextBox _name;
    protected @UiField Button _create;

    protected interface Binder extends UiBinder<Widget, AccountPanel> {}
    protected static final Binder _binder = GWT.create(Binder.class);
    protected static final DataServiceAsync _datasvc = GWT.create(DataService.class);
}
