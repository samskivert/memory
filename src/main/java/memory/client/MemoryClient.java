//
// $Id$

package memory.client;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Node;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

import com.threerings.gwt.ui.Widgets;

import memory.data.Access;
import memory.data.Datum;
import memory.data.Type;

/**
 * The main entry point for a data page.
 */
public class MemoryClient implements EntryPoint
{
    // from interface EntryPoint
    public void onModuleLoad ()
    {
        Widget root = RootPanel.get(ROOT_DIV);
        if (root == null) {
            RootPanel.get(CLIENT_DIV).add(new Label("Missing data?"));
        } else {
            Element relem = root.getElement();
            relem.removeFromParent();
            String cortexId = relem.getAttribute("x:cortex");
            for (Datum datum : parseChildren(relem)) {
                RootPanel.get(CLIENT_DIV).add(DatumPanel.create(true, cortexId, datum));
            }
        }
    }

    protected static Datum parseDatum (Element elem)
    {
        Datum datum = new Datum();
        datum.id = Long.parseLong(elem.getId());
        datum.parentId = Long.parseLong(elem.getAttribute("x:parentId"));
        datum.type = Enum.valueOf(Type.class, elem.getAttribute("x:type"));
        datum.meta = elem.getAttribute("x:meta");
        datum.title = elem.getTitle();
        if (datum.type.hasText()) {
            datum.text = elem.getInnerText().trim();
        }
        datum.when = Long.parseLong(elem.getAttribute("x:when"));
        datum.children = parseChildren(elem);
        return datum;
    }

    protected static List<Datum> parseChildren (Element elem)
    {
        List<Datum> children = new ArrayList<Datum>();
        for (int ii = 0, ll = elem.getChildCount(); ii < ll; ii++) {
            Node node = elem.getChild(ii);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                try {
                    children.add(parseDatum((Element)node));
                } catch (Exception e) {
                    GWT.log("Failed to parse datum " + node, e);
                }
            }
        }
        return children;
    }

    protected static final String CLIENT_DIV = "client";
    protected static final String ROOT_DIV = "root";
}
