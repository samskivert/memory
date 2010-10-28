//
// $Id$

package memory.client;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;

import com.threerings.gwt.ui.FluentTable;
import com.threerings.gwt.ui.Popups;
import com.threerings.gwt.ui.Widgets;
import com.threerings.gwt.util.ClickCallback;
import com.threerings.gwt.util.StringUtil;

/**
 * Displays an HTML datum.
 */
public class HTMLDatumPanel extends DatumPanel
{
    @Override protected void createContents ()
    {
        if (!StringUtil.isBlank(_datum.title)) {
            add(Widgets.newLabel(_rsrc.styles().textTitle()));
        }
        add(new HTMLPanel(_datum.text));
    }

    protected Widget createEditor ()
    {
        FluentTable editor = new FluentTable(0, 5, _rsrc.styles().stretchWide());

        final TextArea text = Widgets.newTextArea(_datum.text, -1, 30);
        text.addStyleName(_rsrc.styles().stretchWide());
        editor.add().setWidget(text);
        Button update = new Button("Update");
        editor.add().setWidget(update).alignRight();
        new ClickCallback<Void>(update) {
            protected boolean callService () {
                _text = text.getText().trim();
                _datasvc.updateDatum(
                    _datum.id, null, null, null, null, null, _text, null, null, this);
                return true;
            }
            protected boolean gotResult (Void result) {
                _datum.text = _text;
                Popups.infoNear(_msgs.datumUpdated(), getPopupNear());
                showContents();
                return true;
            }
            protected String _text;
        };

        return editor;
    }
}
