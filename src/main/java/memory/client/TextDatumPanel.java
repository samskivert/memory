//
// $Id$

package memory.client;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;

import com.threerings.gwt.ui.Popups;
import com.threerings.gwt.ui.Widgets;
import com.threerings.gwt.util.ClickCallback;

/**
 * A base class for text datum panels.
 */
public abstract class TextDatumPanel extends DatumPanel
{
    protected void addEditor (FlowPanel editor)
    {
        addTitleEditor(editor);
        editor.add(Widgets.newShim(5, 5));

        final TextArea text = Widgets.newTextArea(_datum.text, -1, 30);
        text.addStyleName(_rsrc.styles().stretchWide());
        editor.add(text);

        Button update = new Button("Update");
        new ClickCallback<Void>(update) {
            protected boolean callService () {
                _text = text.getText().trim();
                _datasvc.updateDatum(_datum.id, null, null, null, null, _text, null, null, this);
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
        editor.add(update);
    }
}
