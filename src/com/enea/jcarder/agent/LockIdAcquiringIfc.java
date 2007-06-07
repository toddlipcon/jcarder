package com.enea.jcarder.agent;

import java.io.IOException;

public interface LockIdAcquiringIfc {

    /*
     * Return an id for a given object.
     *
     * If the method is invoked witht the same object instance more than once
     * this method will guarantee that they will get the same id. Two objects
     * that are not identical (as compared with "==") will get different ids.
     */
    int acquireLockId(Object o) throws IOException;

}
