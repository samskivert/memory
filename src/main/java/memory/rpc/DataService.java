//
// $Id$

package memory.rpc;

import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import memory.data.Access;
import memory.data.Datum;
import memory.data.FieldValue;
import memory.data.Type;

/**
 * Provides GWT services.
 */
@RemoteServiceRelativePath(DataService.ENTRY_POINT)
public interface DataService extends RemoteService
{
    /** The path at which this service's servlet is mapped. */
    public static final String ENTRY_POINT = "data";

    /** Returned by {@link #loadAccountInfo}. */
    public static class AccountResult implements IsSerializable
    {
        /** The nickname as which the user is logged in. */
        public String nickname;

        /** The cortices to which this user has access. */
        public Map<Access, List<String>> cortexen;
    }

    /** Loads info for the authenticated account. */
    AccountResult loadAccountInfo () throws ServiceException;

    /** Creates a new cortex with the specified id.
     * @exception ServiceException thrown with `e.name_in_use` if the requested name is used. */
    void createCortex (String cortexId) throws ServiceException;

    /** Creates a new datum for the calling user.
     * @return the id of the newly created datum. */
    long createDatum (String cortexId, Datum datum) throws ServiceException;

    /** Updates the specified field of the specified datum. */
    void updateDatum (String cortexId, long id, Datum.Field field, FieldValue value)
        throws ServiceException;

    /** Updates the specified fields of the specified datum. */
    void updateDatum (String cortexId, long id, Datum.Field field1, FieldValue value1,
                      Datum.Field field2, FieldValue value2)
        throws ServiceException;
}
