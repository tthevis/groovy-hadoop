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

package net.thevis.groovyhadoop;

import groovy.lang.Script;

import java.io.IOException;

import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.Mapper.Context;

/**
 * @author Thomas Thevis
 *
 */
class ScriptReducer<KEY_IN, VALUE_IN, KEY_OUT, VALUE_OUT> 
		extends Reducer<KEY_IN, VALUE_IN, KEY_OUT, VALUE_OUT> {

	static final String CONF_REDUCE_SCRIPT = 'groovyhadoop.reduce.script'

	private Script script

	@Override
	protected void setup(Context context) throws IOException, InterruptedException {
		def scriptText = context.getConfiguration().get(CONF_REDUCE_SCRIPT)
		def scriptProvider = new ScriptProvider()
		this.script = scriptProvider.getParsedScript(scriptText)
	}
	
	@Override
	protected void reduce(KEY_IN key, Iterable<VALUE_IN> values, Context context) 
		throws IOException, InterruptedException {
			
		script.binding.key = key
		script.binding.values = values
		script.binding.context = context
			
		script.run()
	}
}
