package com.snyper.keeva.model;

import java.util.List;

/**
 * Created by stephen snyper on 9/28/2018.
 */

public class MyResponse {
    public long multicast_id;
    public int success;
    public int faiure;
    public int canonical_ids;
    public List<Result> results;

}
