//
// $Id$

package memory.rpc;

import com.google.gwt.user.client.rpc.RemoteService;

import memory.data.Access;
import memory.data.Datum;
import memory.data.Type;

/**
 * Provides GWT services.
 */
public interface DataService extends RemoteService
{
    /** Creates a new datum for the calling user. */
    void createDatum (Datum datum) throws ServiceException;

    /** Updates the specified datum. Only the non-null fields are modified. */
    void updateDatum (long id, Long parentId, Access access, String meta, String text,
                      Type type, Long when, Boolean archived)
        throws ServiceException;
}
