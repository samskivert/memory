//
// $Id$

package memory.client;

import java.util.Map;

import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;

import com.threerings.gwt.ui.FluentTable;
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

        Label wikiHelp = Widgets.newLabel("Wiki help", "actionLabel", _rsrc.styles().floatRight(),
                                          _rsrc.styles().wikiHelpLabel());
        wikiHelp.addMouseOverHandler(new MouseOverHandler() {
            public void onMouseOver (MouseOverEvent event) {
                if (_popup == null) {
                    Widget title = Widgets.newLabel(
                        "Wiki Formatting Syntax", _rsrc.styles().textTitle());
                    FluentTable contents = new FluentTable(3, 0);
                    for (String help : WIKI_HELP) {
                        contents.add().setHTML(help, _rsrc.styles().wikiExample()).
                            right().setText("→").
                            right().setHTML(WikiUtil.format(_ctx.cortexId, _datum, help),
                                            _rsrc.styles().wiki());
                    }
                    _popup = new PopupPanel(true);
                    _popup.addStyleName(_rsrc.styles().popup());
                    _popup.setWidget(
                        Widgets.newFlowPanel(_rsrc.styles().wikiHelp(), title, contents));
                }
                _popup.center();
            }
            protected PopupPanel _popup;
        });
        editor.add(wikiHelp);

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

    protected static final String[] WIKI_HELP = {
        "//italics//",
        "**bold**",
        "`code`",
        "* Bullet list\n* Second item\n** Sub item",
        "# Numbered list\n# Second item\n## Sub item",
        "Link to [[wikipage]]",
        "[[http://www.google.com/|Google]]",
        "== Large heading\n=== Medium heading\n==== Small heaing",
        "Horizontal line:\n----",
        "|=|=table|=header|\n"+
        "|a|table|row|\n"+
        "|b|table|row|\n",
        "{{{\nvoid code () {\n  // no **wiki** formatting here\n}\n}}}",
        "> quoted\n> text",
    };
}
