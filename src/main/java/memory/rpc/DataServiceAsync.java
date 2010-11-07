//
// $Id$

package memory.rpc;

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
     * The async version of {@link DataService#loadJournalData}.
     */
    void loadJournalData (String cortexId, long journalId, long when, AsyncCallback<Datum> callback);

    /**
     * The async version of {@link DataService#loadAccountInfo}.
     */
    void loadAccountInfo (AsyncCallback<DataService.AccountResult> callback);

    /**
     * The async version of {@link DataService#loadAccessInfo}.
     */
    void loadAccessInfo (String cortexId, long datumId, AsyncCallback<DataService.AccessResult> callback);

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
    void updateDatum (String cortexId, long id, Datum.Field field, FieldValue value, AsyncCallback<Void> callback);

    /**
     * The async version of {@link DataService#updateDatum}.
     */
    void updateDatum (String cortexId, long id, Datum.Field field, FieldValue value, Datum.Field arg4, FieldValue arg5, AsyncCallback<Void> callback);

    /**
     * The async version of {@link DataService#updateDatum}.
     */
    void updateDatum (String cortexId, long id, Datum.Field field, FieldValue value, Datum.Field arg4, FieldValue arg5, Datum.Field arg6, FieldValue arg7, AsyncCallback<Void> callback);

    /**
     * The async version of {@link DataService#updateAccess}.
     */
    void updateAccess (String userId, String cortexId, long datumId, Access access, AsyncCallback<Void> callback);

    /**
     * The async version of {@link DataService#deleteDatum}.
     */
    void deleteDatum (String cortextId, long id, AsyncCallback<Void> callback);
}
