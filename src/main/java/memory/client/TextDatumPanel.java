//
// $Id$

package memory.client;

import java.util.Map;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
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
import com.threerings.gwt.ui.Popups;
import com.threerings.gwt.ui.Widgets;
import com.threerings.gwt.util.Value;

import memory.data.Datum;
import memory.data.FieldValue;
import memory.data.Type;

/**
 * A base class for text datum panels.
 */
public abstract class TextDatumPanel extends DatumPanel
{
    @Override protected void addContentEditors (FlowPanel editor)
    {
        super.addContentEditors(editor);

        int height = Math.max(5, Math.min(30, countNewlines(_datum.text)+2));
        final TextArea text = Widgets.newTextArea(_datum.text, -1, height);
        text.addStyleName(_rsrc.styles().width98());
        editor.add(text);

        Value<Boolean> helpPopped = Value.create(false);
        Popups.bindPopped(helpPopped, new Popups.Thunk() {
            public PopupPanel createPopup () {
                Widget title = Widgets.newLabel(
                    "Wiki Formatting Syntax", _rsrc.styles().textTitle());
                FluentTable contents = new FluentTable(3, 0);
                for (String help : WIKI_HELP) {
                    contents.add().setHTML(help, _rsrc.styles().wikiExample()).
                        right().setText("→").
                        right().setHTML(WikiUtil.format(_ctx.cortexId, _datum, help),
                                        _rsrc.styles().wiki());
                }
                PopupPanel popup = new PopupPanel(true);
                popup.addStyleName(_rsrc.styles().popup());
                popup.setWidget(Widgets.newFlowPanel(_rsrc.styles().wikiHelp(), title, contents));
                return popup;
            }
        });
        Label wikiHelp = Widgets.newActionLabel(
            "Wiki help", _rsrc.styles().wikiHelpLabel(), Bindings.makeToggler(helpPopped));
        wikiHelp.addStyleName(_rsrc.styles().floatRight());
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

    @Override protected void addChildrenEditor (final FlowPanel editor)
    {
        // add the media management interface
        editor.add(Widgets.newLabel("Media:", _rsrc.styles().editorTitle()));
        final FlowPanel mpanel = Widgets.newFlowPanel();
        mpanel.add(_media = Widgets.newFlowPanel("inline"));
        Value<Boolean> uploadShowing = Value.create(false);
        if (_ctx.canOpenEditor()) {
            mpanel.add(Widgets.newActionLabel("upload", _rsrc.styles().floatRight(),
                                             Bindings.makeToggler(uploadShowing)));
        }
        editor.add(mpanel);

        // populate said interface with the current media
        refreshMedia();

        // add the interface shown when the user clicks 'upload'
        if (_ctx.canWrite()) {
            Widget utitle = Widgets.newLabel("Upload media:", _rsrc.styles().editorTitle());
            Bindings.bindVisible(uploadShowing, utitle);
            editor.add(utitle);

            Bindings.bindVisible(uploadShowing, new Bindings.Thunk() {
                public Widget createWidget () {
                    return createUploadUI(editor);
                }
            });
        } else {
            Widget demoup = Widgets.newLabel("Uploads not enabled for demo wiki.");
            Bindings.bindVisible(uploadShowing, demoup);
            editor.add(demoup);
        }
    }

    protected void refreshMedia ()
    {
        _media.clear();
        for (Datum child : _datum.children) {
            if (child.type != Type.MEDIA) {
                continue;
            }
            FlowPanel box = Widgets.newFlowPanel(_rsrc.styles().mediaBox());
            box.add(createDeleteButton(child, box));
            box.add(new Anchor(WikiUtil.makePath(_ctx.cortexId, _datum.id, child.title),
                               child.title));
            _media.add(box);
        }
        if (_media.getWidgetCount() == 0) {
            _media.add(Widgets.newInlineLabel("<no media>", _rsrc.styles().noitems()));
        }
    }

    protected FormPanel createUploadUI (FlowPanel editor)
    {
        final Button upload = new Button("Upload ");
        final TextBox name = Widgets.newTextBox("", 64, 20);
        name.setName("name");
        final FileUpload file = new FileUpload();
        file.setName("media");

        // these two hidden form fields will convey additional info to the upload servlet
        final TextBox cortexId = Widgets.newTextBox(_ctx.cortexId, -1, -1);
        cortexId.setName("cortexId");
        cortexId.setVisible(false);
        final TextBox parentId = Widgets.newTextBox(""+_datum.id, -1, -1);
        parentId.setName("parentId");
        parentId.setVisible(false);

        final FormPanel form = new FormPanel();
        form.setEncoding(FormPanel.ENCODING_MULTIPART);
        form.setMethod(FormPanel.METHOD_POST);
        form.setWidget(new FluentTable(0, 2).add().setText("Pick file:").
                       right().setWidget(file).setColSpan(2).
                       add().setText("Name:").right().setWidget(name).
                       right().setWidget(upload).alignRight().
                       add().setWidget(cortexId).right().setWidget(parentId).table());

        refreshUploadURL(form, upload);

        file.addChangeHandler(new ChangeHandler() {
            public void onChange (ChangeEvent event) {
                if (name.getText().trim().length() == 0) {
                    String fname = file.getFilename();
                    // work around weird WebKit? bug
                    if (fname.startsWith("C:\\fakepath\\")) fname = fname.substring(12);
                    name.setText(fname);
                }
            }
        });

        form.addSubmitHandler(new FormPanel.SubmitHandler() {
            public void onSubmit (FormPanel.SubmitEvent event) {
                String error = null;
                if (file.getFilename().trim().length() == 0) {
                    error = "Please select a file for upload.";
                } else if (name.getText().trim().length() == 0) {
                    error = "Please enter a name for the media.";
                }
                if (error != null) {
                    Popups.errorBelow(error, upload);
                    upload.setEnabled(true);
                    event.cancel();
                }
            }
        });

        form.addSubmitCompleteHandler(new FormPanel.SubmitCompleteHandler() {
            public void onSubmitComplete (FormPanel.SubmitCompleteEvent event) {
                try {
                    // parse the returned datum id and create a datum for our new media
                    Datum mdatum = new Datum();
                    mdatum.id = Long.parseLong(event.getResults().trim());
                    mdatum.parentId = _datum.id;
                    mdatum.type = Type.MEDIA;
                    mdatum.title = name.getText().trim();
                    mdatum.when = System.currentTimeMillis(); // close enough
                    _datum.children.add(mdatum);
                    refreshMedia();

                    // reset the UI
                    form.reset();
                    name.setText("");

                    // and prepare for another upload
                    refreshUploadURL(form, upload);
                    // these get cleared during the upload process, so fill them back in
                    cortexId.setText(_ctx.cortexId);
                    parentId.setText(""+_datum.id);

                } catch (Exception e) {
                    // cope with various random fucking Google weirdness
                    Popups.errorBelow(e.getMessage(), upload);
                    upload.setEnabled(true);
                }
            }
        });

        upload.addClickHandler(new ClickHandler() {
            public void onClick (ClickEvent event) {
                upload.setEnabled(false);
                form.submit();
            }
        });
        editor.add(form);

        return form;
    }

    protected void refreshUploadURL (final FormPanel form, final Button upload)
    {
        upload.setEnabled(false);
        _datasvc.getUploadURL(new MPopupCallback<String>(form) {
            public void onSuccess (String url) {
                form.setAction(url);
                upload.setEnabled(true);
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

    protected FlowPanel _media;

    protected static final String[] WIKI_HELP = {
        "//italics//",
        "**bold**",
        "`code`",
        "* Bullet list\n* Second item\n** Sub item",
        "# Numbered list\n# Second item\n## Sub item",
        "Link to [[wikipage]]",
        "[[http://www.google.com/|Google]]",
        "{{http://sparecortex.com/favicon.ico|Logo}}",
        "== Large heading\n=== Medium heading\n==== Small heading",
        "Horizontal line:\n----",
        "|=|=table|=header|\n"+
        "|a|table|row|\n"+
        "|b|table|row|\n",
        "{{{\nvoid code () {\n  // no **wiki** formatting here\n}\n}}}",
        "> quoted\n> text",
        ">>> **FLOAT**\nText or image that floats right amidst\n" +
        "other text. Use `<<<` for left floating.",
    };
}
