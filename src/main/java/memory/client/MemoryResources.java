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
        String floatLeft ();
        String floatRight ();
        String popup ();
        String navigationLink ();
        String title ();

        String editor ();
        String editorTitle ();
        String view ();
        String pageTitle ();
        String pageDatum ();
        String textTitle ();
        String listItem ();
        String insetBox ();

        String width98 ();
        String width99 ();
        String width100 ();

        String columnCont ();
        String column ();
        String column12 ();
        String column22 ();
        String column13 ();
        String column23 ();
        String column33 ();

        String nonExistNote ();
        String pickerPopper ();
    }

    @Source("memory.css")
    Styles styles ();

    @Source("edit.png")
    ImageResource editImage ();

    @Source("close.png")
    ImageResource closeImage ();

    @Source("access.png")
    ImageResource accessImage ();

    @Source("pickdate.png")
    ImageResource pickdateImage ();

    @Source("today.png")
    ImageResource todayImage ();
}
