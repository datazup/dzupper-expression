package org.datazup.apiinternal;

/**
 * Created by ninel on 11/22/17.
 */
public interface APIRunnable {
    CommonApiResponse run(CommonApiParams params);
    String getName();
}
