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

import java.awt.JobAttributes;
import java.util.concurrent.ConcurrentSkipListMap.Index;

import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

/**
 * @author Thomas Thevis
 *
 */
class Main extends Configured implements Tool {

	
	int run(String[] args) {
		def cli = new CliBuilder(usage: 'Main [-map <map script>] [-reduce <reduce script>] [-input <input paths>] [-output <output path>]')
		cli.h longOpt: 'help', 'Show usage information'
		cli.map args: 1, argName: 'map script', 'Executes the script in the map phase. Available parameters: key, value, context'
		cli.reduce args: 1, argName: 'reduce script', 'Executes the script in the reduce phase. Available parameters: key, values, context'
		cli.input args: 1, argName: 'input paths', 'Convenience parameter. Sets the \'mapred.input.dir\' property.'
		cli.output args: 1, argName: 'output paths', 'Convenience parameter. Sets the \'mapred.output.dir\' property. The corresponding path should usually not exist.'
		
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

		def success = job.waitForCompletion(true);		
		return success ? 0 : 1
	}
	
			
	static main(String[] args) {
		ToolRunner.run(new Main(), args)		
	}
}
