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

import memory.data.Datum;
import memory.data.Type;

/**
 * The main entry point for the Memory client.
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
            Datum data = parseDatum((Element)relem.getChild(0));
            relem.removeFromParent();
            RootPanel.get(CLIENT_DIV).add(new Label("Parsed: " + data.text));
        }
    }

    protected Datum parseDatum (Element elem)
    {
        // parse the data for this element
        Datum datum = new Datum();
        datum.id = Long.parseLong(elem.getId());
        datum.meta = elem.getAttribute("x:meta");
        datum.text = elem.getInnerHTML();
        datum.when = Long.parseLong(elem.getAttribute("x:when"));
        datum.type = Enum.valueOf(Type.class, elem.getAttribute("x:type"));

        // then parse any children
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
        datum.children = children.toArray(new Datum[children.size()]);

        return datum;
    }

    protected static final String CLIENT_DIV = "client";
    protected static final String ROOT_DIV = "root";
}
