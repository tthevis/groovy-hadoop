/*
 * Copyright 2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.thevis.groovyhadoop

import org.apache.hadoop.conf.Configured
import org.apache.hadoop.mapreduce.Job
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.util.Tool
import org.apache.hadoop.util.ToolRunner

/**
 * The application's entry point.
 * 
 * @author Thomas Thevis
 * @since 0.1.0
 */
class Main extends Configured implements Tool {
	
	def cli;
	
	{
		cli = new CliBuilder(usage: '$ hadoop jar groovy-hadoop-VERSION.jar [generic hadoop options] [groovy-hadoop options]',
			header: 'Note: groovy-hadoop options override generic hadoop options.')
		cli.h longOpt: 'help', 'Show usage information'
		cli.map args: 1, argName: 'map script', 'Executes the script in the map phase. Available parameters: key, value, context'
		cli.reduce args: 1, argName: 'reduce script', 'Executes the script in the reduce phase. Available parameters: key, values, context'
		cli.input args: 1, argName: 'input paths', 'Convenience parameter. Sets the "mapred.input.dir" property.'
		cli.output args: 1, argName: 'output paths', 'Convenience parameter. Sets the "mapred.output.dir" property. The corresponding path should usually not exist.'
		cli.q longOpt: 'quiet', 'Do not use verbose output.'	
		
		/* generic hadoop options as specified by GenericOptionsParser */
		cli.fs args: 1, argName: 'local|namenode:port', 'Generic hadoop option. Sets "fs.default.name" property.'
		cli.jt args: 1, argName: 'local|jobtracker:port', 'Generic hadoop option. Sets "mapred.job.tracker" property.'
		cli.conf args: 1, argName: 'configuration file', 'Generic hadoop option. Specify an application configuration file.'
		cli.'D' args: 1, argName: 'property=value', 'Generic hadoop option. Set arbitrary property value.'
		cli.libjars args: 1, argName: 'paths', 'Generic hadoop option. Comma separated jar files to include in the classpath.'
		cli.files args: 1 , argName: 'paths', 'Generic hadoop option. Comma separated files to be copied to the map reduce cluster.'
		cli.archives args: 1, argName: 'paths', 'Generic hadoop option. Comma separated archives to be unarchived on the compute machines.'
	}
	
	int run(String[] args) {
				
		def options = cli.parse(args)
		if (options.h) {
			cli.usage()
			return 0;
		}
		def job = new Job(getConf());
		job.setJarByClass(getClass())
		
		if (options.map) {
			job.setMapperClass(ScriptMapper.class)
			job.getConfiguration().set(ScriptMapper.CONF_MAP_SCRIPT, options.map)
		}
		if (options.reduce) {
			job.setReducerClass(ScriptReducer.class)
			job.getConfiguration().set(ScriptReducer.CONF_REDUCE_SCRIPT, options.reduce)
		}
		if (options.input) {
			job.getConfiguration().set('mapred.input.dir', options.input)
		}
		if (options.output) {
			job.getConfiguration().set('mapred.output.dir', options.output)
		}
		def inputFormatClass = job.getInputFormatClass()
		if (FileInputFormat.class.isAssignableFrom(inputFormatClass)) {
			job.setInputFormatClass(DelegatingCombineFileInputFormat.class)
			job.getConfiguration().set(DelegatingCombineFileRecordReader.CONF_ORIGINAL_INPUT_FORMAT, inputFormatClass.name)
		}
		
		def success = job.waitForCompletion(!options.q);		
		return success ? 0 : 1
	}
	
			
	static main(String[] args) {
		ToolRunner.run(new Main(), args)		
	}
}
