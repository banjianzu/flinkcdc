package com.cdc;

import org.apache.flink.runtime.state.filesystem.FsStateBackend;

public class FsStateBackends extends FsStateBackend {

    public FsStateBackends(String checkpointDataUri) {
        super(checkpointDataUri);
    }

    public FsStateBackends(String checkpointDataUri,boolean falg){

        this(checkpointDataUri) ;
        if(falg)
            CDCLog.info("FsStateBackends---------------------------");
    }
}
