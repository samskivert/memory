//
// $Id$

package memory.client;

import java.util.Comparator;
import java.util.Date;
import java.util.List;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.datepicker.client.DatePicker;

import com.threerings.gwt.ui.Popups;
import com.threerings.gwt.ui.Widgets;
import com.threerings.gwt.util.PopupCallback;

import memory.data.Datum;

/**
 * Displays a daily journal of "done" things.
 */
public class JournalDatumPanel extends ListDatumPanel
{
    @Override protected void showContents ()
    {
        _today = _datum.children.get(0);
        super.showContents();
    }

    @Override protected void addTitle (FlowPanel header)
    {
        Image today = Widgets.newImage(_rsrc.todayImage(), _rsrc.styles().pickerPopper());
        header.add(Widgets.makeActionImage(today, "Go to today.", new ClickHandler() {
            public void onClick (ClickEvent event) {
                changeDate(System.currentTimeMillis());
            }
        }));
        header.add(_popper = createPickerPopper());
        header.add(_title = Widgets.newLabel("", _rsrc.styles().title()));
        dateUpdated(new Date(Long.parseLong(_today.title)));
    }

    @Override protected List<Datum> getChildData ()
    {
        return _today.children;
    }

    @Override protected long getParentIdForChild ()
    {
        return _today.id;
    }

    protected void changeDate (long when)
    {
        _datasvc.loadJournalData(_ctx.cortexId, _datum.id, when, new PopupCallback<Datum>(_popper) {
            public void onSuccess (Datum datum) {
                _datum.children.set(0, datum);
                showContents();
            }
        });
    }

    protected void dateUpdated (Date when)
    {
        _curdate = when;
        _title.setText(_datum.title + " - " + _yfmt.format(_curdate));
    }

    protected Widget createPickerPopper ()
    {
        final Image popper = new Image(_rsrc.pickdateImage());
        popper.addStyleName(_rsrc.styles().pickerPopper());
        Widgets.makeActionImage(popper, "Change date.", new ClickHandler() {
            public void onClick (ClickEvent event) {
                if (_popup == null) {
                    _popup = new PopupPanel(true);
                    _popup.addStyleName(_rsrc.styles().popup());
                    _popup.setWidget(createDatePicker(_popup));
                }
                if (_popup.isShowing()) {
                    _popup.hide();
                } else {
                    Popups.showNear(_popup, popper);
                }
            }
            protected PopupPanel _popup;
        });
        return popper;
    }

    protected DatePicker createDatePicker (final PopupPanel popup)
    {
        DatePicker picker = new DatePicker();
        picker.setValue(_curdate, true);
        picker.addValueChangeHandler(new ValueChangeHandler<Date>() {
            public void onValueChange (ValueChangeEvent<Date> event) {
                changeDate(event.getValue().getTime());
                popup.hide();
            }
        });
        return picker;
    }

    protected Datum _today;
    protected Date _curdate;
    protected Label _title;
    protected Widget _popper;

    protected static final DateTimeFormat _yfmt = DateTimeFormat.getFormat("MMM dd, yyyy");
}
