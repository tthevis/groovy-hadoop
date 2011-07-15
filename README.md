# Groovy Hadoop 

This project's intention is to bring the power and expressiveness of [Groovy](http://groovy.codehaus.org/) 
to the almighty [Hadoop](http://hadoop.apache.org/) framework to support *ad-hoc* Hadoop job execution right 
from the command line with expressive Java or Groovy syntax and without the need to create Java projects and 
executable JAR files.  

Target audience:

* People who like to think and to code in terms of map/reduce but are a little annoyed 
by the need of setting up Java projects and creating executable Hadoop `*.jar` files 
for even the most basic map/reduce applications
* People who would like to combine *hadoop-streaming* with Groovy but do not have the possibility 
to install Groovy on the Hadoop cluster nodes
* People who would like to run *ad-hoc* processing jobs and want to use their custom `Writable` implementations via the Java/Groovy API   

Who will not benefit from using *groovy-hadoop*? 

* Performance hunters. After all, it is Groovy, it is dynamic, it is not completely pre-compiled. 
The means to re-use output keys and values are limited and so on...
* People striving for *yet another abstraction layer* (TM) on top of map/reduce. 
Please consider using [Pig](http://pig.apache.org/) or [Hive](http://hive.apache.org/) or something similar this case.
* Scripting Gurus. If you know how to write your custom queries using your scripting language of choice in concert with
[hadoop-streaming](http://hadoop.apache.org/common/docs/current/streaming.html), chances are that you can do without 
this project.  

## User Guide

*groovy-hadoop* is a command line tool. It integrates seamlessly with the `hadoop jar` application. Hadoop properties are transparently
delegated to underlying framework with only some minor exceptions.
Users can specify their `map()` and `reduce()` code inline at the command prompt.

### Example: The Ubiquitous Wordcount
[Wordcount](http://wiki.apache.org/hadoop/WordCount) with *groovy-hadoop* is executed as

	$ hadoop jar groovy-hadoop-0.1.0.jar                                                              \
	-D mapred.output.key.class=org.apache.hadoop.io.Text                                              \
	-D mapred.output.value.class=org.apache.hadoop.io.LongWritable                                    \
	-map 'value.toString().tokenize().each { context.write(new Text(it), new LongWritable(1)) }'      \
	-reduce 'def sum = 0; values.each { sum += it.get() }; context.write(key, new LongWritable(sum))' \
	-input <input path(s) in HDFS>                                                                    \
	-output <output path in HDFS>                                                                     \
	
Of course, it is possible to write plain old Java syntax. For this example, we save the Java code in text files
and read it during execution from a subshell:

1) Create `map.txt`
    
    Scanner scanner = new Scanner(value.toString()); 
    while (scanner.hasNext()) { 
        context.write(new Text(scanner.next()), new LongWritable(1)); 
    }
	
2) Create `reduce.txt`

    int sum = 0; 
    for (LongWritable value : values) {
        sum += value.get();
    }
    context.write(key, new LongWritable(sum));
	
3) Execution

    $ hadoop jar groovy-hadoop-0.1.0.jar                            \
    -D mapred.output.key.class=org.apache.hadoop.io.Text            \
    -D mapred.output.value.class=org.apache.hadoop.io.LongWritable  \
    -map `cat map.txt`                                              \
    -reduce `reduce.txt`                                            \
    -input <input path(s) in HDFS>                                  \
    -output <output path in HDFS>                                   \

### Magic Parameters and Magic Imports

Map and reduce scripts must contain of syntactically correct Groovy or Java code. Classes which are
not automatically imported have to be fully qualified or have to be imported as in every other Java/Groovy application.

The following parameters are injected and accessible within the `map` code:

- key : The input key type as provided by the Inputformat
- value : The input value type as provided by the Inputformat
- context : `org.apache.hadoop.mapreduce.Mapper.Context`
- log : `java.util.logging.Logger` 

The following parameters are injected and accessible within the `reduce` code:

- key : The input key type as specified by `mapred.output.key.class` or `mapred.mapoutput.key.class`
- values : `java.lang.Iterable` of the generic type specified by `mapred.output.value.class` or `mapred.mapoutput.value.class`
- context : `org.apache.hadoop.mapreduce.Reducer.Context`
- log : `java.util.logging.Logger` 

Additionally, some classes are already imported for convenience. In addition to the 
[Groovy default imports](http://groovy.codehaus.org/Differences+from+Java), all the `Writable`s from
`org.apache.hadoop.io` are accessible and do not need to be fully qualified.

### Combining groovy-hadoop with Custom Libraries

		    	
## Developer Guide

### Build the Project

The project's sources are written in Groovy (version: 1.8), the project is built 
with [Gradle](http://www.gradle.org/) (version: 1.0-milestone-1). To set up an Eclipse project execute

    $ gradle eclipse
    
To create an executable JAR file in the `build/libs` folder type

    $ gradle build
    
'nuff said.
        
## License

This project is licensed under the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0). 
A copy of it can be found in the main project folder. In short: this project is (obviously) open source and free. 
 