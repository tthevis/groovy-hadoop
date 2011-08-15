Tutorial
========

1. The Ubiquitous Wordcount
---------------------------

.. _Wordcount: http://wiki.apache.org/hadoop/WordCount

For starters, we'll use a well-known map/reduce application: Wordcount_. We'll start simple
and refine the example codes in several iterations to get acquainted with the most important
*groovy-hadoop* features and command line options to end up with short and efficient code. 
Note that all following program versions are fully functional and that it's merely a matter 
of taste and runtime efficiency which one to choose.

First attempt: Borrow the wordcount example code
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

For this example, we'll cut the beef from the example (with small modifications) and forget
all the boilerplate stuff.
Create a file ``map.txt`` with the following content (The file creation part is only 
for a clear arrangement of the example. One could as well provide all the Java code inline at 
the command line):  

.. code-block:: java

    String line = value.toString();
    StringTokenizer tokenizer = new StringTokenizer(line);
    Text word = new Text();
    IntWritable one = new IntWritable(1);
    while (tokenizer.hasMoreTokens()) {
        word.set(tokenizer.nextToken());
        context.write(word, one);
    }

Additionally, create a file ``reduce.txt`` like: 

.. code-block:: java

    int sum = 0;
    for (IntWritable val : values) {
        sum += val.get();
    }
    context.write(key, new IntWritable(sum));

Now, what are the differences between traditional ``hadoop jar`` and *groovy-hadoop* execution?
To make the original Wordcount_ example run, one would have to 

#. compile all the beefy code together with the boilerplate stuff, 
#. package a Jar file (ideally executable) and
#. run ``$ hadoop jar wordcount.jar <input path(s)> <output path>``

In contrast, with *groovy-hadoop* all one would have to do is execute

.. code-block:: bash

    $ hadoop jar groovy-hadoop-0.2.0.jar   \
    -outputKeyClass Text                   \
    -outputValueClass IntWritable          \
    -map "`cat map.txt`"                   \
    -reduce "`cat reduce.txt`"             \
    -input <input path(s) in HDFS>         \
    -output <output path in HDFS>          \

No boilerplate code, no ``javac``, and no ``jar``!

Next step: Performance improvements
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

#. Reuse output keys and values.
   To omit object creation and garbage collection overhead, a common technique is to use
   single instances for output keys and values in ``Mapper`` and ``Reducer`` implementations.
   For this purpose, *groovy-hadoop* injects the objects ``outKey`` and ``outValue`` into all
   map, reduce, and combine scripts.
#. Use a ``Combiner`` 
   Nothing new here. For applications suited for map-side reduce operations, there is a ``-combine``
   command line parameter which works exactly as ``-reduce``.
#. Use `CombineSplits`
   A well-known way to deal with lots of small input files is to use the ``CombineFileInputFormat``. 
   Additionally, this hadoop ``InputFormat`` comes in very handy if the intention is to process more data
   at once in an ``InputSplit`` than the HDFS block size allows. *groovy-hadoop* uses a custom ``CombineFileInputFormat``
   implementation by default if the specified ``InputFormat`` extends ``FileInputFormat``. The default input split size is
   set to 512M. To disable this behavior, the command line option ``-combinesplits 0`` has to be used.
#. Reuse JVMs for maps and reduces.
   JVM startup and cleanup takes some time and the overall job performance can be significantly increased by simply
   reusing these instances. Therefore, *groovy-hadoop*'s default behavior is to set the ``mapred.job.reuse.jvm.num.tasks`` parameter 
   to ``-1``. If this behavior is not desired, the command line option ``-jvmreuse <reuse value>`` has to be used. 


Final version: Make it groovy!
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

With Groovy instead of Java syntax the complete example reads like

.. code-block:: bash

    $ hadoop jar groovy-hadoop-0.2.0.jar               \
    -outputKeyClass Text                               \
    -outputValueClass IntWritable                      \
    -map 'outValue.set(1)
          value.toString().tokenize().each{
              outKey.set(it)
              context.write(outKey,outValue)
          }'                                           \
    -reduce 'def sum=0
             values.each{
               sum += it.get()
             }
             outValue.set(sum)
             context.write(key,outValue)'              \
    -combine 'def sum=0
              values.each{
                sum += it.get()
              }
              outValue.set(sum)
              context.write(key,outValue)'              \
    -input <input path(s) in HDFS>                      \
    -output <output path in HDFS>                       \

2. Use custom ``Writables``
---------------------------

Integration of custom classes within the scripts is fairly straightforward:

#. Add the corresponding Jar file to the classpath using hadoop's ``-libjars`` option
#. Refer to the class in the script via the fully qualified name

**Note:** ``outKey`` and ``outValue`` usage (see above) does also work for custom classes.

Example
^^^^^^^

Suppose, there is a custom ``Writable`` implementation like

.. code-block:: java

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

Furthermore, suppose that this ``CustomWritable`` is properly bundled within a Jar file called ``my-writable.jar``.
Then it is possible to access and use this class from the ``map`` and ``reduce`` scripts with a call like

.. code-block:: bash

    $ hadoop jar groovy-hadoop-0.2.0.jar                                     \
    -libjars my-writable.jar                                                 \
    -outputKeyClass my.fancy.CustomWritable                                  \
    -outputValueClass LongWritable                                           \
    -D mapred.reduce.tasks=0                                                 \
    -map 'context.write(new my.fancy.CustomWritable(value.toString()), key)' \
    -input <input path(s) in HDFS>                                           \
    -output <output path in HDFS>                                            \




