//
// $Id$

package memory.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import com.allen_sauer.gwt.dnd.client.DragContext;
import com.allen_sauer.gwt.dnd.client.DragHandler;
import com.allen_sauer.gwt.dnd.client.PickupDragController;
import com.allen_sauer.gwt.dnd.client.util.DOMUtil;
import com.allen_sauer.gwt.dnd.client.util.DragClientBundle;
import com.allen_sauer.gwt.dnd.client.drop.FlowPanelDropController;

import com.threerings.gwt.ui.Widgets;

/**
 * Encapsulates some drag and drop bits.
 */
public class DnDUtil
{
    public static PickupDragController addDnD (FlowPanel target, DragHandler handler)
    {
        PickupDragController dragger = new PickupDragController(RootPanel.get(), false);
        dragger.addDragHandler(handler);
        dragger.registerDropController(new FlowPanelDropController(target) {
            @Override protected Widget newPositioner (DragContext context) {
                // copied from VerticalPanelDropController
                SimplePanel outer = new SimplePanel();
                outer.addStyleName(DragClientBundle.INSTANCE.css().positioner());
                RootPanel.get().add(outer, -500, -500);
                int width = 0;
                int height = 0;
                for (Widget widget : context.selectedWidgets) {
                    width = Math.max(width, widget.getOffsetWidth());
                    height += widget.getOffsetHeight();
                }
                SimplePanel inner = new SimplePanel();
                inner.setPixelSize(width - DOMUtil.getHorizontalBorders(outer), height
                                   - DOMUtil.getVerticalBorders(outer));
                outer.setWidget(inner);
                return outer;
            }
        });
        return dragger;
    }

    public static Image newDragIcon ()
    {
        return Widgets.newImage(_rsrc.draggerImage(), _rsrc.styles().dragIcon());
    }

    protected static final MemoryResources _rsrc = GWT.create(MemoryResources.class);
}
