//
// $Id$

package memory.data;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * A poor man's union type.
 */
public class FieldValue implements IsSerializable
{
    /** Contains a long value. */
    public static class LongValue extends FieldValue {
        public long value;
        public String toString () {
            return String.valueOf(value);
        }
    }

    /** Contains a String value. */
    public static class StringValue extends FieldValue {
        public String value;
        public String toString () {
            return value;
        }
    }

    /** Contains a Type value. */
    public static class TypeValue extends FieldValue {
        public Type value;
        public String toString () {
            return value.toString();
        }
    }

    /** Creates a long value. */
    public static FieldValue of (long value) {
        LongValue fvalue = new LongValue();
        fvalue.value = value;
        return fvalue;
    }

    /** Creates a String value. */
    public static FieldValue of (String value) {
        StringValue fvalue = new StringValue();
        fvalue.value = value;
        return fvalue;
    }

    /** Creates a Type value. */
    public static FieldValue of (Type value) {
        TypeValue fvalue = new TypeValue();
        fvalue.value = value;
        return fvalue;
    }
}
