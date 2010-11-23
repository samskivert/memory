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
            if (!_metamap.get(child.id).get(MetaData.DONE, false)) {
                addItem(_items, child);
            }
        }

        _doneItems = new FlowPanel();
        for (Datum child : getChildData()) { // we don't use custom order here
            if (_metamap.get(child.id).get(MetaData.DONE, false)) {
                addItem(_doneItems, child);
            }
        }
        add(_doneItems);
    }

    @Override protected Widget createItemWidget (final Datum item)
    {
        final MetaData data = _metamap.get(item.id);
        final CheckBox box = new CheckBox();
        box.setEnabled(_ctx.canOpenEditor());
        box.addStyleName("inline");
        box.setValue(data.get(MetaData.DONE, false));

        Widget ilabel = createItemLabel(item);
        ilabel.addStyleName("inline");

        final Widget row = Widgets.newFlowPanel(box, ilabel);

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
                                _items.remove(row);
                                _doneItems.add(row);
                            } else {
                                _doneItems.remove(row);
                                _items.add(row);
                            }
                        }
                    }, box));
            }
        });

        return row;
    }

    protected FlowPanel _doneItems;
}
