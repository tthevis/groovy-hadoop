# Groovy Hadoop 

This project's intention is to bring the power and expressiveness of [Groovy](http://groovy.codehaus.org/) 
to the almighty [Hadoop](http://hadoop.apache.org/) framework to support *ad-hoc* Hadoop job execution right 
from the command line with expressive Java or Groovy syntax and without the need to create Java projects and 
executable JAR files.  

Target audience:

* People who like to think and to code in terms of map/reduce but are a little annoyed 
by the need of setting up Java projects and creating executable Hadoop JAR files 
for even the most basic map/reduce applications
* People who would like to combine *hadoop-streaming* with Groovy but do not have the possibility 
to install Groovy on the Hadoop cluster nodes
* People who would like to run *ad-hoc* processing jobs and want to use their custom `Writable` implementations via the Java/Groovy API   
* People wanting to prototype map/reduce applications before developing *proper* Java projects

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
delegated to the underlying framework with only some minor exceptions.
Users can specify their `map()` and `reduce()` code inline right at the command prompt. Since version 0.2.0 it
is also possible to specify `combine` code.

### Requirements

The only requirement is a working Hadoop installation. The application was tested with:

- Hadoop-0.20.2 (vanilla Hadoop from Apache)
- Hadoop-0.20.2-cdh3u1-SNAPSHOT (from Cloudera)

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
    -map "`cat map.txt`"                                            \
    -reduce "`cat reduce.txt`"                                      \
    -input <input path(s) in HDFS>                                  \
    -output <output path in HDFS>                                   \

### Magic Parameters and Magic Imports

Map and reduce scripts must contain syntactically correct Groovy or Java code. Classes which are
not automatically imported have to be fully qualified or have to be imported as in every other Java/Groovy application.

The following parameters are injected and accessible within the `map` code:

- `key` : The input key type as provided by the Inputformat
- `value` : The input value type as provided by the Inputformat
- `context` : `org.apache.hadoop.mapreduce.Mapper.Context`
- `log` : `java.util.logging.Logger` 
- `outKey` : a single output key instance which will be reused 
- `outValue` : a single output value instance which will be reused 

The following parameters are injected and accessible within the `reduce` and `combine` code:

- `key` : The input key type as specified by `mapred.output.key.class` or `mapred.mapoutput.key.class`
- `values` : `java.lang.Iterable` of the generic type specified by `mapred.output.value.class` or `mapred.mapoutput.value.class`
- `context` : `org.apache.hadoop.mapreduce.Reducer.Context`
- `log` : `java.util.logging.Logger` 
- `outKey` : a single output key instance which will be reused 
- `outValue` : a single output value instance which will be reused 

Additionally, some classes are already imported for convenience. In addition to the 
[Groovy default imports](http://groovy.codehaus.org/Differences+from+Java), all the `Writable`s from
`org.apache.hadoop.io` are accessible and do not need to be fully qualified.

### Command Line Interface		    	
	
    usage: $ hadoop jar groovy-hadoop-VERSION.jar [generic hadoop options]
             [groovy-hadoop options]
    Note: groovy-hadoop options override generic hadoop options.
     -archives <paths>                 Generic hadoop option. Comma separated
                                       archives to be unarchived on the
                                       compute machines.
     -combine <combine script>         Executes the script in the combine
                                       phase. Available parameters: key,
                                       values, context, outKey, outValue
     -combinesplits <max split size>   Sets maximum split size for
                                       InputFormats extending FileInputFormat.
                                       Use "0" to prevent the applicaton from
                                       using combine splits. Example values:
                                       "128M", "1G", "134217728". Default is
                                       "512M".
     -conf <configuration file>        Generic hadoop option. Specify an
                                       application configuration file.
     -D <property=value>               Generic hadoop option. Set arbitrary
                                       property value.
     -files <paths>                    Generic hadoop option. Comma separated
                                       files to be copied to the map reduce
                                       cluster.
     -fs <local|namenode:port>         Generic hadoop option. Sets
                                       "fs.default.name" property.
     -help                             Show usage information
     -input <input paths>              Convenience parameter. Sets the
                                       "mapred.input.dir" property.
     -jt <local|jobtracker:port>       Generic hadoop option. Sets
                                       "mapred.job.tracker" property.
     -jvmreuse <reuse value>           Sets "mapred.job.reuse.jvm.num.tasks"
                                       property. Default value is "-1" meaning
                                       "use JVM instances as often as
                                       possible".
     -libjars <paths>                  Generic hadoop option. Comma separated
                                       jar files to include in the classpath.
     -map <map script>                 Executes the script in the map phase.
                                       Available parameters: key, value,
                                       context, outKey, outValue
     -output <output paths>            Convenience parameter. Sets the
                                       "mapred.output.dir" property. The
                                       corresponding path should usually not
                                       exist.
     -quiet                            Do not use verbose output.
     -reduce <reduce script>           Executes the script in the reduce
                                       phase. Available parameters: key,
                                       values, context, outKey, outValue

### Combining groovy-hadoop with Custom Libraries

Suppose there is custom `Writable` implementation like

    package my.fancy;

    import java.io.DataInput;
    import java.io.DataOutput;
    import java.io.IOException;

    import org.apache.hadoop.io.Text;
    import org.apache.hadoop.io.Writable;

    public class CustomWritable implements Writable {

        private String value;
	
        public CustomWritable(String value) {
            this.value = value;
        }
	
    	@Override
    	public String toString() {
    		return this.value;
    	}
	
    	@Override
    	public void readFields(DataInput in) throws IOException {
    		this.value = Text.readString(in);
    	}

    	@Override
    	public void write(DataOutput out) throws IOException {
    		Text.writeString(out, this.value);
    	}
    }

Furthermore, suppose that this `CustomWritable` is properly bundled within a JAR file called `my-writable.jar`.
Then it is possible to access and use this class from the map and reduce scripts with a call like 

    $ hadoop jar groovy-hadoop-0.1.0.jar                                     \
    -libjars my-writable.jar                                                 \
    -D mapred.output.key.class=my.fancy.CustomWritable                       \
    -D mapred.output.value.class=org.apache.hadoop.io.LongWritable           \
    -D mapred.reduce.tasks=0                                                 \
    -map 'context.write(new my.fancy.CustomWritable(value.toString()), key)' \
    -input <input path(s) in HDFS>                                           \
    -output <output path in HDFS>                                            \       		    	
		    	
### Performance

- Short version:
*First make it work, then make it fast!*
Performance will be targeted with release 0.2.0.
- Longer version:
Although the primary target of this project is not delivering incredible performance 
writing *ad-hoc* map/reduce applications is more fun if the jobs run as fast 
as possible. In first (unorganized) benchmarks *groovy-hadoop* jobs took about 
10 to 20 percent longer than analogous *hadoop-streaming* jobs.   		    	
		    			    	
## Developer Guide

### Build the Project

The project's sources are written in Groovy (version: 1.8), the project is built 
with [Gradle](http://www.gradle.org/) (version: 1.0-milestone-1). To set up an Eclipse project execute

    $ gradle eclipse
    
To create an executable JAR file in the `build/libs` folder type

    $ gradle build
    
The resulting JAR file will be executable and contains the necessary Groovy libraries in a `lib` folder
in its root directory.
        
## License

This project is licensed under the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0). 
A copy of it can be found in the main project folder. In short: this project is (obviously) open source and free. 
 