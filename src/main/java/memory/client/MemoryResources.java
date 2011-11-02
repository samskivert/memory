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
        String header ();
        String iconButton ();
        String rightIconButton ();
        String floatLeft ();
        String floatRight ();
        String dragIcon ();
        String textCenter ();
        String blockCenter ();
        String popup ();
        String navigationLink ();
        String title ();

        String editorBox ();
        String editor ();
        String editorTitle ();
        String editorSelfLink ();
        String editorLabel ();
        String editorChildItem ();
        String editorUpdateButton ();
        String view ();
        String pageTitle ();
        String pageDatum ();
        String textTitle ();
        String listItem ();
        String unsavedItem ();
        String failedItem ();
        String stretchBox ();
        String mediaBox ();
        String noitems ();

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
        String tip ();
        String wiki ();
        String wikiHelp ();
        String wikiExample ();
        String wikiHelpLabel ();
    }

    @Source("memory.css")
    Styles styles ();

    @Source("edit.png")
    ImageResource editImage ();

    @Source("help.png")
    ImageResource helpImage ();

    @Source("close.png")
    ImageResource closeImage ();

    @Source("access.png")
    ImageResource accessImage ();

    @Source("pickdate.png")
    ImageResource pickdateImage ();

    @Source("today.png")
    ImageResource todayImage ();

    @Source("fwdday.png")
    ImageResource fwddayImage ();

    @Source("backday.png")
    ImageResource backdayImage ();

    @Source("add.png")
    ImageResource addImage ();

    @Source("save.png")
    ImageResource saveImage ();

    @Source("delete.png")
    ImageResource deleteImage ();

    @Source("dragger.png")
    ImageResource draggerImage ();
}
