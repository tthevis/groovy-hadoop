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

import java.io.IOException

import net.thevis.groovyhadoop.backport.CombineFileSplit

import org.apache.hadoop.mapreduce.InputSplit
import org.apache.hadoop.mapreduce.RecordReader
import org.apache.hadoop.mapreduce.TaskAttemptContext
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat
import org.apache.hadoop.mapreduce.lib.input.FileSplit
import org.apache.hadoop.util.ReflectionUtils

/**
 * @author Thomas Thevis
 *
 */
class DelegatingCombineFileRecordReader extends RecordReader {
	
	static final String CONF_ORIGINAL_INPUT_FORMAT = 'groovyhadoop.original.inputformat'
	
	def delegate
	def index
	
	public DelegatingCombineFileRecordReader(
	    CombineFileSplit split, TaskAttemptContext context, Integer idx) throws IOException {
		
		FileSplit fileSplit = new FileSplit(
		split.getPath(idx), split.getOffset(idx), split.getLength(idx), split.getLocations())
		def originalInputFormatClass = context.getConfiguration().getClass(
			CONF_ORIGINAL_INPUT_FORMAT, null)
		FileInputFormat originalInputFormat = ReflectionUtils.newInstance(originalInputFormatClass, context.getConfiguration())
		
	    this.delegate = originalInputFormat.createRecordReader(fileSplit, context)
		this.index = idx
	}
	
	@Override	
	void initialize(InputSplit split, TaskAttemptContext context) throws IOException, InterruptedException {
		FileSplit fileSplit = new FileSplit(
		split.getPath(this.index), split.getOffset(this.index), split.getLength(this.index), split.getLocations())
		this.delegate.initialize(fileSplit, context)
	}	
	
	@Override
	public boolean nextKeyValue() throws IOException, InterruptedException {
		return this.delegate.nextKeyValue()
	}
	
	@Override
	public Object getCurrentKey() throws IOException, InterruptedException {
		return this.delegate.getCurrentKey()
	}
	
	@Override
	public Object getCurrentValue() throws IOException, InterruptedException {
		return this.delegate.getCurrentValue()
	}
	
	@Override
	public float getProgress() throws IOException, InterruptedException {
		return this.delegate.getProgress()
	}
	
	@Override
	public void close() throws IOException {
		this.delegate.close()
	}
}
