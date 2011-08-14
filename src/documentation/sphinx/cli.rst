Command Line Interface
======================

*groovy-hadoop* is a command line tool which integrates with the ``hadoop jar``
application.

The syntax is ::

    $ hadoop jar groovy-hadoop-0.2.0.jar [generic hadoop options] [groovy-hadoop options]

with the following rules:

#.    All generic hadoop options must be specified before *groovy-hadoop* options to be taken into account
#.    In case of conflicting options *groovy-hadoop* options override the generic hadoop ones. For example,
      if both ``-D mapred.input.dir=some/path`` and ``-input some/other/path`` are set, the value ``some/other/path`` will be used.

*groovy-hadoop* Options
-----------------------

.. option:: -help

   prints help text with a description of both generic hadoop and *groovy-hadoop* options

.. option:: -input  <input paths> 

   Convenience parameter. Sets the ``mapred.input.dir`` property

.. option:: -output  <output paths>

   Convenience parameter. Sets the ``mapred.output.dir`` property. The corresponding 
   path should usually not exist.

.. option:: -map  <map script>

   Executes the script in the map phase. Available parameters: key, value,
   context, outKey, outValue, and logger

.. option:: -reduce  <reduce script>

   Executes the script in the reduce phase. Available parameters: key, values,
   context, outKey, outValue, and logger

.. option:: -combine  <combine script> 

   Executes the script in the combine phase. Available parameters: key, values, 
   context, outKey, outValue, and logger

.. option:: -outputKeyClass  <class name>

   Convenience parameter for the ``mapred.output.key.class`` property. All hadoop 
   writables can be specified with only their simple class name, like ``Text`` for example.

.. option:: -outputValueClass  <class name>

   Convenience parameter for the ``mapred.output.value.class`` property. All hadoop 
   writables can be specified with only their simple class name, like ``Text`` for example.

.. option:: -mapOutputKeyClass  <class name>

   Convenience parameter for the ``mapred.mapoutput.key.class`` property. All hadoop 
   writables can be specified with only their simple class name, like ``Text`` for example.

.. option:: -mapOutputValueClass  <class name>

   Convenience parameter for the ``mapred.mapoutput.value.class`` property. All hadoop 
   writables can be specified with only their simple class name, like ``Text`` for example.

.. option:: -combinesplits  <max split size>

   Sets the maximum split size for InputFormats extending ``FileInputFormat``. Use ``0`` to prevent
   the applicaton from using combine splits, at all. Example values: ``128M``, ``1G``, ``134217728``. 
   Default value is ``512M``.

.. option:: -jvmreuse  <reuse value>

   Sets the ``mapred.job.reuse.jvm.num.tasks`` property. **NOTE:** In contrast to generic hadoop, 
   the default value is ``-1`` meaning "use JVM instances as often as possible".

.. option:: -quiet

   Do not use verbose output.


Generic Hadoop Options
----------------------

Just for the sake of completeness, the following list gives an overview on the generic hadoop options which are
supported and trasparently delegated to the ``hadoop jar`` application. The parameter value names and options descriptions
are taken from the ``org.apache.hadoop.util.GenericOptionsParser`` class.

.. option:: -archives  <paths>

   Comma separated archives to be unarchived on the compute machines.

.. option:: -conf  <configuration file>

   Specify an application configuration file.

.. option:: -D  <property=value>

   Set arbitrary property value.

.. option:: -files  <paths>

   Comma separated files to be copied to the map reduce cluster.

.. option:: -fs  <local|namenode:port>

   Sets ``fs.default.name`` property.

.. option:: -jt  <local|jobtracker:port>

   Sets ``mapred.job.tracker`` property.

.. option:: -libjars  <paths>

   Comma separated jar files to include in the classpath.

