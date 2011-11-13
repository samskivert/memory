//
// $Id$

package memory.client;

import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

import com.threerings.gwt.ui.Widgets;
import com.threerings.gwt.util.Callbacks;
import com.threerings.gwt.util.DateUtil;

import memory.data.Datum;
import memory.data.FieldValue;
import memory.data.MetaData;

/**
 * Displays a checklist datum.
 */
public class ChecklistDatumPanel extends ListDatumPanel
{
    @Override protected void addTitle (FlowPanel header)
    {
        super.addTitle(header);

        // if we're not already in history mode, add a history link
        if (!Window.Location.getHref().endsWith(Datum.HISTORY_TAG)) {
            String hurl = "/c/" + _ctx.cortexId.toLowerCase() + "/" + _datum.id + Datum.HISTORY_TAG;
            Widget history = createImageAnchor(hurl, _msgs.helpTip(), _rsrc.pickdateImage());
            history.addStyleName(_rsrc.styles().rightIconButton());
            header.add(history);
        }
    }

    @Override protected void addItems ()
    {
        List<Datum> children = getOrderedChildren();
        for (Iterator<Datum> iter = children.iterator(); iter.hasNext(); ) {
            Datum child = iter.next();
            MetaData data = _metamap.get(child.id);
            if (!data.get(MetaData.DONE, false)) {
                addItem(_items, child, data);
                iter.remove();
            }
        }

        // sort the done items from oldest to newest
        Collections.sort(children, Datum.BY_WHEN);
        Collections.reverse(children);

        _doneItems = new FlowPanel();
        int archiveYear = 0, archiveMonth = 0;
        for (Datum child : children) {
            MetaData data = _metamap.get(child.id);
            if (!data.get(MetaData.DONE, false)) continue;
            // if this datum is archived, check whether we should add an archive header before it;
            // we'll add an archive header for each month of archived items
            if (child.archived) {
                Date when = new Date(child.when);
                int year = DateUtil.getYear(when), month = DateUtil.getMonth(when);
                if (year != archiveYear || month != archiveMonth) {
                    _doneItems.add(Widgets.newLabel(_afmt.format(when), _rsrc.styles().textTitle()));
                    archiveYear = year;
                    archiveMonth = month;
                }
            }
            addItem(_doneItems, child, data);
        }
        add(_doneItems);
    }

    @Override protected void addItemWidget (
        final FlowPanel parent, final Datum item, final MetaData data)
    {
        // if the item is not yet saved, just display the label
        if (item.id == 0) {
            super.addItemWidget(parent, item, data);
            return;
        }

        // we're adding a checkbox, so we remove the bullet
        parent.removeStyleName(_rsrc.styles().bulleted());

        final CheckBox box = new CheckBox();
        box.setEnabled(_ctx.canOpenEditor() && !item.archived);
        box.addStyleName("inline");
        box.setValue(data.get(MetaData.DONE, false));
        parent.add(box);

        Widget ilabel = addItemLabel(parent, item);

        // listen for checklist changes and toggle "done" metadata
        box.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            public void onValueChange (ValueChangeEvent<Boolean> event) {
                final boolean isDone = event.getValue();
                data.set(MetaData.DONE, isDone);
                final String meta = data.toMetaString();
                _datasvc.updateDatum(
                    _ctx.cortexId, item.id, Datum.Field.META, FieldValue.of(meta),
                    Callbacks.disabler(new MPopupCallback<Void>(box) {
                        public void onSuccess (Void result) {
                            item.meta = meta;
                            if (isDone) {
                                _items.remove(parent);
                                _doneItems.add(parent);
                            } else {
                                _doneItems.remove(parent);
                                _items.add(parent);
                            }
                        }
                    }, box));
            }
        });
    }

    protected FlowPanel _doneItems;
    protected static final DateTimeFormat _afmt = DateTimeFormat.getFormat("MMMM yyyy");
}
