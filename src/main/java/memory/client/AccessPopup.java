//
// $Id$

package memory.client;

import java.util.EnumSet;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;

import com.threerings.gwt.ui.EnumListBox;
import com.threerings.gwt.ui.FluentTable;
import com.threerings.gwt.ui.Popups;
import com.threerings.gwt.ui.Widgets;

import memory.data.Access;
import memory.data.Datum;
import memory.rpc.DataService;
import memory.rpc.DataServiceAsync;

/**
 * Displays an interface for editing access controls.
 */
public abstract class AccessPopup extends PopupPanel
{
    public static Image createAccessIcon (ClickHandler onClick, String... styleNames)
    {
        Image accessIcon = Widgets.newImage(_rsrc.accessImage(), _rsrc.styles().iconButton());
        for (String styleName : styleNames) {
            accessIcon.addStyleName(styleName);
        }
        Widgets.makeActionImage(accessIcon, _msgs.accessTip(), onClick);
        return accessIcon;
    }

    protected AccessPopup (DataService.AccessResult info)
    {
        super(true);
        addStyleName(_rsrc.styles().popup());

        final EnumListBox<Access> publicBox = new EnumListBox<Access>(
            Access.class, EnumSet.complementOf(EnumSet.of(Access.DEMO)));
        publicBox.setSelectedValue(info.publicAccess);
        publicBox.addChangeHandler(new ChangeHandler() {
            public void onChange (ChangeEvent event) {
                updatePublicAccess(
                    publicBox.getSelectedValue(), new MPopupCallback<Void>(publicBox) {
                    public void onSuccess (Void result) {
                        Popups.infoBelow("Access updated.", publicBox);
                    }
                });
            }
        });

        FlowPanel contents = new FlowPanel();
        FluentTable pubtab = new FluentTable();
        pubtab.add().setText("Share with Everyone", _rsrc.styles().textTitle()).setColSpan(2);
        pubtab.add().setText("Public access:").right().setWidget(publicBox);
        contents.add(pubtab);
        addUserAccess(contents);
        setWidget(contents);
    }

    protected abstract void updatePublicAccess (Access access, AsyncCallback<Void> callback);

    protected void addUserAccess (FlowPanel contents) {
    }

    protected static final DataServiceAsync _datasvc = GWT.create(DataService.class);
    protected static final MemoryResources _rsrc = GWT.create(MemoryResources.class);
    protected static final MemoryMessages _msgs = GWT.create(MemoryMessages.class);
}
