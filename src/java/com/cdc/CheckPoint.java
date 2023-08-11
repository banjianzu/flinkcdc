package com.cdc;

import com.ververica.cdc.connectors.base.options.StartupOptions;
import com.ververica.cdc.connectors.oracle.OracleSource;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.environment.CheckpointConfig;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import java.util.Properties;

/**
 * 用于数据实时同步断点续传功能实现
 */
public class CheckPoint {
    
    public static void main(String[] args) {

        String savePoint = null ;
        if(args.length > 0)
            savePoint = args[0] ;

        StreamExecutionEnvironment env = getEnv(savePoint);
        Properties p = new Properties();
        p.setProperty("debezium.database.tablename.case.insensitive", "false");
        p.setProperty("database.tablename.case.insensitive", "false");
        p.setProperty("log.mining.strategy", "online_catalog");
        p.setProperty("log.mining.continuous.mine", "true");

        com.ververica.cdc.debezium.DebeziumSourceFunction<String> sourceFunsion =
                OracleSource.<String>builder()
                        .hostname("localhost")
                        .port(1521)
                        .database("orcl") //小写
                        .schemaList("FLINKUSER")
                        .tableList("FLINKUSER.MDM_COMMPROP_CONTENT") //带前缀
                        .username("flinkuser")
                        .password("flinkpw20230607")
                        .deserializer(new CoarseDeserializationSchema())
                        .debeziumProperties(p)
                        .startupOptions(StartupOptions.latest())
                        .build();

        assert env != null;
        env.addSource(sourceFunsion).print();

        CDCLog.info("***FlinkCDC监听主数据变化启动***");

        try {
            env.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static StreamExecutionEnvironment getEnv(String savaPotion) {

        try {

            StreamExecutionEnvironment env;
            if(savaPotion != null && !savaPotion.equals("")){

                org.apache.flink.configuration.Configuration configuration =
                        new org.apache.flink.configuration.Configuration();
                // "file:///D:\\tmp\\flink\\checkpoint\\a1b7fada2242ea85b155bf2b1fa5b879\\chk-6"
                configuration.setString("execution.savepoint.path", savaPotion);

                env = StreamExecutionEnvironment.getExecutionEnvironment(configuration);
            }else env = StreamExecutionEnvironment.getExecutionEnvironment();

            env.enableCheckpointing(60 * 1000, CheckpointingMode.EXACTLY_ONCE);
            env.getCheckpointConfig().setCheckpointTimeout(2 * 60 * 1000);
            env.getCheckpointConfig().setTolerableCheckpointFailureNumber(Integer.MAX_VALUE);
            env.getCheckpointConfig().setMaxConcurrentCheckpoints(1);
            env.getCheckpointConfig().enableExternalizedCheckpoints(
                    CheckpointConfig.ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION);
            env.setStateBackend(new FsStateBackends(
                    "file:///D:\\tmp\\flink\\checkpoint" ,true));

            env.setParallelism(4);

            return env;
        } catch (Exception e) {
            CDCLog.info("env监听错误！" + e.getMessage());
        }

        return null;
    }
}