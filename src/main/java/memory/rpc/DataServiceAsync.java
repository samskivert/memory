//
// $Id$

package memory.rpc;

import com.google.gwt.user.client.rpc.AsyncCallback;
import memory.data.Datum;
import memory.data.Type;

/**
 * Provides the asynchronous version of {@link DataService}.
 */
public interface DataServiceAsync
{
    /**
     * The async version of {@link DataService#loadAccountInfo}.
     */
    void loadAccountInfo (AsyncCallback<DataService.AccountResult> callback);

    /**
     * The async version of {@link DataService#createCortex}.
     */
    void createCortex (String cortexId, AsyncCallback<Void> callback);

    /**
     * The async version of {@link DataService#createDatum}.
     */
    void createDatum (String cortexId, Datum datum, AsyncCallback<Long> callback);

    /**
     * The async version of {@link DataService#updateDatum}.
     */
    void updateDatum (String cortexId, long id, Long parentId, Type type, String meta, String title, String text, Long when, Boolean archived, AsyncCallback<Void> callback);
}
