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
    protected void addItem (FlowPanel items, final Datum item)
    {
        final MetaData data = new MetaData(item.meta);
        final CheckBox box = new CheckBox();
        box.addStyleName("inline");
        box.setValue(data.get(DONE, false));

        Widget ilabel = createItemLabel(item);
        ilabel.addStyleName("inline");
        items.add(Widgets.newFlowPanel(box, ilabel));

        // listen for checklist changes and toggle "done" metadata
        box.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            public void onValueChange (ValueChangeEvent<Boolean> event) {
                data.set(DONE, event.getValue());
                _datasvc.updateDatum(
                    _cortexId, item.id, Datum.Field.META, FieldValue.of(data.toMetaString()),
                    new PopupCallback<Void>(box) {
                    public void onSuccess (Void result) {
                        // nada
                    }
                });
            }
        });
    }

    protected static final String DONE = "done";
}
