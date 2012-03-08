//
// $Id$

package memory.rpc;

import java.util.Map;
import com.google.gwt.user.client.rpc.AsyncCallback;
import memory.data.Access;
import memory.data.Datum;

import memory.data.FieldValue;

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
     * The async version of {@link DataService#loadAccessInfo}.
     */
    void loadAccessInfo (String cortexId, AsyncCallback<DataService.AccessResult> callback);

    /**
     * The async version of {@link DataService#loadAccessInfo}.
     */
    void loadAccessInfo (String cortexId, long arg1, AsyncCallback<DataService.AccessResult> callback);

    /**
     * The async version of {@link DataService#createCortex}.
     */
    void createCortex (String cortexId, AsyncCallback<Void> callback);

    /**
     * The async version of {@link DataService#forkCortex}.
     */
    void forkCortex (String cortexId, long datumId, String newCortexId, AsyncCallback<Void> callback);

    /**
     * The async version of {@link DataService#shareCortex}.
     */
    void shareCortex (String cortexId, String email, Access access, AsyncCallback<Void> callback);

    /**
     * The async version of {@link DataService#updateCortexPublicAccess}.
     */
    void updateCortexPublicAccess (String cortexId, Access access, AsyncCallback<Void> callback);

    /**
     * The async version of {@link DataService#updateCortexAccess}.
     */
    void updateCortexAccess (long id, Access access, AsyncCallback<Void> callback);

    /**
     * The async version of {@link DataService#getShareInfo}.
     */
    void getShareInfo (String token, AsyncCallback<DataService.ShareInfo> callback);

    /**
     * The async version of {@link DataService#acceptShareRequest}.
     */
    void acceptShareRequest (String token, AsyncCallback<Void> callback);

    /**
     * The async version of {@link DataService#createDatum}.
     */
    void createDatum (String cortexId, Datum datum, AsyncCallback<Long> callback);

    /**
     * The async version of {@link DataService#updateDatum}.
     */
    void updateDatum (String cortexId, long id, Datum.Field field, FieldValue value, AsyncCallback<Void> callback);

    /**
     * The async version of {@link DataService#updateDatum}.
     */
    void updateDatum (String cortexId, long id, Map<Datum.Field, FieldValue> field, AsyncCallback<Void> callback);

    /**
     * The async version of {@link DataService#updatePublicAccess}.
     */
    void updatePublicAccess (String cortexId, long datumId, Access access, AsyncCallback<Void> callback);

    /**
     * The async version of {@link DataService#loadJournalData}.
     */
    void loadJournalData (String cortexId, long journalId, long when, AsyncCallback<Datum> callback);

    /**
     * The async version of {@link DataService#deleteDatum}.
     */
    void deleteDatum (String cortextId, long id, AsyncCallback<Void> callback);

    /**
     * The async version of {@link DataService#getUploadURL}.
     */
    void getUploadURL (AsyncCallback<String> callback);
}
