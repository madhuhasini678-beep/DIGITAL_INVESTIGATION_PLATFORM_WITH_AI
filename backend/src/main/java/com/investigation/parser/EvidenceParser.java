package com.investigation.parser;

import java.io.IOException;
import java.io.InputStream;

public interface EvidenceParser<T> {

    T parse(InputStream inputStream) throws IOException;
}
