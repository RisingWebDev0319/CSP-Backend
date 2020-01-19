package ca.freshstart.applications.client.helpers;


import ca.freshstart.helpers.DateUtils;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.sql.Time;


public class CspTimeSerializer extends StdSerializer<Time> {

    public CspTimeSerializer() {
        this(null);
    }

    public CspTimeSerializer(Class<Time> t) {
        super(t);
    }

    @Override
    public void serialize(Time value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeString(DateUtils.toTimeFormat(value));
    }

}
