package com.cdc.util

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}

object HadoopUtil {

  def main(args: Array[String]): Unit = {
    val conf = new Configuration()
    val fs = FileSystem.get(conf)

    val path = new Path("hdfs://localhost:9000/path/to/file")
    val inputStream = fs.open(path)

    try {
      val content = scala.io.Source.fromInputStream(inputStream).mkString
      println(content)
    } finally {
      inputStream.close()
    }
  }


}
