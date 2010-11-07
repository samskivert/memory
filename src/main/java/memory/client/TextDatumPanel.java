//
// $Id$

package memory.client;

import java.util.Map;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;

import com.threerings.gwt.ui.FluentTable;
import com.threerings.gwt.ui.Popups;
import com.threerings.gwt.ui.Widgets;
import com.threerings.gwt.util.ClickCallback;

import memory.data.Datum;
import memory.data.FieldValue;

/**
 * A base class for text datum panels.
 */
public abstract class TextDatumPanel extends DatumPanel
{
    @Override protected void addBitsEditors (FlowPanel editor, FluentTable bits)
    {
        super.addBitsEditors(editor, bits);

        int height = Math.max(5, Math.min(30, countNewlines(_datum.text)+2));
        final TextArea text = Widgets.newTextArea(_datum.text, -1, height);
        text.addStyleName(_rsrc.styles().width98());
        editor.add(text);

        _updaters.add(new BitsUpdater() {
            public void addUpdates (Map<Datum.Field, FieldValue> updates) {
                _text = text.getText().trim();
                updates.put(Datum.Field.TEXT, FieldValue.of(_text));
            }
            public void applyUpdates () {
                _datum.text = _text;
            }
            protected String _text;
        });
    }

    @Override protected void addChildrenEditor (FlowPanel editor)
    {
        // no children of text datum
    }

    protected static int countNewlines (String text)
    {
        int nl = 0, nlidx = -1;
        while ((nlidx = text.indexOf("\n", nlidx+1)) != -1) {
            nl++;
        }
        return nl;
    }
}
