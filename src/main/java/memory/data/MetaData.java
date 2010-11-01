//
// $Id$

package memory.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A utility for managing the metadata for a datum.
 */
public class MetaData
{
    /**
     * Parses the supplied metadata string into a more useful format.
     */
    public MetaData (String meta)
    {
        for (String item : meta.split(";")) {
            int eidx = item.indexOf("=");
            if (eidx != -1) {
                _data.put(item.substring(0, eidx), item.substring(eidx+1));
            } // else: not much we can do here but drop the malformed data
        }
    }

    /**
     * Returns a boolean value for the specified key, or `defval` if no value exists.
     */
    public boolean get (String key, boolean defval)
    {
        String value = _data.get(key);
        return (value == null) ? defval : "t".equals(value);
    }

    /**
     * Returns an integer value for the specified key, or `defval` if no value exists.
     */
    public int get (String key, int defval)
    {
        String value = _data.get(key);
        try {
            return (value == null) ? defval : Integer.parseInt(value);
        } catch (NumberFormatException nfe) {
            return defval;
        }
    }

    /**
     * Returns a string value for the specified key, or `defval` if no value exists.
     */
    public String get (String key, String defval)
    {
        String value = _data.get(key);
        return (value == null) ? defval : value;
    }

    /**
     * Returns an ordered list of ids for the specified key, or the empty list if no value exists.
     * The returned list is freshly created and can be mutated by the caller as desired.
     */
    public List<Long> getIds (String key)
    {
        List<Long> ids = new ArrayList<Long>();
        String value = _data.get(key);
        if (value != null) {
            for (String idstr : value.split(",")) {
                try {
                    ids.add(Long.parseLong(idstr));
                } catch (NumberFormatException nfe) {
                    // oh well, drop it from the list
                }
            }
        }
        return ids;
    }

    /**
     * Sets the specified key to the specified value.
     */
    public void set (String key, boolean value)
    {
        _data.put(key, value ? "t" : "f");
    }

    /**
     * Sets the specified key to the specified value.
     */
    public void set (String key, int value)
    {
        _data.put(key, String.valueOf(value));
    }

    /**
     * Sets the specified key to the specified (non-null) value.
     */
    public void set (String key, String value)
    {
        if (value == null) {
            throw new NullPointerException();
        }
        _data.put(key, value);
    }

    /**
     * Sets the specified key to the specified ordered list of ids.
     */
    public void setIds (String key, List<Long> ids)
    {
        StringBuilder buf = new StringBuilder();
        for (Long id : ids) {
            if (buf.length() > 0) {
                buf.append(",");
            }
            buf.append(id);
        }
        _data.put(key, buf.toString());
    }

    /**
     * Formats our current metadata into a string for storage back into a {@link Datum}.
     */
    public String toMetaString ()
    {
        StringBuilder buf = new StringBuilder();
        for (Map.Entry<String, String> entry : _data.entrySet()) {
            if (buf.length() > 0) {
                buf.append(";");
            }
            // TODO: escape ; in entry.getValue()
            buf.append(entry.getKey()).append('=').append(entry.getValue());
        }
        return buf.toString();
    }

    protected Map<String, String> _data = new HashMap<String, String>();
}
