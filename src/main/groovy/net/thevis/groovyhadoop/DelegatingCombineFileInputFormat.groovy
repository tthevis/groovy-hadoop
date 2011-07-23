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

import java.io.IOException;

import net.thevis.groovyhadoop.backport.CombineFileRecordReader;

import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.util.ReflectionUtils;

/**
 * @author Thomas Thevis
 *
 */
class DelegatingCombineFileInputFormat extends
		net.thevis.groovyhadoop.backport.CombineFileInputFormat {
			
	/** {@inheritDoc} */
	@Override
	public RecordReader createRecordReader(InputSplit split,
			TaskAttemptContext context) throws IOException {
		return new CombineFileRecordReader(split, context, DelegatingCombineFileRecordReader.class)	
	}
}
