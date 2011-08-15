.. groovy-hadoop documentation master file, created by
   sphinx-quickstart on Mon Jul 25 21:20:07 2011.
   You can adapt this file completely to your liking, but it should at least
   contain the root `toctree` directive.

About groovy-hadoop
===================

.. _Groovy: http://groovy.codehaus.org/
.. _Hadoop: http://hadoop.apache.org/

This project's intention is to bring the power and expressiveness of Groovy_
to the almighty Hadoop_ framework to support 
*ad-hoc* Hadoop job execution 
right from the command line with expressive Java or Groovy syntax and without 
the need to create Java projects and executable JAR files.

Requirements
------------

The only requirement is a working Hadoop installation (the ``hadoop jar`` application must be executable). 
*groovy-hadoop* was tested with clusters featuring:

* Hadoop-0.20.2 (vanilla Hadoop from Apache)
* Hadoop-0.20.2-cdh3u1-SNAPSHOT (from Cloudera)

Target audience:
----------------

.. _hadoop-streaming: http://hadoop.apache.org/common/docs/current/streaming.html

* People who like to think and to code in terms of map/reduce but are a little annoyed by the need of setting up Java projects and creating executable Hadoop JAR files for even the most basic map/reduce applications
* People who would like to combine hadoop-streaming_ with Groovy but do not have the possibility to install Groovy on the Hadoop cluster nodes
* People who would like to run ad-hoc processing jobs and want to use their custom ``Writable`` implementations via the Java/Groovy API
* People wanting to prototype map/reduce applications before developing proper Java projects

Who will not benefit from using groovy-hadoop?
----------------------------------------------
.. _Pig: http://pig.apache.org/
.. _Hive: http://hive.apache.org/ 

* Performance hunters. After all, it is Groovy, it is dynamic, it is not completely pre-compiled. However, it seems to be faster than hadoop-streaming.
* People striving for *yet another abstraction layer (TM)* on top of map/reduce. Please consider using Pig_ or Hive_ or something similar this case.
* Scripting Gurus. If you know how to write your custom queries using your scripting language of choice in concert with hadoop-streaming_, chances are that you can do without this project.

License
-------

.. _`Apache License, Version 2.0`: http://www.apache.org/licenses/LICENSE-2.0

This project is licensed under the `Apache License, Version 2.0`_. 
A copy of it can be found in the project's main folder. 
In short: this project is (obviously) open source and free.


.. toctree::
   :maxdepth: 2
   :hidden:
	
   Home <self>
   tutorial
   cli
   changes
   downloads


