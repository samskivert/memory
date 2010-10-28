//
// $Id$

package memory.rpc;

import com.google.gwt.user.client.rpc.AsyncCallback;
import memory.data.Access;
import memory.data.Datum;
import memory.data.Type;

/**
 * Provides the asynchronous version of {@link DataService}.
 */
public interface DataServiceAsync
{
    /**
     * The async version of {@link DataService#createDatum}.
     */
    void createDatum (Datum datum, AsyncCallback<Void> callback);

    /**
     * The async version of {@link DataService#updateDatum}.
     */
    void updateDatum (long id, Long parentId, Access access, String meta, String text, Type type, Long when, Boolean archived, AsyncCallback<Void> callback);
}
