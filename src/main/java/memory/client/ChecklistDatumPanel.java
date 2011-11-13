//
// $Id$

package memory.client;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

import com.threerings.gwt.ui.Widgets;
import com.threerings.gwt.util.Callbacks;

import memory.data.Datum;
import memory.data.FieldValue;
import memory.data.MetaData;

/**
 * Displays a checklist datum.
 */
public class ChecklistDatumPanel extends ListDatumPanel
{
    @Override protected void addItems ()
    {
        for (Datum child : getOrderedChildren()) {
            MetaData data = _metamap.get(child.id);
            if (!data.get(MetaData.DONE, false)) {
                addItem(_items, child, data);
            }
        }

        _doneItems = new FlowPanel();
        for (Datum child : getChildData()) { // we don't use custom order here
            MetaData data = _metamap.get(child.id);
            if (data.get(MetaData.DONE, false)) {
                addItem(_doneItems, child, data);
            }
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

        final CheckBox box = new CheckBox();
        box.setEnabled(_ctx.canOpenEditor());
        box.addStyleName("inline");
        box.setValue(data.get(MetaData.DONE, false));
        parent.add(box);

        Widget ilabel = addItemLabel(parent, item);
        ilabel.addStyleName("inline");

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
}
