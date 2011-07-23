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

import org.apache.hadoop.mapreduce.Reducer
import org.apache.hadoop.mapreduce.Reducer.Context
import org.apache.hadoop.util.ReflectionUtils

/**
 * Reads reduce text from configuration and parses and prepares script once
 * during {@link #setup(Context)}.  
 * 
 * @author Thomas Thevis
 * @since 0.1.0
 */
class ScriptReducer<KEY_IN, VALUE_IN, KEY_OUT, VALUE_OUT> 
		extends Reducer<KEY_IN, VALUE_IN, KEY_OUT, VALUE_OUT> {

	static final String CONF_REDUCE_SCRIPT = 'groovyhadoop.reduce.script'

	private Script script

	private KEY_IN lastKey
	private Iterable<VALUE_IN> lastValues
	private Context lastContext

	@Override
	protected void setup(Context context) throws IOException, InterruptedException {
		def scriptText = context.getConfiguration().get(getScriptConfigKey())
		def scriptProvider = new ScriptProvider()
		this.script = scriptProvider.getParsedScript(scriptText)

		this.script.binding.outKey = ReflectionUtils.newInstance(
				context.getOutputKeyClass(), context.getConfiguration())
		this.script.binding.outValue = ReflectionUtils.newInstance(
				context.getOutputValueClass(), context.getConfiguration())
	}

	protected String getScriptConfigKey() {
		return CONF_REDUCE_SCRIPT;
	}
		
	@Override
	protected void reduce(KEY_IN key, Iterable<VALUE_IN> values, Context context) 
			throws IOException, InterruptedException {		
		
		if (!key.is(lastKey)) {
			script.binding.key = key;
			lastKey = key
		}		
		if (!values.is(lastValues)) {
			script.binding.values = values;
			lastValues = values
		}		
		if (!context.is(lastContext)) {
			script.binding.context = context;
			lastContext = context
		}		
		
		this.script.run()		
	}
}
