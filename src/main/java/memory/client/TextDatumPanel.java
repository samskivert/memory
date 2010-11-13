//
// $Id$

package memory.client;

import java.util.Map;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import com.threerings.gwt.ui.Anchor;
import com.threerings.gwt.ui.Bindings;
import com.threerings.gwt.ui.FluentTable;
import com.threerings.gwt.ui.Widgets;
import com.threerings.gwt.util.ClickCallback;
import com.threerings.gwt.util.Value;

import memory.data.Datum;
import memory.data.FieldValue;
import memory.data.Type;

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
        // add the media management interface
        editor.add(Widgets.newLabel("Media:", _rsrc.styles().editorTitle()));

        final FlowPanel media = Widgets.newFlowPanel();
        for (Datum child : _datum.children) {
            if (child.type != Type.MEDIA) {
                continue;
            }
            if (media.getWidgetCount() > 0) {
                media.add(Widgets.newInlineLabel(", "));
            }
            media.add(new Anchor(child.title, child.meta));
        }
        if (media.getWidgetCount() == 0) {
            media.add(Widgets.newInlineLabel("<no media>", _rsrc.styles().noitems()));
        }
        Value<Boolean> uploadShowing = Value.create(false);
        media.add(Widgets.newActionLabel("upload", _rsrc.styles().floatRight(),
                                         Bindings.makeToggler(uploadShowing)));
        editor.add(media);

        Widget utitle = Widgets.newLabel("Upload media:", _rsrc.styles().editorTitle());
        Bindings.bindVisible(uploadShowing, utitle);
        editor.add(utitle);

        Button upload = new Button("Upload ");
        final TextBox name = Widgets.newTextBox("", 64, 20);
        final FileUpload file = new FileUpload();
        file.setName("media");
        final FormPanel form = new FormPanel();
        form.setAction("/todo");
        form.setWidget(new FluentTable(0, 2).add().setText("Pick file:").right().
                       setWidget(file).setColSpan(2).add().setText("Name:").right().
                       setWidget(name).right().setWidget(upload).alignRight().table());
        editor.add(form);
        Bindings.bindVisible(uploadShowing, form);

        file.addChangeHandler(new ChangeHandler() {
            public void onChange (ChangeEvent event) {
                if (name.getText().trim().length() == 0) {
                    name.setText(file.getFilename());
                }
            }
        });
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
