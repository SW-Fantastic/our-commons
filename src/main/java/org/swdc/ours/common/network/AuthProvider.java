package org.swdc.ours.common.network;

import java.util.Map;

public interface AuthProvider {

    void authenticate(Map<String, String> headers);

}
