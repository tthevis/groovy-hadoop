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

import groovy.util.logging.Log

import org.apache.hadoop.io.ArrayWritable
import org.apache.hadoop.io.BooleanWritable
import org.apache.hadoop.io.BytesWritable
import org.apache.hadoop.io.DoubleWritable
import org.apache.hadoop.io.FloatWritable
import org.apache.hadoop.io.IntWritable
import org.apache.hadoop.io.LongWritable
import org.apache.hadoop.io.MapWritable
import org.apache.hadoop.io.NullWritable
import org.apache.hadoop.io.ObjectWritable
import org.apache.hadoop.io.Text
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer
import org.codehaus.groovy.control.customizers.ImportCustomizer


/**
 * Parses and prepares scripts. Injects Hadoop defaults imports and a logger.
 * 
 * @author Thomas Thevis
 * @since 0.1.0
 */
class ScriptProvider {

	Script getParsedScript(String scriptText) {
		def configuration = new CompilerConfiguration()
		configuration.addCompilationCustomizers(new ASTTransformationCustomizer(Log))
		
		def custo = new ImportCustomizer()
		custo.addImports(Text.name, LongWritable.name, IntWritable.name, 
			BooleanWritable.name, ArrayWritable.name, BytesWritable.name,
			BytesWritable.name, DoubleWritable.name, FloatWritable.name,
			MapWritable.name, NullWritable.name, ObjectWritable.name)
		configuration.addCompilationCustomizers(custo)
		
		def shell = new GroovyShell(configuration)
		def script = shell.parse(scriptText)
		
		return script;
	}
}
