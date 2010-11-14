//
// $Id$

package memory.client;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import com.threerings.gwt.ui.Anchor;
import com.threerings.gwt.ui.Popups;
import com.threerings.gwt.ui.Widgets;
import com.threerings.gwt.util.ClickCallback;

import memory.data.Access;
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

        addCortexenLinks(data.cortexen.get(Access.WRITE), _readWrite);
        addCortexenLinks(data.cortexen.get(Access.READ), _readOnly);

        new ClickCallback<Void>(_create, _name) {
            protected boolean callService () {
                _nname = _name.getText().trim();
                if (_nname.length() < 4) {
                    reportFailure(new Throwable("Cortex name must be at least 4 characters long."));
                    return false;
                }
                _datasvc.createCortex(_nname, this);
                return true;
            }
            protected boolean gotResult (Void result) {
                List<String> write = data.cortexen.get(Access.WRITE);
                if (write == null) {
                    data.cortexen.put(Access.WRITE, write = new ArrayList<String>());
                }
                write.add(_nname);
                addCortexenLinks(write, _readWrite);
                Popups.infoBelow("Cortex created!", getPopupNear());
                _name.setText("");
                return true;
            }
            protected String _nname;
        };
    }

    protected void addCortexenLinks (List<String> cortexen, FlowPanel target)
    {
        target.clear();
        if (cortexen != null) {
            for (String cortex : cortexen) {
                target.add(new Anchor("/c/" + cortex.toLowerCase(), cortex));
            }
        }
        if (target.getWidgetCount() == 0) {
            target.add(Widgets.newLabel("None", _styles.none()));
        }
    }

    protected interface Styles extends CssResource
    {
        String none ();
    }
    protected @UiField Styles _styles;

    protected @UiField Label _nickname;
    protected @UiField Button _logout;
    protected @UiField FlowPanel _readWrite;
    protected @UiField FlowPanel _readOnly;

    protected @UiField TextBox _name;
    protected @UiField Button _create;

    protected interface Binder extends UiBinder<Widget, AccountPanel> {}
    protected static final Binder _binder = GWT.create(Binder.class);
    protected static final DataServiceAsync _datasvc = GWT.create(DataService.class);
}
