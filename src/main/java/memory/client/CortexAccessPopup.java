//
// $Id$

package memory.client;

import java.util.EnumSet;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import com.threerings.gwt.ui.EnumListBox;
import com.threerings.gwt.ui.FluentTable;
import com.threerings.gwt.ui.Popups;
import com.threerings.gwt.ui.Widgets;

import memory.data.Access;
import memory.rpc.DataService;

/**
 * Displays access editing for a cortex.
 */
public class CortexAccessPopup extends AccessPopup
{
    public static void show (final String cortexId, final Widget near) {
        _datasvc.loadAccessInfo(cortexId, new MPopupCallback<DataService.AccessResult>(near) {
            public void onSuccess (DataService.AccessResult access) {
                Popups.showBelow(new CortexAccessPopup(cortexId, access), near);
            }
        });
    }

    protected CortexAccessPopup (String cortexId, DataService.AccessResult access) {
        super(access);
        _cortexId = cortexId;
    }

    @Override protected void updatePublicAccess (Access access, AsyncCallback<Void> callback) {
        _datasvc.updateCortexPublicAccess(_cortexId, access, callback);
    }

    @Override protected void addUserAccess (FlowPanel contents) {
        final TextBox email = Widgets.newTextBox("", 256, 20);
        final EnumListBox<Access> accessBox = new EnumListBox<Access>(
            Access.class, EnumSet.complementOf(EnumSet.of(Access.DEMO, Access.NONE)));
        final Button submit = new Button("Share");

        FluentTable share = new FluentTable();
        share.add().setText("Share with One Person", _rsrc.styles().textTitle()).setColSpan(3);
        share.add().setText("Email:").right().setWidget(email).setColSpan(2);
        share.add().setText("Access:").right().setWidget(accessBox).
            right().setWidget(submit).alignRight();
        contents.add(share);

        new MClickCallback<Void>(submit) {
            protected boolean callService () {
                String addr = email.getText().trim();
                if (addr.length() == 0) return false;
                _datasvc.shareCortex(_cortexId, addr, accessBox.getSelectedValue(), this);
                return true;
            }
            protected boolean gotResult (Void result) {
                email.setText("");
                Popups.infoBelow("Share email sent!", submit);
                return true;
            }
        };
    }

    protected String _cortexId;
}
