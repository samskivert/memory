//
// $Id$

package memory.rpc;

import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import memory.data.Access;
import memory.data.AccessInfo;
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
    public static class AccountResult implements IsSerializable {
        /** The userId as which the user is logged in. */
        public String userId;

        /** The nickname as which the user is logged in. */
        public String nickname;

        /** A URL for logging out. */
        public String logoutURL;

        /** The list of cortices owned by this user. */
        public List<String> owned;

        /** The list of cortices shared with this user. */
        public List<AccessInfo> shared;
    }

    /** Returned by {@link #loadAccessInfo}. */
    public static class AccessResult implements IsSerializable {
        /** The public access setting for this datum. */
        public Access publicAccess;

        /** A list of users who have custom access. */
        public List<AccessInfo> userAccess;
    }

    /** Returned by {@link #getShareInfo}. */
    public static class ShareInfo implements IsSerializable {
        /** The name of the cortex being shared with the caller. */
        public String cortex;

        /** The nickname of the account on which the caller is authenticated. */
        public String nickname;

        /** A URL for logging out. */
        public String logoutURL;
    }

    /** Loads info for the authenticated account. */
    AccountResult loadAccountInfo () throws ServiceException;

    /** Loads the access information for the specified cortex. */
    AccessResult loadAccessInfo (String cortexId) throws ServiceException;

    /** Loads the access information for the specified datum. */
    AccessResult loadAccessInfo (String cortexId, long datumId) throws ServiceException;

    /** Creates a new cortex with the specified id.
     * @exception ServiceException thrown with `e.name_in_use` if the requested name is used. */
    void createCortex (String cortexId) throws ServiceException;

    /** Deletes the cortex with the specified id. */
    void deleteCortex (String cortexId) throws ServiceException;

    /** Forks the specified datum (and all of its children) into a new cortex. */
    void forkCortex (String cortexId, long datumId, String newCortexId) throws ServiceException;

    /** Requests to share the specified cortex with the supplied email address. */
    void shareCortex (String cortexId, String email, Access access) throws ServiceException;

    /** Updates the public access for the specified cortex. */
    void updateCortexPublicAccess (String cortexId, Access access) throws ServiceException;

    /** Updates the specified existing cortex access record. */
    void updateCortexAccess (long id, Access access) throws ServiceException;

    /** Returns info related to a share notification. */
    ShareInfo getShareInfo (String token) throws ServiceException;

    /** Accepts the specified share request. */
    void acceptShareRequest (String token) throws ServiceException;

    /** Creates a new datum for the calling user.
     * @return the id of the newly created datum. */
    long createDatum (String cortexId, Datum datum) throws ServiceException;

    /** Updates the specified field of the specified datum. */
    void updateDatum (String cortexId, long id, Datum.Field field, FieldValue value)
        throws ServiceException;

    /** Updates the specified fields of the specified datum. */
    void updateDatum (String cortexId, long id, Map<Datum.Field, FieldValue> updates)
        throws ServiceException;

    /** Updates public access to the specified datum. */
    void updatePublicAccess (String cortexId, long datumId, Access access) throws ServiceException;

    /** Loads the journal data for the specified date. */
    Datum loadJournalData (String cortexId, long journalId, long when) throws ServiceException;

    /** Deletes the specified datum. */
    void deleteDatum (String cortextId, long id) throws ServiceException;

    /** Returns the URL to which media may be uploaded. */
    String getUploadURL () throws ServiceException;
}
