package com.cdc;

import com.alibaba.fastjson.JSONObject;
import com.ververica.cdc.debezium.DebeziumDeserializationSchema;
import io.debezium.data.Envelope;
import org.apache.flink.api.common.typeinfo.BasicTypeInfo;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.util.Collector;
import org.apache.kafka.connect.data.Field;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.source.SourceRecord;

import java.util.List;

/**
 * 粗粒度解析数据
 */
public class CoarseDeserializationSchema implements
        DebeziumDeserializationSchema<String> {

    public CoarseDeserializationSchema(){}

    /**
     * 每次处理一行数据
     */
    @Override
    public void deserialize(SourceRecord sourceRecord, Collector<String> collector) throws Exception {

        JSONObject  result = new JSONObject() ;
        String topic = sourceRecord.topic() ;
        String[] fields = topic.split("\\.") ;
        result.put("db",fields[1]) ;
        result.put("tableName",fields[2]) ;
        // 获取before 数据
        Struct value = (Struct) sourceRecord.value();
        Struct before = value.getStruct("before");
        JSONObject beforeJson = new JSONObject();
        if (before != null)
        {
            //获取列信息
            Schema schema = before.schema();
            List<Field> fieldList = schema.fields();
            for (Field field:fieldList)
                beforeJson.put(field.name(),before.get(field));
        }
        result.put("before",beforeJson);
        // 获取after 数据
        Struct after = value.getStruct("after");
        JSONObject afterJson = new JSONObject();
        if (after !=null)
        {
            Schema schema = after.schema();
            List<Field> afterFields = schema.fields();
            for (Field field:afterFields)
                afterJson.put(field.name(),after.get(field));

        }

        result.put("after", afterJson);
        //获取操作类型
        Envelope.Operation operation = Envelope.operationFor(sourceRecord);
        result.put("op", operation);
        //输出数据
        String res = result.toJSONString() ;
        CDCLog.info("CDC数据跟踪:" +res);

    }

    @Override
    public TypeInformation<String> getProducedType() {
        return  BasicTypeInfo.STRING_TYPE_INFO;
    }
}