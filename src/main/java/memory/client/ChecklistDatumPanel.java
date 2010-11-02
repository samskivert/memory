//
// $Id$

package memory.client;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

import com.threerings.gwt.ui.Widgets;
import com.threerings.gwt.util.PopupCallback;

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
        _items = new FlowPanel();
        for (Datum child : _datum.children) {
            if (!_metamap.get(child.id).get(DONE, false)) {
                addItem(_items, child);
            }
        }
        add(_items);

        _doneItems = new FlowPanel();
        for (Datum child : _datum.children) {
            if (_metamap.get(child.id).get(DONE, false)) {
                addItem(_doneItems, child);
            }
        }
        add(_doneItems);
    }

    @Override protected Widget addItem (FlowPanel items, final Datum item)
    {
        final MetaData data = _metamap.get(item.id);
        final CheckBox box = new CheckBox();
        box.addStyleName("inline");
        box.setValue(data.get(DONE, false));

        Widget ilabel = createItemLabel(item);
        ilabel.addStyleName("inline");

        final Widget row = Widgets.newFlowPanel(box, ilabel);
        items.add(row);

        // listen for checklist changes and toggle "done" metadata
        box.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            public void onValueChange (ValueChangeEvent<Boolean> event) {
                final boolean isDone = event.getValue();
                data.set(DONE, isDone);
                final String meta = data.toMetaString();
                _datasvc.updateDatum(
                    _cortexId, item.id, Datum.Field.META, FieldValue.of(meta),
                    new PopupCallback<Void>(box) {
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
                });
            }
        });

        return box;
    }

    protected FlowPanel _doneItems;

    protected static final String DONE = "done";
}
