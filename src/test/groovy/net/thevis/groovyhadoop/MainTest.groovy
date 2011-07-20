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

import static org.junit.Assert.*

/**
 * Performs blackbox tests with a local Jobtracker in the local 
 * filesystem.
 * 
 * @author Thomas Thevis
 * @since 0.1.0
 */
class MainTest extends GroovyTestCase {

	private File testOutput
	
	void setUp() {
		this.testOutput = new File('build/test-main')
	}
	
	void tearDown() {
		if (this.testOutput?.exists()) {
			this.testOutput.deleteDir()
		}
	}
	
	void testWordcount_groovy() {
		def map = 'value.toString().tokenize().each { context.write(new Text(it), new LongWritable(1)) }'
		def reduce = 'def sum = 0; values.each { sum += it.get() }; context.write(key, new LongWritable(sum))'
		def input = './LICENSE-2.0'
		
		def result = executeMain(map, reduce, input)
		
		def expectedResult = getClass().getClassLoader()
			.getResourceAsStream('wordcount.result').text
		assert result == expectedResult
	}

	void testWordcount_groovy_withOutKeyAndOutValue() {
		def map = 'outValue.set(1); value.toString().tokenize().each { outKey.set(it); context.write(outKey, outValue) }'
		def reduce = 'def sum = 0; values.each { sum += it.get() }; outValue.set(sum); context.write(key, outValue)'
		def input = './LICENSE-2.0'
		
		def result = executeMain(map, reduce, input)
		
		def expectedResult = getClass().getClassLoader()
			.getResourceAsStream('wordcount.result').text
		assert result == expectedResult
	}

	
	void testWordcount_java() {
		def map = '''
			Scanner scanner = new Scanner(value.toString()); 
			while (scanner.hasNext()) { 
				context.write(new Text(scanner.next()), new LongWritable(1)); 
			}
		'''
		def reduce = '''
			int sum = 0; 
			for (LongWritable value : values) {
				sum += value.get();
			}
			context.write(key, new LongWritable(sum));
		'''
		def input = './LICENSE-2.0'
		
		def result = executeMain(map, reduce, input)
		
		def expectedResult = getClass().getClassLoader()
			.getResourceAsStream('wordcount.result').text
		assert result == expectedResult
	}
		
	def executeMain = { map, reduce, inputPath ->
		Main.main((String[])[
			'-D', 'mapred.output.key.class=org.apache.hadoop.io.Text',
			'-D', 'mapred.output.value.class=org.apache.hadoop.io.LongWritable',
			'-map', map,
			'-reduce', reduce,
			'-input', inputPath, 
			'-output', this.testOutput.path])
		
		return new File(this.testOutput, "part-r-00000").text
	}
}
