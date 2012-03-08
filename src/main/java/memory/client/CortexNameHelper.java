//
// $Id$

package memory.client;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.TextBox;

/**
 * Handles enforcing cortex name restrictions.
 */
public class CortexNameHelper
{
    public CortexNameHelper (TextBox name, HTML tip, Button create) {
        _name = name;
        _tip = tip;
        _create = create;

        _name.addKeyUpHandler(new KeyUpHandler() {
            public void onKeyUp (KeyUpEvent event) {
                processName();
            }
        });
        _name.addChangeHandler(new ChangeHandler() {
            public void onChange (ChangeEvent event) {
                processName();
            }
        });
        _create.setEnabled(false);
    }

    public boolean processName ()
    {
        String cid = _name.getText().trim().toLowerCase().replace(' ', '-').replaceAll("[&\\?]", "");
        _name.setText(cid);
        _create.setEnabled(cid.length() >= 4);
        if (cid.length() == 0) {
            _tip.setText("");
            return false;
        } else if (cid.length() < 4) {
            _tip.setText("The name must be at least 4 characters long.");
            return false;
        } else {
            _tip.setHTML("Your cortex URL will be:<br/>\n" +
                         "http://www.sparecortex.com/c/" + URL.encode(cid));
            return true;
        }
    }

    protected final TextBox _name;
    protected final HTML _tip;
    protected final Button _create;
}
