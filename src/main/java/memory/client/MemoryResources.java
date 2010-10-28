//
// $Id$

package memory.client;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;

/**
 * Resources used by the Memory GWT client.
 */
public interface MemoryResources extends ClientBundle
{
    /** Defines our CSS styles. */
    public interface Styles extends CssResource {
        String cornerButton ();
        String editor ();
        String view ();
        String pageTitle ();
        String textTitle ();
        String stretchWide ();
    }

    @Source("memory.css")
    Styles styles ();

    @Source("edit.png")
    ImageResource editImage ();

    @Source("close.png")
    ImageResource closeImage ();
}
