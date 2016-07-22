/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
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
 * </p>
 */

package com.dangdang.ddframe.job.api.type.dataflow.throughput;

import com.dangdang.ddframe.job.api.job.dataflow.AbstractDataFlowElasticJob;
import com.dangdang.ddframe.job.api.job.dataflow.DataFlowType;
import com.dangdang.ddframe.job.api.type.ElasticJobAssert;
import com.dangdang.ddframe.job.api.type.dataflow.AbstractDataFlowElasticJobTest;
import com.dangdang.ddframe.job.api.type.fixture.FooUnstreamingThroughputDataFlowElasticJob;
import com.dangdang.ddframe.job.api.type.fixture.JobCaller;
import lombok.AccessLevel;
import lombok.Getter;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Getter(AccessLevel.PROTECTED)
public class UnstreamingThroughputDataFlowElasticJobTest extends AbstractDataFlowElasticJobTest {
    
    @Test
    public void assertExecuteWhenFetchDataIsNull() {
        when(getJobCaller().fetchData()).thenReturn(null);
        getDataFlowElasticJob().execute();
        verify(getJobCaller(), times(0)).processData(any());
        ElasticJobAssert.verifyForIsNotMisfire(getJobFacade(), getShardingContext());
        ElasticJobAssert.assertProcessCountStatistics(0, 0);
    }
    
    @Test
    public void assertExecuteWhenFetchDataIsEmpty() {
        when(getJobCaller().fetchData()).thenReturn(Collections.emptyList());
        getDataFlowElasticJob().execute();
        verify(getJobCaller(), times(0)).processData(any());
        ElasticJobAssert.verifyForIsNotMisfire(getJobFacade(), getShardingContext());
        ElasticJobAssert.assertProcessCountStatistics(0, 0);
    }
    
    @Test
    public void assertExecuteWhenFetchDataIsNotEmptyAndConcurrentDataProcessThreadCountIsOne() {
        when(getJobCaller().fetchData()).thenReturn(Arrays.<Object>asList(1, 2));
        when(getJobFacade().getConcurrentDataProcessThreadCount()).thenReturn(1);
        getDataFlowElasticJob().execute();
        verify(getJobCaller()).processData(1);
        verify(getJobCaller()).processData(2);
        verify(getJobFacade()).getConcurrentDataProcessThreadCount();
        ElasticJobAssert.verifyForIsNotMisfire(getJobFacade(), getShardingContext());
        ElasticJobAssert.assertProcessCountStatistics(1, 0);
    }
    
    @Test
    public void assertExecuteWhenFetchDataIsNotEmptyAndDataIsOne() {
        when(getJobCaller().fetchData()).thenReturn(Collections.<Object>singletonList(1));
        when(getJobFacade().getConcurrentDataProcessThreadCount()).thenReturn(2);
        getDataFlowElasticJob().execute();
        verify(getJobCaller()).processData(1);
        verify(getJobFacade()).getConcurrentDataProcessThreadCount();
        ElasticJobAssert.verifyForIsNotMisfire(getJobFacade(), getShardingContext());
        ElasticJobAssert.assertProcessCountStatistics(1, 0);
    }
    
    @Test
    public void assertExecuteWhenFetchDataIsNotEmptyAndConcurrentDataProcessThreadCountIsOneAndProcessFailureWithException() {
        when(getJobCaller().fetchData()).thenReturn(Arrays.<Object>asList(1, 2));
        doThrow(IllegalStateException.class).when(getJobCaller()).processData(2);
        when(getJobFacade().getConcurrentDataProcessThreadCount()).thenReturn(1);
        getDataFlowElasticJob().execute();
        verify(getJobCaller()).processData(1);
        verify(getJobCaller()).processData(2);
        verify(getJobFacade()).getConcurrentDataProcessThreadCount();
        ElasticJobAssert.verifyForIsNotMisfire(getJobFacade(), getShardingContext());
        ElasticJobAssert.assertProcessCountStatistics(0, 1);
    }
    
    @Test
    public void assertExecuteWhenFetchDataIsNotEmptyAndConcurrentDataProcessThreadCountIsOneAndProcessFailureWithWrongResult() {
        when(getJobCaller().fetchData()).thenReturn(Arrays.<Object>asList(1, 2));
        when(getJobFacade().getConcurrentDataProcessThreadCount()).thenReturn(1);
        getDataFlowElasticJob().execute();
        verify(getJobCaller()).processData(1);
        verify(getJobCaller()).processData(2);
        verify(getJobFacade()).getConcurrentDataProcessThreadCount();
        ElasticJobAssert.verifyForIsNotMisfire(getJobFacade(), getShardingContext());
        ElasticJobAssert.assertProcessCountStatistics(1, 0);
    }
    
    @Test
    public void assertExecuteWhenFetchDataIsNotEmptyForMultipleThread() {
        when(getJobCaller().fetchData()).thenReturn(Arrays.<Object>asList(1, 2, 3, 4));
        when(getJobFacade().getConcurrentDataProcessThreadCount()).thenReturn(2);
        getDataFlowElasticJob().execute();
        verify(getJobCaller()).processData(1);
        verify(getJobCaller()).processData(2);
        verify(getJobCaller()).processData(3);
        verify(getJobCaller()).processData(4);
        verify(getJobFacade()).getConcurrentDataProcessThreadCount();
        ElasticJobAssert.verifyForIsNotMisfire(getJobFacade(), getShardingContext());
        ElasticJobAssert.assertProcessCountStatistics(2, 0);
    }
    
    @Test
    public void assertUpdateOffset() {
        getDataFlowElasticJob().updateOffset(0, "offset1");
        verify(getJobFacade()).updateOffset(0, "offset1");
    }
    
    @Override
    protected DataFlowType getDataFlowType() {
        return DataFlowType.THROUGHPUT;
    }
    
    @Override
    protected boolean isStreamingProcess() {
        return false;
    }
    
    @Override
    protected AbstractDataFlowElasticJob createDataFlowElasticJob(final JobCaller jobCaller) {
        return new FooUnstreamingThroughputDataFlowElasticJob(jobCaller);
    }
}
