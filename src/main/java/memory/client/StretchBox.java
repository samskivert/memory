//
// $Id$

package memory.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Displays a bunch of widgets in a row, with one widget stretched to consume extra space. Does so
 * via JavaScript layout, not the bullshit CSS box model.
 */
public class StretchBox extends FlowPanel
{
    public StretchBox (int stretchIdx, Widget... widgets)
    {
        addStyleName(_rsrc.styles().stretchBox());
        _stretch = widgets[stretchIdx];
        for (Widget w : widgets) {
            if (w != null) {
                add(w);
            }
        }
        queueUpdateStretcher();
    }

    @Override // from Widget
    public void setVisible (boolean visible)
    {
        super.setVisible(visible);
        if (visible) {
            queueUpdateStretcher();
        }
    }

    /**
     * Tells the stretch box about border and margin pixels that are not accounted for by the
     * {@link Widget#getOffsetWidth} calls it makes to compute layout size. Borders and margins of
     * the stretched widget <em>must</em> be included in these gaps, and the borders and margins of
     * some non-stretched widgets may need to be supplied here as well. Yay for CSS!
     */
    public StretchBox gaps (int gapPixels)
    {
        _gaps = gapPixels;
        return this;
    }

    protected void queueUpdateStretcher ()
    {
        DeferredCommand.addCommand(new Command() {
            public void execute () {
                if (isAttached() && isVisible()) {
                    updateStretcher();
                }
            }
        });
    }

    protected void updateStretcher ()
    {
        int avail = getOffsetWidth() - _gaps;
        for (int ii = 0, ll = getWidgetCount(); ii < ll; ii++) {
            Widget w = getWidget(ii);
            if (w != _stretch) {
                avail -= w.getOffsetWidth();
            }
        }
        _stretch.setWidth(avail + "px");
    }

    protected int _gaps;
    protected Widget _stretch;

    protected static final MemoryResources _rsrc = GWT.create(MemoryResources.class);
}
