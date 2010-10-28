//
// $Id$

package memory.rpc;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import memory.data.Access;
import memory.data.Datum;
import memory.data.Type;

/**
 * Provides GWT services.
 */
@RemoteServiceRelativePath(DataService.ENTRY_POINT)
public interface DataService extends RemoteService
{
    /** The path at which this service's servlet is mapped. */
    public static final String ENTRY_POINT = "data";

    /** Creates a new datum for the calling user.
     * @return the id of the newly created datum. */
    long createDatum (Datum datum) throws ServiceException;

    /** Updates the specified datum. Only the non-null fields are modified. */
    void updateDatum (long id, Long parentId, Access access, Type type,
                      String meta, String title, String text, Long when, Boolean archived)
        throws ServiceException;
}
