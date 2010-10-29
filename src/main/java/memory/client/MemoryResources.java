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
        String editorTitle ();
        String view ();
        String pageTitle ();
        String pageDatum ();
        String textTitle ();
        String stretchWide ();
        String insetBox ();
    }

    @Source("memory.css")
    Styles styles ();

    @Source("edit.png")
    ImageResource editImage ();

    @Source("close.png")
    ImageResource closeImage ();
}
