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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.text.MaskFormatter.LiteralCharacter;

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
		cli.help 'Show usage information'
		cli.map args: 1, argName: 'map script', 'Executes the script in the map phase. Available parameters: key, value, context, outKey, outValue'
		cli.reduce args: 1, argName: 'reduce script', 'Executes the script in the reduce phase. Available parameters: key, values, context, outKey, outValue'
		cli.input args: 1, argName: 'input paths', 'Convenience parameter. Sets the "mapred.input.dir" property.'
		cli.output args: 1, argName: 'output paths', 'Convenience parameter. Sets the "mapred.output.dir" property. The corresponding path should usually not exist.'
		cli.quiet 'Do not use verbose output.'
		cli.jvmreuse args: 1, argName: 'reuse value', 'Sets "mapred.job.reuse.jvm.num.tasks" property. Default value is "-1" meaning "use JVM instances as often as possible".'	
		cli.combinesplits args: 1, argName: 'max split size', 'Sets maximum split size for InputFormats extending FileInputFormat. Use "0" to prevent the applicaton from using combine splits. Example values: "128M", "1G", "134217728". Default is "512M".'
		cli.combine args: 1, argName: 'combine script', 'Executes the script in the combine phase. Available parameters: key, values, context, outKey, outValue'
				
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
		if (options.help) {
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
		if (options.combine) {
			job.setCombinerClass(ScriptCombiner.class)
			job.getConfiguration().set(ScriptCombiner.CONF_COMBINE_SCRIPT, options.combine)
		}
		if (options.input) {
			job.getConfiguration().set('mapred.input.dir', options.input)
		}
		if (options.output) {
			job.getConfiguration().set('mapred.output.dir', options.output)
		}
		job.getConfiguration().set("mapred.job.reuse.jvm.num.tasks", options.jvmreuse ?: "-1")
		
		if (options.combinesplits != "0") {
			def inputFormatClass = job.getInputFormatClass()
			if (FileInputFormat.class.isAssignableFrom(inputFormatClass)) {
			    job.setInputFormatClass(DelegatingCombineFileInputFormat.class)
			    job.getConfiguration().set(DelegatingCombineFileRecordReader.CONF_ORIGINAL_INPUT_FORMAT, inputFormatClass.name)
			    def splitSize = parseBytes(options.combinesplits ?: "512M")
				job.getConfiguration().setLong("mapreduce.input.fileinputformat.split.maxsize", splitSize)
			}
		}
		def success = job.waitForCompletion(!options.quiet);		
		return success ? 0 : 1
	}
			
	long parseBytes(String literal) {
		def matcher = new Matcher(Pattern.compile(/(\d+)(M|m|G|g)?\b/), literal)
		if (!matcher.matches()) {
			throw new IllegalArgumentException("invalid input value: ${literal}")
		}
		def base = Long.parseLong(matcher[0][1])
		if (!matcher[0][2]) {
			return base
		}
		return matcher[0][2] in ['M', 'm'] ? base * 1024 * 1024 : base * 1024 * 1024 * 1024
	} 
	
	static main(String[] args) {
		ToolRunner.run(new Main(), args)		
	}
}
