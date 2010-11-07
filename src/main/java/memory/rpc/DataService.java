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

    /** Supplied for userId when checking access for unauthenticated viewers. */
    public static final String NO_USER = "<global>";

    /** Returned by {@link #loadAccountInfo}. */
    public static class AccountResult implements IsSerializable
    {
        /** The userId as which the user is logged in. */
        public String userId;

        /** The nickname as which the user is logged in. */
        public String nickname;

        /** The cortices to which this user has access. */
        public Map<Access, List<String>> cortexen;
    }

    /** Returned by {@link #loadAccessInfo}. */
    public static class AccessResult implements IsSerializable
    {
        /** The public access setting for this datum. */
        public Access publicAccess;

        /** A map from userId to access privileges for users with custom access. */
        public Map<String, Access> userAccess;
    }

    /** Loads info for the authenticated account. */
    AccountResult loadAccountInfo () throws ServiceException;

    /** Loads the access information for the specified datum. */
    AccessResult loadAccessInfo (String cortexId, long datumId) throws ServiceException;

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
    void updateDatum (String cortexId, long id,
                      Datum.Field field1, FieldValue value1,
                      Datum.Field field2, FieldValue value2)
        throws ServiceException;

    /** Updates the specified fields of the specified datum. */
    void updateDatum (String cortexId, long id,
                      Datum.Field field1, FieldValue value1,
                      Datum.Field field2, FieldValue value2,
                      Datum.Field field3, FieldValue value3)
        throws ServiceException;

    /** Updates access to the specified datum for the specified user. */
    void updateAccess (String userId, String cortexId, long datumId, Access access)
        throws ServiceException;

    /** Loads the journal data for the specified date. */
    Datum loadJournalData (String cortexId, long journalId, long when)
        throws ServiceException;

    /** Deletes the specified datum. */
    void deleteDatum (String cortextId, long id);
}
