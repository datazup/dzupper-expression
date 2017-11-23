package org.datazup.apiinternal;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ninel on 11/22/17.
 */
public abstract class ApiServiceBase implements ApiService {

    protected Map<String,APIRunnable> apis = new HashMap<>();

    public void add(APIRunnable apiRunnable){
        apis.put(apiRunnable.getName(), apiRunnable);
    }

    public Boolean contains(String apiName){
        return apis.containsKey(apiName);
    }

    public Object execute(String apiName, Object params){

        if (apis.containsKey(apiName)){
            APIRunnable api = apis.get(apiName);
            CommonApiParams params1 = new CommonApiParams();
            params1.setPayload(params);
            CommonApiResponse apiResponse = api.run(params1);
            return apiResponse;
        }
        return null;
    }
}
